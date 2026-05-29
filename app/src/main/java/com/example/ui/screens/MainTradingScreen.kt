package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.automirrored.outlined.HelpOutline
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.ui.graphics.drawscope.Stroke
import com.example.data.local.AlertEntity
import com.example.data.local.PositionEntity
import com.example.data.model.MarketRepository
import com.example.data.model.MarketStock
import com.example.ui.AiAnalysisState
import com.example.ui.TradingViewModel
import com.example.ui.components.StockChart

enum class TradingTab {
    DASHBOARD,
    ANALYSIS,
    PORTFOLIO,
    ALERTS,
    PREMIUM
}

enum class DashboardCategory {
    BREAKOUTS,
    WATCHLIST,
    EMA_CROSSOVER
}

// Global dictionary for Hindi + English support
object Localization {
    fun translate(key: String, isHindi: Boolean): String {
        val dict = mapOf(
            "ema_title" to Pair("EMA 10/20 Crossover", "EMA 10/20 क्रॉसओवर"),
            "ema_refresh" to Pair("Next scan in: ", "अगला स्कैन: "),
            "sec_unit" to Pair("s", "सेकंड"),
            "high_volume_only" to Pair("High Volume Crossover Only", "केवल हाई वॉल्यूम क्रॉसओवर"),
            "app_name" to Pair("BreakoutScanner AI", "ब्रेकआउटस्कैनर AI"),
            "disclaimer" to Pair(
                "DISCLAIMER: This app is for educational and research purposes only. Trading involves market risk. No strategy guarantees returns.",
                "डिस्क्लेमर: यह ऐप केवल शैक्षिक और अनुसंधान उद्देश्यों के लिए है। ट्रेडिंग में बाजार जोखिम शामिल है। कोई भी रणनीति लाभ की गारंटी नहीं देती है।"
            ),
            "scanner_title" to Pair("Scan Breakouts", "ब्रेकआउट स्कैन करें"),
            "scanner_subtitle" to Pair("Indian Equities (NSE/BSE) Scanner", "भारतीय शेयर (NSE/BSE) स्कैनर"),
            "search_hint" to Pair("Search stock by symbol or name...", "सर्च शेयर सिंबल या नाम..."),
            "all_sectors" to Pair("All Sectors", "सभी सेक्टर्स"),
            "all_patterns" to Pair("All Patterns", "सभी पैटर्न्स"),
            "confidence" to Pair("Confidence", "विश्वास"),
            "watchlist" to Pair("Watchlist", "वॉचलिस्ट"),
            "portfolio" to Pair("Portfolio", "पोर्टफोलियो"),
            "alerts" to Pair("Alerts", "अलर्ट्स"),
            "ai_pro" to Pair("AI Pro Tools", "AI प्रो टूल्स"),
            "buy_signal" to Pair("BUY SETUP", "बाय सेँटअप"),
            "volume_mult" to Pair("Volume Spike", "वॉल्यूम स्पाइक"),
            "unusual_volume" to Pair("Unusual Volume", "असामान्य वॉल्यूम"),
            "buy" to Pair("Buy / Trade", "खरीदें / ट्रेड करें"),
            "stop_loss" to Pair("Stop Loss", "स्टॉप लॉस"),
            "target" to Pair("Targets", "लक्ष्य (Targets)"),
            "multi_timeframe" to Pair("Multi-Timeframe Trend Match", "मल्टी-टाइमफ्रेम ट्रेंड मिलान"),
            "generate_ai" to Pair("Scan with Breakout AI", "ट्रेडिंग AI से स्कैन करें"),
            "fake_breakout_detection" to Pair("Fake Breakout Score", "फेक ब्रेकआउट स्कोर"),
            "broker_integration" to Pair("Direct Broker Terminal", "डायरेक्ट ब्रोकर टर्मिनल"),
            "connected_broker" to Pair("Connected Broker", "संबद्ध ब्रोकर"),
            "daily_pnl" to Pair("Today's Profit & Loss", "आज का लाभ और हानि"),
            "total_return" to Pair("Total Return", "कुल रिटर्न"),
            "active_positions" to Pair("Active Execution Positions", "सक्रिय ट्रेड पोजीशन्स"),
            "add_alert" to Pair("Create Signal Breakout Alert", "ब्रेकआउट अलर्ट बनाएं"),
            "alert_delivered" to Pair("Delivered Logs", "भेजे गए अलर्ट लॉग"),
            "premium_unlock_title" to Pair("Unlock Indian Market Alpha", "भारतीय बाजार अल्फा अनलॉक करें"),
            "premium_unlock_sub" to Pair("Advanced ML scanners, LSTM forecaster & auto-backtesting", "उन्नत कृत्रिम बुद्धिमत्ता और गतिशीलता"),
            "voice_assistant" to Pair("AI Trading Assistant - Voice Input", "AI ट्रेडिंग सहायक - आवाज इनपुट"),
            "listen_hint" to Pair("Say: 'Select Reliance' or 'Show Tata Motors'", "बोलें: 'Select Reliance' या 'Show Tata Motors'"),
            "manipulation_check" to Pair("Operator Block Scanning", "ऑपरेटर गतिविधि स्कैनिंग"),
            "relative_strength" to Pair("Strength vs NIFTY", "निफ्टी के मुकाबले मजबूती")
        )
        val pair = dict[key] ?: return key
        return if (isHindi) pair.second else pair.first
    }
}

