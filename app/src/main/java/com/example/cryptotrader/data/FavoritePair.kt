package com.example.cryptotrader.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favorite_pairs")
data class FavoritePair(
    @PrimaryKey val symbol: String     // 以 symbol 作为主键
)
