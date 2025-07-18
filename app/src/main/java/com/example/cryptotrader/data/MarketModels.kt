package com.example.cryptotrader.data

/**
 * Data models representing market information such as live ticks and candles.
 */

data class PriceUpdate(
    val symbol: String,
    val last: Double,
    val ask: Double,
    val bid: Double
)

/** Candle information for a specified time interval. */
data class Candle(
    val time: Long,
    val open: Double,
    val high: Double,
    val low: Double,
    val close: Double
)

/** Possible sides of an order. */
enum class OrderSide { BUY, SELL }

/** Type of order: market or limit. */
enum class OrderType { MARKET, LIMIT }