@Composable
fun MainTradingScreen(
    viewModel: TradingViewModel,
    modifier: Modifier = Modifier
) {
    val isLoggedIn by viewModel.isLoggedIn.collectAsState()
    val isHindi by viewModel.isHindi.collectAsState()
    val isPremium by viewModel.isPremium.collectAsState()
    val selectedStock by viewModel.selectedStock.collectAsState()
    val alertFormState by viewModel.alertFormState.collectAsState()
    val brokerDialogState by viewModel.brokerDialogState.collectAsState()

    var activeTab by remember { mutableStateOf(TradingTab.DASHBOARD) }

    if (!isLoggedIn) {
        AuthScreen(
            viewModel = viewModel,
            isHindi = isHindi,
            modifier = modifier
        )
    } else {
        Scaffold(
            modifier = modifier
                .testTag("main_screen")
                .fillMaxSize(),
        bottomBar = {
            Column(modifier = Modifier.background(Color.White)) {
                // Persistent dynamic disclaimer in premium white format
                DisclaimerRow(isHindi = isHindi)
                
                NavigationBar(
                    containerColor = Color.White,
                    tonalElevation = 8.dp,
                    windowInsets = WindowInsets.navigationBars
                ) {
                    NavigationBarItem(
                        icon = { Icon(Icons.Filled.Radar, contentDescription = "Dashboard") },
                        label = { Text(Localization.translate("scanner_title", isHindi), fontSize = 10.sp, overflow = TextOverflow.Ellipsis) },
                        selected = activeTab == TradingTab.DASHBOARD,
                        onClick = { activeTab = TradingTab.DASHBOARD }
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Filled.ShowChart, contentDescription = "Analysis") },
                        label = { Text("AI Chart", fontSize = 10.sp) },
                        selected = activeTab == TradingTab.ANALYSIS,
                        onClick = { activeTab = TradingTab.ANALYSIS }
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Filled.AccountBalanceWallet, contentDescription = "Portfolio") },
                        label = { Text(Localization.translate("portfolio", isHindi), fontSize = 10.sp) },
                        selected = activeTab == TradingTab.PORTFOLIO,
                        onClick = { activeTab = TradingTab.PORTFOLIO }
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Filled.NotificationsActive, contentDescription = "Alerts") },
                        label = { Text(Localization.translate("alerts", isHindi), fontSize = 10.sp) },
                        selected = activeTab == TradingTab.ALERTS,
                        onClick = { activeTab = TradingTab.ALERTS }
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Filled.AutoAwesome, contentDescription = "Premium") },
                        label = { Text("Premium", fontSize = 10.sp) },
                        selected = activeTab == TradingTab.PREMIUM,
                        onClick = { activeTab = TradingTab.PREMIUM }
                    )
                }
            }
        },
        topBar = {
            TradingTopBar(
                viewModel = viewModel,
                isHindi = isHindi,
                isPremium = isPremium
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF8F9FA))
                .padding(innerPadding)
        ) {
            // Main content based on active tab
            Crossfade(targetState = activeTab, label = "TabTransition") { tab ->
                when (tab) {
                    TradingTab.DASHBOARD -> DashboardScannerTab(viewModel = viewModel, isHindi = isHindi, onStockSelect = {
                        viewModel.selectStock(it)
                        activeTab = TradingTab.ANALYSIS
                    })
                    TradingTab.ANALYSIS -> ChartAnalysisTab(viewModel = viewModel, isHindi = isHindi, isPremium = isPremium, stock = selectedStock)
                    TradingTab.PORTFOLIO -> PortfolioTab(viewModel = viewModel, isHindi = isHindi)
                    TradingTab.ALERTS -> AlertsTab(viewModel = viewModel, isHindi = isHindi)
                    TradingTab.PREMIUM -> PremiumProTab(viewModel = viewModel, isHindi = isHindi, isPremium = isPremium)
                }
            }

            // Global Broker direct buy order dialog
            brokerDialogState?.let { stock ->
                BrokerDirectBuyDialog(
                    stock = stock,
                    viewModel = viewModel,
                    onDismiss = { viewModel.closeBrokerTrade() },
                    onConfirm = { qty, broker ->
                        viewModel.executeBrokerTrade(
                            symbol = stock.symbol,
                            stockName = stock.name,
                            buyPrice = stock.price,
                            qty = qty,
                            broker = broker
                        )
                    }
                )
            }

            // Global stock alert creator pop-up
            alertFormState?.let { sym ->
                val matchingStock = MarketRepository.getStockBySymbol(sym)
                if (matchingStock != null) {
                    CreateAlertDialog(
                        stock = matchingStock,
                        onDismiss = { viewModel.closeAlertForm() },
                        onConfirm = { trigger, dir, type ->
                            viewModel.addAlert(
                                symbol = matchingStock.symbol,
                                stockName = matchingStock.name,
                                triggerPrice = trigger,
                                direction = dir,
                                type = type
                            )
                        }
                    )
                }
            }
        }
    }
}
}

// --- App Toolbar Header ---

