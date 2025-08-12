package com.example.cryptotrader.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "orders")
data class OrderEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val symbol: String,
    val price: Double,
    val amount: Double,
    val side: String,
    val type: String,
    val status: String
)
