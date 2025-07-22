package com.example.cryptotrader.data

import androidx.compose.runtime.Immutable

@Immutable
data class Order(
    val id: Long,
    val symbol: String,
    val price: Float,           // 提交时价格；市价单=最新价格快照
    val qty: Float,
    val side: Side,             // BUY / SELL
    val type: Type,             // LIMIT / MARKET
    val status: Status,         // OPEN / FILLED / CANCELED
    val timeMillis: Long
) {
    enum class Side { BUY, SELL }
    enum class Type { LIMIT, MARKET }
    enum class Status { OPEN, FILLED, CANCELED }
}