@Composable
fun TradingTopBar(
    viewModel: TradingViewModel,
    isHindi: Boolean,
    isPremium: Boolean
) {
    Surface(
        color = Color.White,
        tonalElevation = 2.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .statusBarsPadding()
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Filled.TrendingUp,
                        contentDescription = "BreakoutScanner Logo",
                        tint = Color(0xFF1E88E5),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "BreakoutScanner AI",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF1A1D20),
                        fontFamily = FontFamily.SansSerif
                    )
                }
                Text(
                    text = if (isHindi) "भारतीय शेयर बाज़ार" else "Indian Equities (NSE/BSE)",
                    fontSize = 11.sp,
                    color = Color.Gray,
                    fontWeight = FontWeight.Medium
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Premium marker badge
                if (isPremium) {
                    Text(
                        text = "PRO PASS",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier
                            .background(
                                brush = Brush.horizontalGradient(listOf(Color(0xFF8E24AA), Color(0xFFE91E63))),
                                shape = RoundedCornerShape(12.dp)
                            )
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }

                // Bilingual Toggle Switch
                IconButton(
                    onClick = { viewModel.toggleHindi() },
                    modifier = Modifier
                        .size(36.dp)
                        .background(Color(0xFFF1F3F5), shape = CircleShape)
                ) {
                    Text(
                        text = if (isHindi) "EN" else "हिन",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E88E5)
                    )
                }

                // Logout button
                IconButton(
                    onClick = { viewModel.logout() },
                    modifier = Modifier
                        .size(36.dp)
                        .background(Color(0xFFFFEBEE), shape = CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Filled.ExitToApp,
                        contentDescription = "Logout",
                        tint = Color(0xFFC62828),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

// --- SEC & Exchange Regulation Warnings ---

@Composable
fun DisclaimerRow(isHindi: Boolean) {
    Surface(
        color = Color(0xFFFFF9C4), // Light Warning Yellow block
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                Icons.Outlined.Warning,
                contentDescription = "Disclaimer Warning",
                tint = Color(0xFFF57F17),
                modifier = Modifier.size(13.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = Localization.translate("disclaimer", isHindi),
                fontSize = 9.sp,
                color = Color(0xFF5D4037),
                lineHeight = 11.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center
            )
        }
    }
}

// --- Tab 1: Live Scanning Feed Page ---

@Composable
fun DashboardScannerTab(
    viewModel: TradingViewModel,
    isHindi: Boolean,
    onStockSelect: (MarketStock) -> Unit
) {
    var selectedSubTab by remember { mutableStateOf(DashboardCategory.BREAKOUTS) }
    
    val scanningStocks by viewModel.scanningStocks.collectAsState()
    val watchlistStocks by viewModel.watchlistStocks.collectAsState()
    val watchlistItems by viewModel.watchlistItems.collectAsState()
    val emaCrossoverStocks by viewModel.emaCrossoverStocks.collectAsState()
    val emaCountdown by viewModel.emaCountdown.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedSector by viewModel.selectedSector.collectAsState()
    val selectedPattern by viewModel.selectedPattern.collectAsState()
    val summary by viewModel.portfolioSummary.collectAsState()

    val sectors = listOf("All", "Automobile", "Energy", "IT", "Banking", "Metals", "Infrastructure", "Finance")
    val patterns = listOf("All", "Cup and Handle", "Triangle Breakout", "Flag Pattern", "Rectangle Breakout", "VWAP Bounce", "Inverse Head & Shoulder", "EMA Pullback", "Opening Range Breakout")

    val displayStocks = when (selectedSubTab) {
        DashboardCategory.BREAKOUTS -> scanningStocks
        DashboardCategory.WATCHLIST -> watchlistStocks.filter { stock ->
            stock.symbol.contains(searchQuery, ignoreCase = true) || stock.name.contains(searchQuery, ignoreCase = true)
        }
        DashboardCategory.EMA_CROSSOVER -> emaCrossoverStocks.filter { stock ->
            stock.symbol.contains(searchQuery, ignoreCase = true) || stock.name.contains(searchQuery, ignoreCase = true)
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { Spacer(modifier = Modifier.height(12.dp)) }

        // Dashboard Tab Selector: All Breakouts vs Watchlist vs EMA Crossover
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFEFEFF0), shape = RoundedCornerShape(12.dp))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Tab 1: Breakout Scanners
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .background(
                            color = if (selectedSubTab == DashboardCategory.BREAKOUTS) Color.White else Color.Transparent,
                            shape = RoundedCornerShape(8.dp)
                        )
                        .clickable { selectedSubTab = DashboardCategory.BREAKOUTS }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.Radar,
                            contentDescription = "Radar",
                            modifier = Modifier.size(14.dp),
                            tint = if (selectedSubTab == DashboardCategory.BREAKOUTS) Color(0xFF1E88E5) else Color.Gray
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isHindi) "ब्रेकआउट" else "Breakouts",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (selectedSubTab == DashboardCategory.BREAKOUTS) Color.Black else Color.Gray,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // Tab 2: My Watchlist
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .background(
                            color = if (selectedSubTab == DashboardCategory.WATCHLIST) Color.White else Color.Transparent,
                            shape = RoundedCornerShape(8.dp)
                        )
                        .clickable { selectedSubTab = DashboardCategory.WATCHLIST }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.Star,
                            contentDescription = "Watchlist",
                            modifier = Modifier.size(14.dp),
                            tint = if (selectedSubTab == DashboardCategory.WATCHLIST) Color(0xFFFFB300) else Color.Gray
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isHindi) "वॉचलिस्ट" else "Watchlist",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (selectedSubTab == DashboardCategory.WATCHLIST) Color.Black else Color.Gray,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // Tab 3: EMA Crossover
                Box(
                    modifier = Modifier
                        .weight(1.2f)
                        .background(
                            color = if (selectedSubTab == DashboardCategory.EMA_CROSSOVER) Color.White else Color.Transparent,
                            shape = RoundedCornerShape(8.dp)
                        )
                        .clickable { selectedSubTab = DashboardCategory.EMA_CROSSOVER }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.AvTimer,
                            contentDescription = "EMA Crossover",
                            modifier = Modifier.size(14.dp),
                            tint = if (selectedSubTab == DashboardCategory.EMA_CROSSOVER) Color(0xFF2E7D32) else Color.Gray
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isHindi) "EMA क्रॉस ओवर" else "EMA Crossover",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (selectedSubTab == DashboardCategory.EMA_CROSSOVER) Color.Black else Color.Gray,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }

        // Live EMA Crossover Ticking countdown panel
        if (selectedSubTab == DashboardCategory.EMA_CROSSOVER) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .padding(12.dp)
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.TrendingUp,
                                contentDescription = "EMA Cross",
                                tint = Color(0xFF2E7D32),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = Localization.translate("ema_title", isHindi),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1B5E20)
                                )
                                Text(
                                    text = Localization.translate("high_volume_only", isHindi),
                                    fontSize = 10.sp,
                                    color = Color(0xFF2E7D32)
                                )
                            }
                        }
                        
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = Localization.translate("ema_refresh", isHindi),
                                fontSize = 11.sp,
                                color = Color.DarkGray
                            )
                            Box(
                                modifier = Modifier
                                    .background(Color(0xFF2E7D32), RoundedCornerShape(6.dp))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "$emaCountdown ${Localization.translate("sec_unit", isHindi)}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }
        }

        // Core Aggregated Confidence Meter
        item {
            ConfidenceMeterCard(isHindi = isHindi, rating = 88)
        }

        // Live Market NIFTY / SENSEX Indices block
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                IndexTickerCard(
                    name = "NIFTY 50",
                    value = "22,945.10",
                    change = "+182.40 (+0.80%)",
                    isUp = true,
                    modifier = Modifier.weight(1f)
                )
                IndexTickerCard(
                    name = "SENSEX",
                    value = "75,418.00",
                    change = "+580.12 (+0.78%)",
                    isUp = true,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Search Bar (White Glass panel)
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.setSearchQuery(it) },
                placeholder = { Text(Localization.translate("search_hint", isHindi), fontSize = 13.sp) },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = "Search", tint = Color.Gray) },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedBorderColor = Color(0xFF1E88E5),
                    unfocusedBorderColor = Color(0xFFE0E0E0),
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("search_input")
            )
        }

        // Sector selector Chips
        if (selectedSubTab == DashboardCategory.BREAKOUTS) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = Localization.translate("all_sectors", isHindi),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.DarkGray
                    )
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(sectors) { sector ->
                            val isSelected = selectedSector == sector
                            Text(
                                text = sector,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = if (isSelected) Color.White else Color.DarkGray,
                                modifier = Modifier
                                    .background(
                                        color = if (isSelected) Color(0xFF1E88E5) else Color.White,
                                        shape = RoundedCornerShape(16.dp)
                                    )
                                    .border(1.dp, if (isSelected) Color.Transparent else Color(0xFFE0E0E0), RoundedCornerShape(16.dp))
                                    .clickable { viewModel.setSectorFilter(sector) }
                                    .padding(horizontal = 14.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }

            // Patterns selector Chips
            item {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = Localization.translate("all_patterns", isHindi),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.DarkGray
                    )
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(patterns) { pattern ->
                            val isSelected = selectedPattern == pattern
                            Text(
                                text = pattern,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = if (isSelected) Color.White else Color.DarkGray,
                                modifier = Modifier
                                    .background(
                                        color = if (isSelected) Color(0xFF4CAF50) else Color.White,
                                        shape = RoundedCornerShape(16.dp)
                                    )
                                    .border(1.dp, if (isSelected) Color.Transparent else Color(0xFFE0E0E0), RoundedCornerShape(16.dp))
                                    .clickable { viewModel.setPatternFilter(pattern) }
                                    .padding(horizontal = 14.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }
        }

        // Live Scanning results header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = when (selectedSubTab) {
                        DashboardCategory.WATCHLIST -> {
                            if (isHindi) "मेरी पसंदीदा वॉचलिस्ट" else "Watchlisted Stocks"
                        }
                        DashboardCategory.EMA_CROSSOVER -> {
                            if (isHindi) "EMA क्रॉस ओवर शेयर्स" else "EMA Crossover Stocks"
                        }
                        else -> {
                            if (isHindi) "उच्च विश्वसनीयता ब्रेकआउट्स" else "High Probability Setups"
                        }
                    },
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A1D20)
                )
                Text(
                    text = when (selectedSubTab) {
                        DashboardCategory.WATCHLIST -> "${displayStocks.size} watched"
                        DashboardCategory.EMA_CROSSOVER -> "${displayStocks.size} crossovers found"
                        else -> "${displayStocks.size} stocks found"
                    },
                    fontSize = 11.sp,
                    color = Color.Gray,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        // Table Rows detailing High Probability Setups / Watchlist Setups / EMA Crossover Setups
        if (displayStocks.isEmpty()) {
            item {
                when (selectedSubTab) {
                    DashboardCategory.WATCHLIST -> {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.StarRate,
                                    contentDescription = "Watchlist empty icon",
                                    tint = Color(0xFFFFA000),
                                    modifier = Modifier.size(44.dp)
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = if (isHindi) "आपकी वॉचलिस्ट खाली है!\nकिसी भी स्टॉक पर जाकर ⭐️ दबाएं।" else "Your Watchlist is empty!\nTap the ⭐️ icon on any stock's chart analysis page to add them here.",
                                    fontSize = 12.sp,
                                    color = Color.Gray,
                                    fontWeight = FontWeight.Medium,
                                    textAlign = TextAlign.Center,
                                    lineHeight = 16.sp
                                )
                            }
                        }
                    }
                    DashboardCategory.EMA_CROSSOVER -> {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                CircularProgressIndicator(
                                    color = Color(0xFF2E7D32),
                                    modifier = Modifier.size(36.dp)
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = if (isHindi) "EMA क्रॉसओवर स्कैन किया जा रहा है..." else "Scanning for high volume EMA crossovers...",
                                    fontSize = 12.sp,
                                    color = Color.DarkGray,
                                    fontWeight = FontWeight.SemiBold,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                    else -> {
                        EmptySearchBlock(isHindi = isHindi)
                    }
                }
            }
        } else {
            items(displayStocks) { stock ->
                val isWatchlisted = watchlistItems.any { it.symbol == stock.symbol }
                StockBreakoutSetupCard(
                    stock = stock,
                    isHindi = isHindi,
                    isWatchlisted = isWatchlisted,
                    onClick = { onStockSelect(stock) },
                    onBuyClick = { viewModel.openBrokerTrade(stock) },
                    onAlertClick = { viewModel.openAlertForm(stock.symbol) },
                    onWatchlistToggle = { viewModel.toggleWatchlist(stock.symbol) }
                )
            }
        }

        item { Spacer(modifier = Modifier.height(24.dp)) }
    }
}

