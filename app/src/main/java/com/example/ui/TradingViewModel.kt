package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.TradingRepository
import com.example.data.api.GeminiApi
import com.example.data.local.AlertEntity
import com.example.data.local.PositionEntity
import com.example.data.local.TradingDatabase
import com.example.data.local.WatchlistEntity
import com.example.data.model.MarketRepository
import com.example.data.model.MarketStock
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlin.random.Random

sealed interface AiAnalysisState {
    object Idle : AiAnalysisState
    object Loading : AiAnalysisState
    data class Success(val report: String) : AiAnalysisState
    data class Error(val message: String) : AiAnalysisState
}

class TradingViewModel(
    application: Application,
    private val repository: TradingRepository
) : AndroidViewModel(application) {

    // --- Authentication States ---
    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    private val _userAccounts = MutableStateFlow<Map<String, String>>(
        mapOf("admin@scanner.com" to "admin123", "user" to "password")
    )

    private val _authError = MutableStateFlow<String?>(null)
    val authError: StateFlow<String?> = _authError.asStateFlow()

    fun login(emailOrUsername: String, password: String): Boolean {
        _authError.value = null
        val expectedPassword = _userAccounts.value[emailOrUsername.trim().lowercase()]
        if (expectedPassword != null && expectedPassword == password) {
            _isLoggedIn.value = true
            return true
        } else {
            _authError.value = "Invalid email/username or password"
            return false
        }
    }

    fun loginWithGoogle(email: String) {
        _authError.value = null
        val cleanKey = email.trim().lowercase()
        val currentAccounts = _userAccounts.value.toMutableMap()
        if (!currentAccounts.containsKey(cleanKey)) {
            currentAccounts[cleanKey] = "google_authenticated_oauth"
            _userAccounts.value = currentAccounts
        }
        _isLoggedIn.value = true
    }

    fun register(emailOrUsername: String, password: String): Boolean {
        _authError.value = null
        val cleanKey = emailOrUsername.trim().lowercase()
        if (cleanKey.isEmpty() || password.isEmpty()) {
            _authError.value = "Please fill in all fields"
            return false
        }
        if (_userAccounts.value.containsKey(cleanKey)) {
            _authError.value = "User already exists"
            return false
        }
        val currentAccounts = _userAccounts.value.toMutableMap()
        currentAccounts[cleanKey] = password
        _userAccounts.value = currentAccounts
        _isLoggedIn.value = true
        return true
    }

    fun forgotPassword(emailOrUsername: String): String? {
        _authError.value = null
        val cleanKey = emailOrUsername.trim().lowercase()
        val expectedPassword = _userAccounts.value[cleanKey]
        if (expectedPassword != null) {
            return "Password found: It is '$expectedPassword'. Try resetting or logging in with it."
        } else {
            return "No registered account found with that email or username."
        }
    }

    fun logout() {
        _isLoggedIn.value = false
        _authError.value = null
    }

    fun clearAuthError() {
        _authError.value = null
    }

    // --- State Toggles & Configurations ---
    private val _isHindi = MutableStateFlow(false)
    val isHindi: StateFlow<Boolean> = _isHindi.asStateFlow()

    private val _isPremium = MutableStateFlow(false)
    val isPremium: StateFlow<Boolean> = _isPremium.asStateFlow()

    // --- Search & Filters for Dashboad Scanner ---
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedSector = MutableStateFlow("All")
    val selectedSector: StateFlow<String> = _selectedSector.asStateFlow()

    private val _selectedPattern = MutableStateFlow("All")
    val selectedPattern: StateFlow<String> = _selectedPattern.asStateFlow()

    // Live fluctuate prices simulation to create realistic charts & live tickers
    private val _liveStockMap = MutableStateFlow<Map<String, MarketStock>>(
        MarketRepository.stocks.associateBy { it.symbol }
    )

    // --- Interactive Selected Stock State ---
    private val _selectedStock = MutableStateFlow<MarketStock>(MarketRepository.stocks.first())
    val selectedStock: StateFlow<MarketStock> = _selectedStock.asStateFlow()

    private val _aiAnalysis = MutableStateFlow<AiAnalysisState>(AiAnalysisState.Idle)
    val aiAnalysis: StateFlow<AiAnalysisState> = _aiAnalysis.asStateFlow()

    // --- Alert System States ---
    private val _alertFormState = MutableStateFlow<String?>(null) // Symbol currently creating alert for
    val alertFormState: StateFlow<String?> = _alertFormState.asStateFlow()

    // --- Broker integration state ---
    private val _brokerDialogState = MutableStateFlow<MarketStock?>(null) // Buy dialog
    val brokerDialogState: StateFlow<MarketStock?> = _brokerDialogState.asStateFlow()

    private val brokerPrefs = application.getSharedPreferences("broker_prefs", android.content.Context.MODE_PRIVATE)

    private val _selectedBroker = MutableStateFlow(brokerPrefs.getString("selected_broker", "Zerodha Kite") ?: "Zerodha Kite")
    val selectedBroker: StateFlow<String> = _selectedBroker.asStateFlow()

    private val _brokerApiKey = MutableStateFlow(brokerPrefs.getString("broker_api_key", "") ?: "")
    val brokerApiKey: StateFlow<String> = _brokerApiKey.asStateFlow()

    private val _brokerApiSecret = MutableStateFlow(brokerPrefs.getString("broker_api_secret", "") ?: "")
    val brokerApiSecret: StateFlow<String> = _brokerApiSecret.asStateFlow()

    private val _isBrokerConnected = MutableStateFlow(brokerPrefs.getBoolean("is_broker_connected", false))
    val isBrokerConnected: StateFlow<Boolean> = _isBrokerConnected.asStateFlow()

    fun updateSelectedBroker(broker: String) {
        _selectedBroker.value = broker
        brokerPrefs.edit().putString("selected_broker", broker).apply()
    }

    fun saveBrokerConnection(apiKey: String, apiSecret: String): Boolean {
        if (apiKey.isBlank() || apiSecret.isBlank()) {
            return false
        }
        _brokerApiKey.value = apiKey.trim()
        _brokerApiSecret.value = apiSecret.trim()
        _isBrokerConnected.value = true
        brokerPrefs.edit()
            .putString("broker_api_key", apiKey.trim())
            .putString("broker_api_secret", apiSecret.trim())
            .putBoolean("is_broker_connected", true)
            .apply()
        return true
    }

    fun disconnectBroker() {
        _brokerApiKey.value = ""
        _brokerApiSecret.value = ""
        _isBrokerConnected.value = false
        brokerPrefs.edit()
            .remove("broker_api_key")
            .remove("broker_api_secret")
            .putBoolean("is_broker_connected", false)
            .apply()
    }

    // --- Voice Assistant State ---
    private val _voiceTranscript = MutableStateFlow<String?>(null)
    val voiceTranscript: StateFlow<String?> = _voiceTranscript.asStateFlow()

    private val _isListening = MutableStateFlow(false)
    val isListening: StateFlow<Boolean> = _isListening.asStateFlow()

    // --- Flows from DB ---
    val watchlistItems: StateFlow<List<WatchlistEntity>> = repository.allWatchlist
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val positions: StateFlow<List<PositionEntity>> = repository.allPositions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val alerts: StateFlow<List<AlertEntity>> = repository.allAlerts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Combined Live Scanning Feed ---
    val scanningStocks: StateFlow<List<MarketStock>> = combine(
        _liveStockMap,
        _searchQuery,
        _selectedSector,
        _selectedPattern
    ) { liveMap, query, sector, pattern ->
        liveMap.values.filter { stock ->
            val matchesQuery = stock.symbol.contains(query, ignoreCase = true) || stock.name.contains(query, ignoreCase = true)
            val matchesSector = sector == "All" || stock.sector.equals(sector, ignoreCase = true)
            val matchesPattern = pattern == "All" || stock.breakoutPattern.equals(pattern, ignoreCase = true)
            matchesQuery && matchesSector && matchesPattern
        }.sortedByDescending { it.confidence }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), MarketRepository.stocks)

    // --- Watchlist Stocks Details ---
    val watchlistStocks: StateFlow<List<MarketStock>> = combine(
        _liveStockMap,
        watchlistItems
    ) { liveMap, items ->
        val itemSymbols = items.map { it.symbol.uppercase() }.toSet()
        liveMap.values.filter { stock -> stock.symbol.uppercase() in itemSymbols }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- EMA Crossover Stocks (Refreshes every 30 seconds with high volume) ---
    private val _emaCrossoverStocks = MutableStateFlow<List<MarketStock>>(emptyList())
    val emaCrossoverStocks: StateFlow<List<MarketStock>> = _emaCrossoverStocks.asStateFlow()

    private val _emaCountdown = MutableStateFlow(30)
    val emaCountdown: StateFlow<Int> = _emaCountdown.asStateFlow()

    // --- Live Portfolio Valuation ---
    data class PortfolioSummary(
        val totalInvestment: Double = 0.0,
        val totalCurrent: Double = 0.0,
        val todaysProfitLoss: Double = 0.0,
        val totalPNLPercent: Double = 0.0
    )

    val portfolioSummary: StateFlow<PortfolioSummary> = combine(
        positions,
        _liveStockMap
    ) { livePositions, liveMap ->
        if (livePositions.isEmpty()) return@combine PortfolioSummary()

        var totalInv = 0.0
        var totalCur = 0.0
        var totalPrevClose = 0.0 // To calculate today's profit/loss changes

        for (pos in livePositions) {
            val liveStock = liveMap[pos.symbol]
            val livePrice = liveStock?.price ?: pos.currentPrice
            val qty = pos.quantity
            totalInv += pos.buyPrice * qty
            totalCur += livePrice * qty
            
            // Assume previous close was today's price minus change
            val pctChange = liveStock?.changePercent ?: 0.0
            val valueDiff = livePrice - (livePrice / (1 + (pctChange / 100.0)))
            totalPrevClose += valueDiff * qty
        }

        val pnlPercent = if (totalInv > 0) ((totalCur - totalInv) / totalInv) * 100.0 else 0.0

        PortfolioSummary(
            totalInvestment = totalInv,
            totalCurrent = totalCur,
            todaysProfitLoss = totalPrevClose,
            totalPNLPercent = pnlPercent
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), PortfolioSummary())

    init {
        // Start live mock market pricing updates
        startLivePriceSimulation()
        startEmaCrossoverSimulation()
    }

    private fun startEmaCrossoverSimulation() {
        viewModelScope.launch(Dispatchers.Default) {
            while (true) {
                val currentStocks = _liveStockMap.value.values.toList()
                if (currentStocks.isNotEmpty()) {
                    // Filter or transform some stocks to represent active crossovers
                    // We randomly pick 2 to 4 stocks and simulate high volume (e.g. Volume Multiplier > 2.0)
                    // and active crossover states
                    val crossoverList = currentStocks.shuffled().take(Random.nextInt(2, 5)).map { stock ->
                        val highVol = Math.round(Random.nextDouble(2.2, 5.5) * 10) / 10.0
                        stock.copy(
                            volumeMultiplier = highVol,
                            breakoutPattern = "EMA 10/20 Crossover"
                        )
                    }
                    _emaCrossoverStocks.value = crossoverList
                }

                // Countdown for 30 seconds
                for (seconds in 30 downTo 1) {
                    _emaCountdown.value = seconds
                    delay(1000)
                }
            }
        }
    }

    private fun startLivePriceSimulation() {
        viewModelScope.launch(Dispatchers.Default) {
            while (true) {
                delay(4000) // update every 4 seconds
                val currentMap = _liveStockMap.value.toMutableMap()
                
                for ((symbol, stock) in currentMap) {
                    // Small fluctuation between -0.15% & +0.18%
                    val changeDelta = Random.nextDouble(-0.15, 0.18)
                    val newPrice = stock.price * (1 + (changeDelta / 100))
                    val newChangePercent = stock.changePercent + changeDelta
                    
                    currentMap[symbol] = stock.copy(
                        price = Math.round(newPrice * 100.0) / 100.0,
                        changePercent = Math.round(newChangePercent * 100.0) / 100.0
                    )
                }
                _liveStockMap.value = currentMap

                // Update selected stock price if it matches
                val currentSelected = _selectedStock.value
                currentMap[currentSelected.symbol]?.let { updated ->
                    _selectedStock.value = updated
                }

                // Check and trigger simulated alerts asynchronously
                checkAlertTriggerPivots(currentMap)
            }
        }
    }

    private fun checkAlertTriggerPivots(currentMap: Map<String, MarketStock>) {
        viewModelScope.launch(Dispatchers.IO) {
            val activeAlerts = alerts.value.filter { !it.isTriggered }
            for (alert in activeAlerts) {
                val stock = currentMap[alert.symbol] ?: continue
                val triggered = if (alert.direction == "ABOVE") {
                    stock.price >= alert.priceTrigger
                } else {
                    stock.price <= alert.priceTrigger
                }

                if (triggered) {
                    repository.markAlertTriggered(alert.id)
                }
            }
        }
    }

    // --- Actions ---

    fun selectStock(stock: MarketStock) {
        _selectedStock.value = stock
        _aiAnalysis.value = AiAnalysisState.Idle // Reset analysis report
    }

    fun toggleHindi() {
        _isHindi.value = !_isHindi.value
    }

    fun togglePremium() {
        _isPremium.value = !_isPremium.value
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setSectorFilter(sector: String) {
        _selectedSector.value = sector
    }

    fun setPatternFilter(pattern: String) {
        _selectedPattern.value = pattern
    }

    fun toggleWatchlist(symbol: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val isWatch = watchlistItems.value.any { it.symbol.equals(symbol, ignoreCase = true) }
            if (isWatch) {
                repository.deleteWatchlist(symbol)
            } else {
                repository.insertWatchlist(symbol)
            }
        }
    }

    fun openAlertForm(symbol: String) {
        _alertFormState.value = symbol
    }

    fun closeAlertForm() {
        _alertFormState.value = null
    }

    fun addAlert(symbol: String, stockName: String, triggerPrice: Double, direction: String, type: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.insertAlert(
                AlertEntity(
                    symbol = symbol.uppercase(),
                    stockName = stockName,
                    priceTrigger = triggerPrice,
                    direction = direction,
                    type = type
                )
            )
            _alertFormState.value = null
        }
    }

    fun deleteAlert(id: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteAlert(id)
        }
    }

    fun openBrokerTrade(stock: MarketStock) {
        _brokerDialogState.value = stock
    }

    fun closeBrokerTrade() {
        _brokerDialogState.value = null
    }

    fun executeBrokerTrade(symbol: String, stockName: String, buyPrice: Double, qty: Int, broker: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val existingPositions = positions.value
            val match = existingPositions.firstOrNull { it.symbol.uppercase() == symbol.uppercase() }

            if (match != null) {
                // Average price logic
                val totalQty = match.quantity + qty
                val avgPrice = ((match.buyPrice * match.quantity) + (buyPrice * qty)) / totalQty
                repository.insertPosition(
                    match.copy(
                        quantity = totalQty,
                        buyPrice = Math.round(avgPrice * 100.0) / 100.0,
                        currentPrice = buyPrice,
                        timestamp = System.currentTimeMillis()
                    )
                )
            } else {
                repository.insertPosition(
                    PositionEntity(
                        symbol = symbol.uppercase(),
                        stockName = stockName,
                        buyPrice = buyPrice,
                        quantity = qty,
                        currentPrice = buyPrice,
                        broker = broker
                    )
                )
            }
            _brokerDialogState.value = null
        }
    }

    fun liquidatePosition(symbol: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deletePosition(symbol)
        }
    }

    fun clearAllPositions() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.clearPositions()
        }
    }

    // --- Gemini Interactive Analysis Calls ---
    fun requestAiAnalysis(stock: MarketStock) {
        viewModelScope.launch(Dispatchers.IO) {
            _aiAnalysis.value = AiAnalysisState.Loading
            
            val report = GeminiApi.analyzeStock(
                stockSymbol = stock.symbol,
                stockName = stock.name,
                price = stock.price,
                pattern = stock.breakoutPattern,
                stopLoss = stock.stopLoss,
                targets = stock.targets,
                volumeMult = stock.volumeMultiplier,
                rsi = stock.rsi,
                support = stock.support,
                resistance = stock.resistance,
                deliveryPct = stock.deliveryPercentage
            )

            _aiAnalysis.value = AiAnalysisState.Success(report)
        }
    }

    // --- AI Voice Assistant Support (Simulated NLP matching) ---
    fun runVoiceCommand(prompt: String) {
        _voiceTranscript.value = prompt
        val clean = prompt.trim().lowercase()

        viewModelScope.launch(Dispatchers.IO) {
            _isListening.value = true
            delay(1500) // Simulated processing lag
            _isListening.value = false

            // Try to match standard commands
            when {
                "tata" in clean || "motors" in clean -> selectStockByName("TATAMOTORS")
                "reliance" in clean -> selectStockByName("RELIANCE")
                "hdfc" in clean || "bank" in clean -> selectStockByName("HDFCBANK")
                "infosys" in clean || "infy" in clean -> selectStockByName("INFY")
                "sbi" in clean || "state" in clean -> selectStockByName("SBIN")
                "adani" in clean || "ports" in clean -> selectStockByName("ADANIPORTS")
                "steel" in clean -> selectStockByName("TATASTEEL")
                "irfc" in clean -> selectStockByName("IRFC")
                "hindi" in clean -> toggleHindi()
                "premium" in clean || "unlock" in clean -> togglePremium()
                "clear" in clean || "liquidate" in clean -> clearAllPositions()
                else -> {
                    // Let's do an AI trigger summary response or message
                    _voiceTranscript.value = "Command recognized. Searching Indian breakout scanners."
                }
            }
        }
    }

    private fun selectStockByName(symbol: String) {
        _liveStockMap.value[symbol]?.let {
            selectStock(it)
        }
    }

    fun resetVoice() {
        _voiceTranscript.value = null
    }
}

// --- Factory Provider so we can construct with context & DB ---

class TradingViewModelFactory(
    private val application: Application,
    private val repository: TradingRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TradingViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TradingViewModel(application, repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
