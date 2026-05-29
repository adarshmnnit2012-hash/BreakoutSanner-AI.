package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "watchlist")
data class WatchlistEntity(
    @PrimaryKey val symbol: String,
    val addedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "positions")
data class PositionEntity(
    @PrimaryKey val symbol: String,
    val stockName: String,
    val buyPrice: Double,
    val quantity: Int,
    val currentPrice: Double,
    val broker: String, // e.g. Zerodha, Upstox, Angel One, Groww
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "alerts")
data class AlertEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val symbol: String,
    val stockName: String,
    val priceTrigger: Double,
    val direction: String, // ABOVE, BELOW
    val type: String, // Push, Telegram, WhatsApp, Email
    val isTriggered: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)