// --- Tab 2: Chart & Detail Analysis Node ---

@Composable
fun ChartAnalysisTab(
    viewModel: TradingViewModel,
    isHindi: Boolean,
    isPremium: Boolean,
    stock: MarketStock
) {
    val aiAnalysisState by viewModel.aiAnalysis.collectAsState()
    val watchlistItems by viewModel.watchlistItems.collectAsState()
    val isWatchlisted = watchlistItems.any { it.symbol == stock.symbol }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Dynamic Stock header
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = stock.symbol,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF1A1D20)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isHindi) "बाय सिग्नल" else "BUY SETUP",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2E7D32),
                            modifier = Modifier
                                .background(Color(0xFFE8F5E9), shape = RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    Text(text = stock.name, fontSize = 12.sp, color = Color.Gray)
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Rs. ${stock.price}",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (stock.changePercent >= 0) Color(0xFF2E7D32) else Color(0xFFC62828)
                    )
                    Text(
                        text = "${if (stock.changePercent >= 0) "+" else ""}${stock.changePercent}%",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (stock.changePercent >= 0) Color(0xFF4CAF50) else Color(0xFFFF5252)
                    )
                }
            }
        }

        // TradingView-Style Interactive Chart Box
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, Color(0xFFEEEEEE)),
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp)
        ) {
            StockChart(stock = stock, modifier = Modifier.fillMaxSize())
        }

        // Action Toolbar (Buy / Watchlist / Set Alerts)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Button(
                onClick = { viewModel.openBrokerTrade(stock) },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Filled.AccountBalance, contentDescription = "Trade")
                Spacer(modifier = Modifier.width(6.dp))
                Text(Localization.translate("buy", isHindi), fontWeight = FontWeight.Bold)
            }

            OutlinedButton(
                onClick = { viewModel.toggleWatchlist(stock.symbol) },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.DarkGray)
            ) {
                Icon(
                    imageVector = if (isWatchlisted) Icons.Filled.Star else Icons.Outlined.StarBorder,
                    contentDescription = "Watchlist toggle",
                    tint = if (isWatchlisted) Color(0xFFFFB300) else Color.DarkGray
                )
            }

            OutlinedButton(
                onClick = { viewModel.openAlertForm(stock.symbol) },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF1E88E5))
            ) {
                Icon(Icons.Filled.AddAlert, contentDescription = "Set Alert")
            }
        }

        // External Research Gateways (TradingView Chart & Screener Analysis)
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = if (isHindi) "बाहरी रिसर्च टूल्स (Research Tools)" else "External Analysis & Interactive Tools",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.DarkGray
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    val context = LocalContext.current
                    
                    OutlinedButton(
                        onClick = {
                            val intent = android.content.Intent(
                                android.content.Intent.ACTION_VIEW,
                                android.net.Uri.parse("https://in.tradingview.com/chart/?symbol=NSE:${stock.symbol}")
                            )
                            context.startActivity(intent)
                        },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF1E88E5))
                    ) {
                        Icon(
                            imageVector = Icons.Filled.ShowChart,
                            contentDescription = "TradingView Icon",
                            tint = Color(0xFF1E88E5),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isHindi) "ट्रेडिंगव्यू चार्ट" else "TradingView ↗",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    OutlinedButton(
                        onClick = {
                            val intent = android.content.Intent(
                                android.content.Intent.ACTION_VIEW,
                                android.net.Uri.parse("https://www.screener.in/company/${stock.symbol}/")
                            )
                            context.startActivity(intent)
                        },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF4CAF50))
                    ) {
                        Icon(
                            imageVector = Icons.Filled.BarChart,
                            contentDescription = "Screener Icon",
                            tint = Color(0xFF4CAF50),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isHindi) "स्क्रीनर विश्लेषण" else "Screener.in ↗",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Technical Analysis Indicators Block (Criteria alignment)
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Engine Audit Logs & Indicators Alignment",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.DarkGray
                )
                
                Divider(color = Color(0xFFEEEEEE))

                TechnicalRow(label = "Trend Direction (EMA 20 > 50 > 200)", matches = true, value = "EMA20: ${stock.ema20} | EMA50: ${stock.ema50} | EMA200: ${stock.ema200}")
                TechnicalRow(label = "Breakout Zone Confirmation", matches = true, value = "Breached Resistance (Rs. ${stock.resistance})")
                TechnicalRow(label = "Volume Multiplier (>2x Average)", matches = stock.volumeMultiplier >= 2.0, value = "${stock.volumeMultiplier}x volume spike")
                TechnicalRow(label = "RSI Momentum Range (60-75)", matches = stock.rsi in 60.0..75.0, value = "RSI index is ${stock.rsi}")
                TechnicalRow(label = "MACD Bullish Crossover", matches = stock.macdCrossover, value = if (stock.macdCrossover) "Bullish Cross Confirmed" else "Bearish")
                TechnicalRow(label = "Relative Strength (vs NIFTY 50)", matches = stock.relativeStrengthNifty == "Strong" || stock.relativeStrengthNifty == "Very Strong", value = stock.relativeStrengthNifty)
                TechnicalRow(label = "Smart Money Accumulation (Delivery %)", matches = stock.deliveryPercentage >= 50, value = "${stock.deliveryPercentage}% Delivery percentage")
                TechnicalRow(label = "Institutional Operator volume spike", matches = stock.institutionalVolumeSpike, value = if (stock.institutionalVolumeSpike) "Spike detected" else "None")
                TechnicalRow(label = "Candle Strength & ATR expansion", matches = stock.atrExpansion, value = "${stock.candleBodyStrength} & ATR dynamic expansion")
            }
        }

        // Multi-timeframe trend match block
        MultiTimeframeTrendSection(stock = stock, isHindi = isHindi)

        // Interactive AI Setup Report section with direct Gemini REST call
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "AI Pro Deep Breakout Analysis",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1A1D20)
                        )
                        Text(
                            text = "Powered by Gemini 3.5-flash",
                            fontSize = 11.sp,
                            color = Color.Gray
                        )
                    }
                    Icon(
                        Icons.Filled.AutoAwesome,
                        contentDescription = "AI Action",
                        tint = Color(0xFF9C27B0),
                        modifier = Modifier.size(24.dp)
                    )
                }

                Divider(color = Color(0xFFEEEEEE))

                when (val analysis = aiAnalysisState) {
                    is AiAnalysisState.Idle -> {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp)
                        ) {
                            Text(
                                text = if (isHindi) "गहन तकनीकी जांच शुरू करें" else "Generate instant AI assessment of breakout quality, manipulation check, and continuation probability.",
                                fontSize = 12.sp,
                                color = Color.Gray,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(bottom = 12.dp)
                            )
                            Button(
                                onClick = { viewModel.requestAiAnalysis(stock) },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF9C27B0)),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Filled.Psychology, contentDescription = "Analyze")
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(Localization.translate("generate_ai", isHindi), fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                    is AiAnalysisState.Loading -> {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp)
                        ) {
                            CircularProgressIndicator(color = Color(0xFF9C27B0))
                            Spacer(modifier = Modifier.height(12.dp))
                            Text("Engine analyzing volume depth, smart money pivots and history parameters...", fontSize = 11.sp, color = Color.Gray)
                        }
                    }
                    is AiAnalysisState.Success -> {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            // High scoring parameters badge
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFFF3E5F5), shape = RoundedCornerShape(8.dp))
                                    .padding(8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Filled.VerifiedUser, contentDescription = "Confidence verified", tint = Color(0xFF8E24AA), modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Operator Security Check: Clear", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF8E24AA))
                                }
                                Text(
                                    text = "Conf Score: ${stock.confidence}/100",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFF8E24AA)
                                )
                            }

                            SelectionContainer {
                                Text(
                                    text = analysis.report,
                                    fontSize = 13.sp,
                                    color = Color(0xFF424242),
                                    lineHeight = 18.sp
                                )
                            }

                            OutlinedButton(
                                onClick = { viewModel.requestAiAnalysis(stock) },
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.align(Alignment.End),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF9C27B0))
                            ) {
                                Icon(Icons.Filled.Refresh, contentDescription = "Retry analysis", modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Re-Analyze", fontSize = 11.sp)
                            }
                        }
                    }
                    is AiAnalysisState.Error -> {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp)
                        ) {
                            Icon(Icons.Filled.ErrorOutline, contentDescription = "Error", tint = Color.Red, modifier = Modifier.size(36.dp))
                            Text("AI scanner connection timed out: ${analysis.message}", fontSize = 12.sp, color = Color.Red, textAlign = TextAlign.Center)
                            Button(
                                onClick = { viewModel.requestAiAnalysis(stock) },
                                modifier = Modifier.padding(top = 8.dp)
                            ) {
                                Text("Retry")
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

// --- Tab 3: Simulated Broker & Portfolio Nodes ---

@Composable
fun PortfolioTab(
    viewModel: TradingViewModel,
    isHindi: Boolean
) {
    val positions by viewModel.positions.collectAsState()
    val summary by viewModel.portfolioSummary.collectAsState()

    var brokerDropdownExpanded by remember { mutableStateOf(false) }
    val selectedBrokerAccount by viewModel.selectedBroker.collectAsState()
    val isBrokerConnected by viewModel.isBrokerConnected.collectAsState()
    val brokerApiKey by viewModel.brokerApiKey.collectAsState()
    val brokerApiSecret by viewModel.brokerApiSecret.collectAsState()

    val brokerOptions = listOf("Zerodha Kite", "Upstox Pro", "Angel One Sky", "Groww App")

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { Spacer(modifier = Modifier.height(12.dp)) }

        // Broker Connect dropdown & Credentials card
        item {
            var inputApiKey by remember { mutableStateOf(brokerApiKey) }
            var inputApiSecret by remember { mutableStateOf(brokerApiSecret) }
            var isSecretVisible by remember { mutableStateOf(false) }
            
            // Sync with viewModel when credentials are saved
            LaunchedEffect(brokerApiKey, brokerApiSecret) {
                inputApiKey = brokerApiKey
                inputApiSecret = brokerApiSecret
            }

            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .background(if (isBrokerConnected) Color(0xFF4CAF50) else Color(0xFFD32F2F), CircleShape)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = selectedBrokerAccount,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Black
                                )
                            }
                            Text(
                                text = if (isBrokerConnected) {
                                    if (isHindi) "सुरक्षित ट्रेडिंग API से कनेक्टेड" else "Secure trading API connected"
                                } else {
                                    if (isHindi) "ट्रेडिंग API कनेक्टेड नहीं है" else "Secure trading API disconnected"
                                },
                                fontSize = 11.sp,
                                color = Color.Gray
                            )
                        }

                        Box {
                            OutlinedButton(
                                onClick = { brokerDropdownExpanded = true },
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(if (isHindi) "ब्रोकर चुनें" else "Switch Broker", fontSize = 11.sp)
                                Icon(Icons.Filled.ArrowDropDown, contentDescription = "dropdown")
                            }
                            DropdownMenu(
                                expanded = brokerDropdownExpanded,
                                onDismissRequest = { brokerDropdownExpanded = false }
                            ) {
                                brokerOptions.forEach { bro ->
                                    DropdownMenuItem(
                                        text = { Text(bro) },
                                        onClick = {
                                            viewModel.updateSelectedBroker(bro)
                                            brokerDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    Divider(color = Color(0xFFF1F5F9))

                    Text(
                        text = if (isHindi) "ब्रोकर API क्रेडेंशियल सेटिंग्स" else "Broker API Authorization Details",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A)
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = inputApiKey,
                            onValueChange = { inputApiKey = it },
                            label = { Text(if (isHindi) "API की (API Key)" else "Broker API Key") },
                            leadingIcon = { Icon(Icons.Filled.VpnKey, contentDescription = "Key Icon", tint = Color.Gray) },
                            singleLine = true,
                            enabled = !isBrokerConnected,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        )

                        OutlinedTextField(
                            value = inputApiSecret,
                            onValueChange = { inputApiSecret = it },
                            label = { Text(if (isHindi) "API सीक्रेट (API Secret)" else "Broker API Secret") },
                            leadingIcon = { Icon(Icons.Filled.Lock, contentDescription = "Lock Icon", tint = Color.Gray) },
                            trailingIcon = {
                                IconButton(onClick = { isSecretVisible = !isSecretVisible }) {
                                    Icon(
                                        imageVector = if (isSecretVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                                        contentDescription = "Toggle Secret Eye"
                                    )
                                }
                            },
                            singleLine = true,
                            enabled = !isBrokerConnected,
                            visualTransformation = if (isSecretVisible) androidx.compose.ui.text.input.VisualTransformation.None else androidx.compose.ui.text.input.PasswordVisualTransformation(),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        )

                        if (!isBrokerConnected) {
                            Button(
                                onClick = {
                                    if (inputApiKey.isNotBlank() && inputApiSecret.isNotBlank()) {
                                        viewModel.saveBrokerConnection(inputApiKey, inputApiSecret)
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(44.dp),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E88E5)),
                                enabled = inputApiKey.isNotBlank() && inputApiSecret.isNotBlank()
                            ) {
                                Icon(Icons.Filled.Link, contentDescription = "Connect API Link Icon")
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (isHindi) "सुरक्षित रूप से कनेक्ट करें" else "Securely Connect Broker",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        } else {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .background(Color(0xFFE8F5E9), shape = RoundedCornerShape(10.dp))
                                        .padding(vertical = 12.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Filled.CheckCircle,
                                            contentDescription = "Check Icon",
                                            tint = Color(0xFF2E7D32),
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = if (isHindi) "कनेक्टेड: लाइव" else "Connected: LIVE",
                                            fontSize = 12.sp,
                                            color = Color(0xFF2E7D32),
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }

                                OutlinedButton(
                                    onClick = {
                                        viewModel.disconnectBroker()
                                        inputApiKey = ""
                                        inputApiSecret = ""
                                    },
                                    modifier = Modifier.height(44.dp),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFC62828))
                                ) {
                                    Icon(Icons.Filled.LinkOff, contentDescription = "Disconnect Icon", modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = if (isHindi) "डिस्कनेक्ट" else "Disconnect",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Consolidated Portfolio Balance
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E88E5)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = if (isHindi) "कुल पोर्टफोलियो मूल्यांकन" else "Total Portfolio Value",
                        fontSize = 12.sp,
                        color = Color(0xFFE3F2FD),
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "Rs. ${String.format("%.2f", summary.totalCurrent)}",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(text = Localization.translate("daily_pnl", isHindi), fontSize = 11.sp, color = Color(0xFFE3F2FD))
                            Text(
                                text = "${if (summary.todaysProfitLoss >= 0) "Rs. +" else "Rs. "}${String.format("%.2f", summary.todaysProfitLoss)}",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (summary.todaysProfitLoss >= 0) Color(0xFFB9F6CA) else Color(0xFFFF8A80)
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(text = Localization.translate("total_return", isHindi), fontSize = 11.sp, color = Color(0xFFE3F2FD))
                            Text(
                                text = "${if (summary.totalPNLPercent >= 0) "+" else ""}${String.format("%.2f", summary.totalPNLPercent)}%",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (summary.totalPNLPercent >= 0) Color(0xFFB9F6CA) else Color(0xFFFF8A80)
                            )
                        }
                    }
                }
            }
        }

        // Active Positions header
        item {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = Localization.translate("active_positions", isHindi),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )

                if (positions.isNotEmpty()) {
                    TextButton(onClick = { viewModel.clearAllPositions() }) {
                        Text("Liquidate All", color = Color.Red, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Active Positions List row
        if (positions.isEmpty()) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(32.dp).fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            Icons.Filled.AccountBalanceWallet,
                            contentDescription = "No transactions",
                            tint = Color.LightGray,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "No active positions found in connected broker.",
                            fontSize = 12.sp,
                            color = Color.Gray,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        } else {
            items(positions) { pos ->
                PositionRowCard(
                    pos = pos,
                    isHindi = isHindi,
                    onSell = { viewModel.liquidatePosition(pos.symbol) }
                )
            }
        }

        item { Spacer(modifier = Modifier.height(24.dp)) }
    }
}

// --- Tab 4: Signals Alerts Terminal ---

@Composable
fun AlertsTab(
    viewModel: TradingViewModel,
    isHindi: Boolean
) {
    val alerts by viewModel.alerts.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item { Spacer(modifier = Modifier.height(12.dp)) }

        // Alert target config helper
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Alert API Gateways Available",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        GatewayChip(icon = Icons.Filled.AppShortcut, label = "Push", active = true)
                        GatewayChip(icon = Icons.Filled.Send, label = "Telegram", active = true)
                        GatewayChip(icon = Icons.Filled.Forum, label = "WhatsApp", active = true)
                        GatewayChip(icon = Icons.Filled.Mail, label = "Email", active = true)
                    }
                    Text(
                        text = "When triggers execute, signals are dispatched to configured integration channels instantly.",
                        fontSize = 10.sp,
                        color = Color.Gray,
                        lineHeight = 13.sp
                    )
                }
            }
        }

        // Live Alerts section
        item {
            Text(
                text = "My Monitoring Alerts",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                modifier = Modifier.padding(horizontal = 4.dp)
            )
        }

        if (alerts.isEmpty()) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(32.dp).fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Filled.NotificationsNone, contentDescription = "Empty Alerts", tint = Color.LightGray, modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(10.dp))
                        Text("No price triggers set. Open any stock and click alert to trace.", fontSize = 11.sp, color = Color.Gray)
                    }
                }
            }
        } else {
            items(alerts) { alert ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .background(
                                        if (alert.isTriggered) Color(0xFFE8F5E9) else Color(0xFFFFEBEE),
                                        CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (alert.isTriggered) Icons.Filled.CheckCircle else Icons.Filled.Alarm,
                                    contentDescription = "Status icon",
                                    tint = if (alert.isTriggered) Color(0xFF4CAF50) else Color(0xFFFF5252),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(alert.symbol, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                Text(
                                    text = "Crosses ${alert.direction} Rs. ${alert.priceTrigger} via ${alert.type}",
                                    fontSize = 11.sp,
                                    color = Color.DarkGray
                                )
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (alert.isTriggered) {
                                Text(
                                    text = "DISPATCHED",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFF2E7D32),
                                    modifier = Modifier
                                        .background(Color(0xFFE8F5E9), shape = RoundedCornerShape(4.dp))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                            }
                            IconButton(onClick = { viewModel.deleteAlert(alert.id) }) {
                                Icon(Icons.Filled.DeleteOutline, contentDescription = "Delete", tint = Color.Gray)
                            }
                        }
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(24.dp)) }
    }
}

