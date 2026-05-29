package com.example.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface TradingDao {

    // Watchlist
    @Query("SELECT * FROM watchlist ORDER BY addedAt DESC")
    fun getAllWatchlist(): Flow<List<WatchlistEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWatchlist(entity: WatchlistEntity)

    @Query("DELETE FROM watchlist WHERE symbol = :symbol")
    suspend fun deleteWatchlist(symbol: String)

    @Query("SELECT EXISTS(SELECT 1 FROM watchlist WHERE symbol = :symbol)")
    fun isWatchlisted(symbol: String): Flow<Boolean>

    // Positions (Portfolio tracking)
    @Query("SELECT * FROM positions ORDER BY timestamp DESC")
    fun getAllPositions(): Flow<List<PositionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPosition(entity: PositionEntity)

    @Query("DELETE FROM positions WHERE symbol = :symbol")
    suspend fun deletePosition(symbol: String)

    @Query("DELETE FROM positions")
    suspend fun clearPositions()

    // Alerts
    @Query("SELECT * FROM alerts ORDER BY timestamp DESC")
    fun getAllAlerts(): Flow<List<AlertEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlert(entity: AlertEntity)

    @Query("DELETE FROM alerts WHERE id = :id")
    suspend fun deleteAlert(id: Int)

    @Query("UPDATE alerts SET isTriggered = 1 WHERE id = :id")
    suspend fun markAlertTriggered(id: Int)
}
