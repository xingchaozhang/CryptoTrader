package com.example.cryptotrader.data.remote

import com.example.cryptotrader.data.Candle
import com.example.cryptotrader.data.PriceUpdate
import com.example.cryptotrader.data.local.OrderEntity
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for providing market data and submitting orders.
 */
interface MarketRepository {
    /**
     * Subscribe to a continuous stream of price updates for the given symbol.
     */
    fun subscribePriceStream(symbol: String): Flow<PriceUpdate>

    /**
     * Load a list of historical candles for the given symbol. The implementation
     * may retrieve from a remote API or generate simulated data.
     */
    suspend fun getHistoricalCandles(symbol: String): List<Candle>

    /**
     * Place a mock order. Typically this will simply persist the order locally.
     */
    suspend fun placeOrder(order: OrderEntity)
}