// --- Tab 5: Premium Tools & Voice AI ---

@Composable
fun PremiumProTab(
    viewModel: TradingViewModel,
    isHindi: Boolean,
    isPremium: Boolean
) {
    val voiceTranscript by viewModel.voiceTranscript.collectAsState()
    val isListening by viewModel.isListening.collectAsState()

    var customVoiceQuery by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Upgrade SaaS Banner
        Card(
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .background(
                        brush = Brush.linearGradient(listOf(Color(0xFF1E88E5), Color(0xFF8E24AA))),
                        shape = RoundedCornerShape(16.dp)
                    )
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = Localization.translate("premium_unlock_title", isHindi),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                    Text(
                        text = Localization.translate("premium_unlock_sub", isHindi),
                        fontSize = 12.sp,
                        color = Color(0xFFE3F2FD)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Rs. 999 / month", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        Button(
                            onClick = { viewModel.togglePremium() },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color(0xFF8E24AA)),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(if (isPremium) "Deactivate" else "Subscribe Pro")
                        }
                    }
                }
            }
        }

        // Voice Assistant Command Node
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Mic, contentDescription = "Voice Input", tint = Color(0xFF1E88E5))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = Localization.translate("voice_assistant", isHindi),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                }

                Text(
                    text = Localization.translate("listen_hint", isHindi),
                    fontSize = 11.sp,
                    color = Color.Gray
                )

                // Simulative Listening/Speak triggers
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = customVoiceQuery,
                        onValueChange = { customVoiceQuery = it },
                        placeholder = { Text("Or write query (e.g. 'Show Reliance')", fontSize = 12.sp) },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
                    )

                    Button(
                        onClick = {
                            if (customVoiceQuery.isNotEmpty()) {
                                viewModel.runVoiceCommand(customVoiceQuery)
                                customVoiceQuery = ""
                            } else {
                                viewModel.runVoiceCommand("Show Tata Motors")
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E88E5))
                    ) {
                        Text("Ask UI")
                    }
                }

                AnimatedVisibility(visible = isListening) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFE3F2FD), shape = RoundedCornerShape(8.dp))
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(10.dp))
                        Text("NLP Voice parser matching query parameters...", fontSize = 11.sp, color = Color(0xFF1565C0))
                    }
                }

                voiceTranscript?.let { transcript ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFF1F3F5), shape = RoundedCornerShape(8.dp))
                            .padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Response: $transcript", fontSize = 12.sp, color = Color.DarkGray, fontWeight = FontWeight.Bold)
                        IconButton(onClick = { viewModel.resetVoice() }, modifier = Modifier.size(24.dp)) {
                            Icon(Icons.Filled.Close, contentDescription = "Clear response", modifier = Modifier.size(14.dp))
                        }
                    }
                }
            }
        }

        // Additional monetized elements (Heatmaps / Sectors)
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Sector Bullish Accumulation Heatmap",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                
                Divider(color = Color(0xFFEEEEEE))

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    HeatmapRow(sector = "Automobiles (NIFTY AUTO)", value = "+2.4%", rating = 94, barColor = Color(0xFF4CAF50))
                    HeatmapRow(sector = "Energy stocks (NIFTY ENERGY)", value = "+1.8%", rating = 88, barColor = Color(0xFF4CAF50))
                    HeatmapRow(sector = "Public Sector Banks (PSU BANK)", value = "+1.2%", rating = 78, barColor = Color(0xFF81C784))
                    HeatmapRow(sector = "Information Tech (NIFTY IT)", value = "-0.4%", rating = 45, barColor = Color(0xFFFF8A80))
                    HeatmapRow(sector = "Metal & Ores (NIFTY METAL)", value = "+3.6%", rating = 96, barColor = Color(0xFF2E7D32))
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

