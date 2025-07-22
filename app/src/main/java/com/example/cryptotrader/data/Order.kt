package com.example.cryptotrader.data

import androidx.compose.runtime.Immutable

@Immutable
data class Order(
    val id: Long,
    val symbol: String,
    val price: Float,           
    val qty: Float,
    val side: Side,             
    val type: Type,             
    val status: Status,         
    val timeMillis: Long
) {
    enum class Side { BUY, SELL }
    enum class Type { LIMIT, MARKET }
    enum class Status { OPEN, FILLED, CANCELED }
}
