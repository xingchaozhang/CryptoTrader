package com.example.cryptotrader.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room entity representing a crypto pair in the user's watchlist.
 */
@Entity(tableName = "watchlist")
data class WatchlistEntity(
    @PrimaryKey val symbol: String,
    val name: String
)
