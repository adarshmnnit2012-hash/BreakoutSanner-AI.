package com.example.data

import com.example.data.local.TradingDao
import com.example.data.local.WatchlistEntity
import com.example.data.local.PositionEntity
import com.example.data.local.AlertEntity
import kotlinx.coroutines.flow.Flow

class TradingRepository(private val tradingDao: TradingDao) {

    // Watchlist
    val allWatchlist: Flow<List<WatchlistEntity>> = tradingDao.getAllWatchlist()

    suspend fun insertWatchlist(symbol: String) {
        tradingDao.insertWatchlist(WatchlistEntity(symbol = symbol))
    }

    suspend fun deleteWatchlist(symbol: String) {
        tradingDao.deleteWatchlist(symbol)
    }

    fun isWatchlisted(symbol: String): Flow<Boolean> {
        return tradingDao.isWatchlisted(symbol)
    }

    // Positions
    val allPositions: Flow<List<PositionEntity>> = tradingDao.getAllPositions()

    suspend fun insertPosition(position: PositionEntity) {
        tradingDao.insertPosition(position)
    }

    suspend fun deletePosition(symbol: String) {
        tradingDao.deletePosition(symbol)
    }

    suspend fun clearPositions() {
        tradingDao.clearPositions()
    }

    // Alerts
    val allAlerts: Flow<List<AlertEntity>> = tradingDao.getAllAlerts()

    suspend fun insertAlert(alert: AlertEntity) {
        tradingDao.insertAlert(alert)
    }

    suspend fun deleteAlert(id: Int) {
        tradingDao.deleteAlert(id)
    }

    suspend fun markAlertTriggered(id: Int) {
        tradingDao.markAlertTriggered(id)
    }
}
