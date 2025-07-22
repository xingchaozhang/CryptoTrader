package com.example.cryptotrader.data.remote

import android.util.Log
import com.example.cryptotrader.data.Candle
import com.example.cryptotrader.data.PriceUpdate
import com.example.cryptotrader.data.local.OrderDao
import com.example.cryptotrader.data.local.OrderEntity
import com.example.cryptotrader.data.local.WatchlistDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import javax.inject.Inject
import kotlin.random.Random

/**
 * A simple implementation of [MarketRepository] that simulates price ticks and candle data.
 */
class MarketRepositoryImpl @Inject constructor(
    private val watchlistDao: WatchlistDao,
    private val orderDao: OrderDao
) : MarketRepository {

    private val TAG = "MarketRepo"

    override fun subscribePriceStream(symbol: String): Flow<PriceUpdate> = flow {
        Log.d(TAG, "subscribePriceStream: $symbol")
        var last = 30000.0
        val random = Random(System.currentTimeMillis())
        while (true) {
            delay(500L) // update every 500ms
            val change = random.nextDouble(-20.0, 20.0)
            last = (last + change).coerceAtLeast(1.0)
            emit(PriceUpdate(symbol, last, last + 0.5, last - 0.5))
        }
    }.flowOn(Dispatchers.IO)

    override suspend fun getHistoricalCandles(symbol: String): List<Candle> {
        Log.d(TAG, "getHistoricalCandles: $symbol")
        val candles = mutableListOf<Candle>()
        val now = System.currentTimeMillis()
        var lastClose = 30000.0
        val random = Random(now)
        val intervalMs = 60_000L // 1 minute
        for (i in 0 until 60) {
            val open = lastClose
            val high = open + random.nextDouble(0.0, 50.0)
            val low = open - random.nextDouble(0.0, 50.0)
            val close = low + random.nextDouble(0.0, high - low)
            val time = now - (59 - i) * intervalMs
            candles += Candle(time, open, high, low, close)
            lastClose = close
        }
        return candles
    }

    override suspend fun placeOrder(order: OrderEntity) {
        Log.d(TAG, "placeOrder: ${'$'}order")
        withContext(Dispatchers.IO) {
            orderDao.insert(order)
        }
    }
}