// --- Supporting Custom Composables ---

@Composable
fun ConfidenceMeterCard(isHindi: Boolean, rating: Int) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, Color(0xFFEEEEEE)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .drawBehind {
                        drawCircle(color = Color(0xFFF1F3F5))
                        drawArc(
                            color = Color(0xFF4CAF50),
                            startAngle = -90f,
                            sweepAngle = (rating / 100f) * 360f,
                            useCenter = false,
                            style = Stroke(width = 10f, cap = androidx.compose.ui.graphics.StrokeCap.Round)
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                Text("$rating%", fontSize = 15.sp, fontWeight = FontWeight.Black, color = Color(0xFF2E7D32))
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column {
                Text(
                    text = Localization.translate("unusual_volume", isHindi),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                Text(
                    text = if (isHindi) "88% भारतीय शेयरों में भारी वॉल्यूम और ब्रेकआउट संचय देखा जा रहा है" else "88% scanned breakout listings confirm institutional Smart Money absorption.",
                    fontSize = 11.sp,
                    color = Color.Gray,
                    lineHeight = 14.sp
                )
            }
        }
    }
}

@Composable
fun IndexTickerCard(name: String, value: String, change: String, isUp: Boolean, modifier: Modifier = Modifier) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, Color(0xFFEEEEEE)),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(name, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
            Text(value, fontSize = 16.sp, fontWeight = FontWeight.Black, color = Color.Black)
            Text(
                change,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = if (isUp) Color(0xFF2E7D32) else Color(0xFFC62828)
            )
        }
    }
}

