package com.example.cryptotrader.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

/**
 * Main Room database containing the watchlist and orders tables.
 */
@Database(
    entities = [WatchlistEntity::class, OrderEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun watchlistDao(): WatchlistDao
    abstract fun orderDao(): OrderDao
}
