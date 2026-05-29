package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [WatchlistEntity::class, PositionEntity::class, AlertEntity::class],
    version = 1,
    exportSchema = false
)
abstract class TradingDatabase : RoomDatabase() {
    abstract fun tradingDao(): TradingDao

    companion object {
        @Volatile
        private var INSTANCE: TradingDatabase? = null

        fun getDatabase(context: Context): TradingDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    TradingDatabase::class.java,
                    "breakout_scanner_db"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