@Composable
fun StockBreakoutSetupCard(
    stock: MarketStock,
    isHindi: Boolean,
    isWatchlisted: Boolean,
    onClick: () -> Unit,
    onBuyClick: () -> Unit,
    onAlertClick: () -> Unit,
    onWatchlistToggle: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, Color(0xFFEEEEEE)),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(stock.symbol, fontSize = 16.sp, fontWeight = FontWeight.Black, color = Color.Black)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stock.breakoutPattern,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1E88E5),
                            modifier = Modifier
                                .background(Color(0xFFE3F2FD), shape = RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    Text(stock.name, fontSize = 11.sp, color = Color.Gray, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        "Rs. ${stock.price}",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (stock.changePercent >= 0) Color(0xFF2E7D32) else Color(0xFFC62828)
                    )
                    Text(
                        "${if (stock.changePercent >= 0) "+" else ""}${stock.changePercent}%",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (stock.changePercent >= 0) Color(0xFF4CAF50) else Color(0xFFFF5252)
                    )
                }
            }

            // High priority setup indicator parameters
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                IndicatorsMiniTag("Confidence: ${stock.confidence}%")
                IndicatorsMiniTag("Volume: ${stock.volumeMultiplier}x")
                IndicatorsMiniTag("RSI: ${stock.rsi.toInt()}")
                IndicatorsMiniTag("Delivery: ${stock.deliveryPercentage}%")
            }

            Divider(color = Color(0xFFF1F3F5))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Column {
                        Text("Trig Zone", fontSize = 9.sp, color = Color.Gray)
                        Text("Rs. ${stock.resistance}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.DarkGray)
                    }
                    Column {
                        Text("Targets", fontSize = 9.sp, color = Color.Gray)
                        Text(stock.targets.split(",").first(), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32))
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    IconButton(
                        onClick = { onWatchlistToggle() },
                        modifier = Modifier
                            .size(32.dp)
                            .background(Color(0xFFF1F3F5), CircleShape)
                    ) {
                        Icon(
                            imageVector = if (isWatchlisted) Icons.Filled.Star else Icons.Outlined.StarBorder,
                            contentDescription = "Watchlist toggle",
                            tint = if (isWatchlisted) Color(0xFFFFB300) else Color.DarkGray,
                            modifier = Modifier.size(15.dp)
                        )
                    }

                    IconButton(
                        onClick = { onAlertClick() },
                        modifier = Modifier
                            .size(32.dp)
                            .background(Color(0xFFF1F3F5), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.AddAlert,
                            contentDescription = "Add alert",
                            tint = Color.DarkGray,
                            modifier = Modifier.size(15.dp)
                        )
                    }

                    Button(
                        onClick = { onBuyClick() },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 2.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Text("Trade Now", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun IndicatorsMiniTag(label: String) {
    Text(
        text = label,
        fontSize = 10.sp,
        color = Color.DarkGray,
        fontWeight = FontWeight.Medium,
        modifier = Modifier
            .background(Color(0xFFF1F3F5), shape = RoundedCornerShape(4.dp))
            .padding(horizontal = 6.dp, vertical = 2.dp)
    )
}

@Composable
fun EmptySearchBlock(isHindi: Boolean) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(Icons.Outlined.ContentPasteOff, contentDescription = "no results", tint = Color.LightGray, modifier = Modifier.size(48.dp))
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = if (isHindi) "कोई ब्रेकआउट परिणाम नहीं। फ़िल्टर बदलें।" else "No breakout listings matched. Adjust filters.",
            fontSize = 12.sp,
            color = Color.Gray,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun TechnicalRow(label: String, matches: Boolean, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Icon(
                imageVector = if (matches) Icons.Filled.CheckCircle else Icons.Filled.Cancel,
                contentDescription = "status",
                tint = if (matches) Color(0xFF4CAF50) else Color(0xFFFF5252),
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(label, fontSize = 12.sp, color = Color.Gray, fontWeight = FontWeight.Normal)
        }
        Text(value, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.DarkGray, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
fun MultiTimeframeTrendSection(stock: MarketStock, isHindi: Boolean) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                text = Localization.translate("multi_timeframe", isHindi),
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            Divider(color = Color(0xFFEEEEEE))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                TimeframeItem(label = "Daily (1D)", signal = stock.dailyTrendUp, modifier = Modifier.weight(1f))
                TimeframeItem(label = "4 Hour (4H)", signal = stock.fourHourTrendUp, modifier = Modifier.weight(1f))
                TimeframeItem(label = "1 Hour (1H)", signal = stock.oneHourTrendUp, modifier = Modifier.weight(1f))
                TimeframeItem(label = "15 Min (15m)", signal = stock.fifteenMinTrendUp, modifier = Modifier.weight(1f))
            }
            Text(
                text = "Trading rule: Only participate when over 3 multi-timeframes indicate direct upward confirmations.",
                fontSize = 10.sp,
                color = Color.Gray,
                lineHeight = 13.sp
            )
        }
    }
}

@Composable
fun TimeframeItem(label: String, signal: Boolean, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .background(if (signal) Color(0xFFE8F5E9) else Color(0xFFFFEBEE), shape = RoundedCornerShape(8.dp))
            .padding(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(label, fontSize = 10.sp, fontWeight = FontWeight.Medium, color = Color.Gray)
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = if (signal) "BULLISH" else "NEUTRAL",
            fontSize = 11.sp,
            fontWeight = FontWeight.ExtraBold,
            color = if (signal) Color(0xFF2E7D32) else Color(0xFFC62828)
        )
    }
}

@Composable
fun PositionRowCard(
    pos: PositionEntity,
    isHindi: Boolean,
    onSell: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(pos.symbol, fontSize = 16.sp, fontWeight = FontWeight.Black, color = Color.Black)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            pos.broker,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Gray,
                            modifier = Modifier
                                .background(Color(0xFFF1F3F5), shape = RoundedCornerShape(4.dp))
                                .padding(horizontal = 4.dp, vertical = 2.dp)
                        )
                    }
                    Text(pos.stockName, fontSize = 11.sp, color = Color.Gray)
                }

                Button(
                    onClick = { onSell() },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF5252)),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 2.dp),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.height(28.dp)
                ) {
                    Text("Exit / Sell", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }

            Divider(color = Color(0xFFF1F3F5))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Avg Buy Price", fontSize = 9.sp, color = Color.Gray)
                    Text("Rs. ${pos.buyPrice}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.DarkGray)
                }
                Column {
                    Text("Quantity", fontSize = 9.sp, color = Color.Gray)
                    Text("${pos.quantity}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.DarkGray)
                }
                Column {
                    Text("P&L Profit", fontSize = 9.sp, color = Color.Gray)
                    val pnl = (pos.currentPrice - pos.buyPrice) * pos.quantity
                    Text(
                        "${if (pnl >= 0) "Rs. +" else "Rs. "}${String.format("%.1f", pnl)}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (pnl >= 0) Color(0xFF2E7D32) else Color(0xFFC62828)
                    )
                }
            }
        }
    }
}

