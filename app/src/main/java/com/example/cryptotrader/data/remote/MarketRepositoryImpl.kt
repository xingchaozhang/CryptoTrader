package com.example.cryptotrader.data.remote

import android.util.Log
import com.example.cryptotrader.BinancePriceStreamer
import com.example.cryptotrader.data.Candle
import com.example.cryptotrader.data.PriceUpdate
import com.example.cryptotrader.data.local.OrderDao
import com.example.cryptotrader.data.local.OrderEntity
import com.example.cryptotrader.data.local.WatchlistDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
import kotlin.random.Random

class MarketRepositoryImpl @Inject constructor(
    private val watchlistDao: WatchlistDao,
    private val orderDao: OrderDao
) : MarketRepository {

    private val TAG = "MarketRepo"

    override fun subscribePriceStream(symbol: String): Flow<PriceUpdate> = callbackFlow {
        Log.d(TAG, "subscribePriceStream: $symbol")

        // ① 建立 Binance 行情流，只订阅当前 symbol
        val streamer = BinancePriceStreamer(listOf(symbol))
        streamer.start()

        // ② 收集价格推送并发送给上游
        val job = launch {
            streamer.tickerFlow.collect { (_, priceStr) ->
                val price = priceStr.toDoubleOrNull() ?: return@collect
                trySend(PriceUpdate(symbol, price, price, price))
            }
        }

        // ③ Flow 取消时关闭 WebSocket
        awaitClose {
            job.cancel()
            streamer.stop()
        }
    }.flowOn(Dispatchers.IO)

    /* -------- 下面方法保持原逻辑（仅生成本地假 K 线 / 下单） -------- */

    override suspend fun getHistoricalCandles(symbol: String): List<Candle> {
        Log.d(TAG, "getHistoricalCandles: $symbol")
        val candles = mutableListOf<Candle>()
        val now = System.currentTimeMillis()
        var lastClose = 30000.0
        val random = Random(now)
        val intervalMs = 60_000L // 1 minute
        repeat(60) { i ->
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
        Log.d(TAG, "placeOrder: $order")
        withContext(Dispatchers.IO) { orderDao.insert(order) }
    }
}