package com.example.cryptotrader.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

/**
 * Main Room database containing the watchlist and orders tables.
 */
@Database(
    entities = [WatchlistEntity::class, OrderEntity::class],
    version = 1,
    exportSchema = false // 关闭 schema 导出，避免警告
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun watchlistDao(): WatchlistDao
    abstract fun orderDao(): OrderDao
}