@Composable
fun GatewayChip(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, active: Boolean) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .background(if (active) Color(0xFFE3F2FD) else Color(0xFFF1F3F5), shape = RoundedCornerShape(12.dp))
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Icon(icon, contentDescription = label, tint = if (active) Color(0xFF1E88E5) else Color.Gray, modifier = Modifier.size(13.dp))
        Spacer(modifier = Modifier.width(4.dp))
        Text(label, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = if (active) Color(0xFF1E88E5) else Color.Gray)
    }
}

@Composable
fun HeatmapRow(sector: String, value: String, rating: Int, barColor: Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(sector, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color.DarkGray)
            LinearProgressIndicator(
                progress = rating / 100f,
                color = barColor,
                trackColor = Color(0xFFF1F3F5),
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .height(4.dp)
                    .padding(top = 4.dp)
            )
        }
        Text(value, fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, color = barColor)
    }
}

// --- Pop-up Dialog: Simulated Broker Trade Execution ---

@Composable
fun BrokerDirectBuyDialog(
    stock: MarketStock,
    viewModel: TradingViewModel,
    onDismiss: () -> Unit,
    onConfirm: (qty: Int, broker: String) -> Unit
) {
    var qtyString by remember { mutableStateOf("10") }
    val isBrokerConnected by viewModel.isBrokerConnected.collectAsState()
    val selectedBrokerAccount by viewModel.selectedBroker.collectAsState()

    var selectedBroker by remember { mutableStateOf(selectedBrokerAccount) }
    val brokers = listOf("Zerodha Kite", "Upstox Pro", "Angel One Sky", "Groww App")

    // Keep selectedBroker in sync with active broker state when loaded
    LaunchedEffect(selectedBrokerAccount) {
        selectedBroker = selectedBrokerAccount
    }

    AlertDialog(
        onDismissRequest = { onDismiss() },
        confirmButton = {
            Button(
                onClick = {
                    val qtyVal = qtyString.toIntOrNull() ?: 10
                    onConfirm(qtyVal, selectedBroker)
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isBrokerConnected) Color(0xFF2E7D32) else Color(0xFFE65100)
                )
            ) {
                Text(
                    if (isBrokerConnected) "Confirm Exec API Order" else "Execute Sandbox Trade",
                    fontWeight = FontWeight.Bold
                )
            }
        },
        dismissButton = {
            TextButton(onClick = { onDismiss() }) { Text("Cancel") }
        },
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Filled.AccountBalance,
                    contentDescription = "broker",
                    tint = if (isBrokerConnected) Color(0xFF2E7D32) else Color(0xFFE65100)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("Execute Direct Trade", fontSize = 16.sp, fontWeight = FontWeight.Black)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                if (isBrokerConnected) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFE8F5E9), shape = RoundedCornerShape(8.dp))
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Filled.CheckCircle, contentDescription = "Active", tint = Color(0xFF2E7D32), modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Authenticated routing via $selectedBroker is ACTIVE",
                            fontSize = 10.sp,
                            color = Color(0xFF2E7D32),
                            fontWeight = FontWeight.Bold
                        )
                    }
                } else {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFFFF3E0), shape = RoundedCornerShape(8.dp))
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Filled.Warning, contentDescription = "Offline Warning", tint = Color(0xFFE65100), modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "OFFLINE SANDBOX MODE: Configure API keds in Portfolio tab.",
                            fontSize = 10.sp,
                            color = Color(0xFFE65100),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Text("Direct execution via connected Indian broker pipelines.", fontSize = 11.sp, color = Color.Gray)
                
                Divider(color = Color(0xFFEEEEEE))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Stock Symbol:", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                    Text(stock.symbol, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Execution Price (LTP):", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                    Text("Rs. ${stock.price}", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                // Choose broker
                Column {
                    Text("Primary Execution Broker:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.DarkGray)
                    Spacer(modifier = Modifier.height(4.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(brokers) { b ->
                            val active = selectedBroker == b
                            Text(
                                text = b,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (active) Color.White else Color.DarkGray,
                                modifier = Modifier
                                    .background(if (active) Color(0xFF2E7D32) else Color(0xFFF1F3F5), RoundedCornerShape(8.dp))
                                    .clickable { selectedBroker = b }
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                // Choose quantity
                OutlinedTextField(
                    value = qtyString,
                    onValueChange = { qtyString = it },
                    label = { Text("Trade Quantity (Shares)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        shape = RoundedCornerShape(16.dp),
        containerColor = Color.White
    )
}

// --- Pop-up Dialog: Trigger Alert setup ---

@Composable
fun CreateAlertDialog(
    stock: MarketStock,
    onDismiss: () -> Unit,
    onConfirm: (triggerPrice: Double, direction: String, type: String) -> Unit
) {
    var triggerStr by remember { mutableStateOf(stock.price.toString()) }
    var direction by remember { mutableStateOf("ABOVE") }
    var selectedGateway by remember { mutableStateOf("Push") }
    val gateways = listOf("Push", "Telegram", "WhatsApp", "Email")

    AlertDialog(
        onDismissRequest = { onDismiss() },
        confirmButton = {
            Button(
                onClick = {
                    val trigVal = triggerStr.toDoubleOrNull() ?: stock.price
                    onConfirm(trigVal, direction, selectedGateway)
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E88E5))
            ) {
                Text("Set Live Alert", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = { onDismiss() }) { Text("Cancel") }
        },
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.AddAlert, contentDescription = "alert", tint = Color(0xFF1E88E5))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Add Setup Trigger", fontSize = 16.sp, fontWeight = FontWeight.Black)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Triggers immediately when target prices are hit.", fontSize = 11.sp, color = Color.Gray)

                OutlinedTextField(
                    value = triggerStr,
                    onValueChange = { triggerStr = it },
                    label = { Text("Trigger Price (Rs.)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Select ABOVE/BELOW direction
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = { direction = "ABOVE" },
                        colors = ButtonDefaults.buttonColors(containerColor = if (direction == "ABOVE") Color(0xFF1E88E5) else Color(0xFFF1F3F5)),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Price Crosses ABOVE", color = if (direction == "ABOVE") Color.White else Color.Black, fontSize = 11.sp)
                    }

                    Button(
                        onClick = { direction = "BELOW" },
                        colors = ButtonDefaults.buttonColors(containerColor = if (direction == "BELOW") Color(0xFF1E88E5) else Color(0xFFF1F3F5)),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Price Crosses BELOW", color = if (direction == "BELOW") Color.White else Color.Black, fontSize = 11.sp)
                    }
                }

                // Choose dispatch gateway channels
                Column {
                    Text("APIs Dispatch Gateway:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.DarkGray)
                    Spacer(modifier = Modifier.height(4.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(gateways) { gate ->
                            val active = selectedGateway == gate
                            Text(
                                text = gate,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (active) Color.White else Color.DarkGray,
                                modifier = Modifier
                                    .background(if (active) Color(0xFF1E88E5) else Color(0xFFF1F3F5), RoundedCornerShape(8.dp))
                                    .clickable { selectedGateway = gate }
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }
        },
        shape = RoundedCornerShape(16.dp),
        containerColor = Color.White
    )
}
