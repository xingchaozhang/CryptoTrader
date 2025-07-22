package com.example.cryptotrader.ui.detail

import kotlin.random.Random
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

/** 单根 K 线 */
data class Candle(
    val time: Float,
    val open: Float,
    val high: Float,
    val low: Float,
    val close: Float,
)

/** 一档盘口 */
data class OrderBookEntry(
    val amount: Float,      // 成交量或委托量
    val bid: Float,         // 买价
    val ask: Float          // 卖价
)

/* ------------ 数据生成与动态刷新 ------------ */

private val random = Random(System.currentTimeMillis())

fun defaultFakeCandles(): List<Candle> {
    val now = System.currentTimeMillis()
    var close = 118_000f
    return List(60) { i ->
        val open = close + random.nextFloat() * 1000f - 500f
        val high = maxOf(open, close) + random.nextFloat() * 300f
        val low  = minOf(open, close) - random.nextFloat() * 300f
        Candle(time = (now - (59 - i) * 60_000).toFloat(), open, high, low, close).also {
            close = it.close + random.nextFloat() * 400f - 200f
        }
    }
}

fun defaultOrderBook(): List<OrderBookEntry> = List(20) {
    OrderBookEntry(
        amount = random.nextFloat() * 0.3f,
        bid    = 118_000f - it * 5f - random.nextFloat() * 2f,
        ask    = 118_000f + it * 5f + random.nextFloat() * 2f
    )
}

/** 持续对烛条和盘口做微幅更新，用于演示实时刷新 */
suspend fun startFakeStreaming(
    candles: MutableStateFlow<List<Candle>>,
    orderBook: MutableStateFlow<List<OrderBookEntry>>
) {
    while (true) {
        // 1️⃣ 更新最后一根 K 线
        candles.update { list ->
            if (list.isEmpty()) list
            else {
                val last = list.last()
                val newClose = last.close + random.nextFloat() * 60f - 30f
                list.dropLast(1) + last.copy(
                    high = maxOf(last.high, newClose),
                    low = minOf(last.low, newClose),
                    close = newClose
                )
            }
        }

        // 2️⃣ 轻微洗盘口
        orderBook.update { book ->
            book.mapIndexed { i, it ->
                val delta = random.nextFloat() * 3f - 1.5f
                it.copy(
                    amount = (it.amount + random.nextFloat() * 0.05f).coerceAtMost(0.4f),
                    bid    = it.bid + delta,
                    ask    = it.ask - delta
                )
            }
        }

        kotlinx.coroutines.delay(200)   // ≤ 500 ms
    }
}
