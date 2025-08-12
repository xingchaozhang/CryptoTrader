package com.example.cryptotrader.ui.detail

import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random

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
    val amount: Float,
    val bid: Float,
    val ask: Float
)

/** K 线周期 */
enum class CandlePeriod(val minutes: Int) { M15(15), H1(60), H4(240), D1(1440) }

private val rnd = Random(System.currentTimeMillis())

/** 以 seed 作为锚点生成一段历史 K 线 */
fun genInitialCandles(seed: Float, p: CandlePeriod, size: Int = 60): List<Candle> {
    val now = System.currentTimeMillis()
    val step = p.minutes * 60_000L
    val aligned = now / step * step
    var prev = seed
    val amp = seed * 0.02f // 单根最大摆动 ~2%

    return List(size) { i ->
        val t = aligned - (size - 1 - i) * step
        val op = prev
        val cl = op + rnd.nextFloat() * amp * 2 - amp
        val hi = max(op, cl) + rnd.nextFloat() * amp
        val lo = min(op, cl) - rnd.nextFloat() * amp
        prev = cl
        Candle(t.toFloat(), op, hi, lo, cl)
    }
}

/** 用最新 tick 更新 K 线（不足一个周期时更新最后一根，跨周期时新开一根） */
fun updateByTick(
    list: List<Candle>,
    p: CandlePeriod,
    now: Long,
    price: Float,
    maxBars: Int = 60
): List<Candle> {
    if (list.isEmpty()) return list
    val step = p.minutes * 60_000L
    val last = list.last()

    return if (now < last.time.toLong() + step) {
        list.dropLast(1) + last.copy(
            high = max(last.high, price),
            low = min(last.low, price),
            close = price
        )
    } else {
        val aligned = now / step * step
        val open = last.close
        val newCandle = Candle(
            time = aligned.toFloat(),
            open = open,
            high = max(open, price),
            low = min(open, price),
            close = price
        )
        (list + newCandle).takeLast(maxBars)
    }
}

/** 生成以 anchor 为中心的盘口 */
fun defaultOrderBookAround(anchor: Float, levels: Int = 20): List<OrderBookEntry> {
    val step = anchor * 0.0005f // 0.05%
    return List(levels) { i ->
        val jitter = step * 0.2f * (rnd.nextFloat() * 2 - 1)
        OrderBookEntry(
            amount = 0.02f + rnd.nextFloat() * 0.28f,
            bid = anchor - i * step + jitter,
            ask = anchor + i * step - jitter
        )
    }
}

/** 盘口轻微抖动 */
fun jitterOrderBook(book: List<OrderBookEntry>): List<OrderBookEntry> =
    book.mapIndexed { i, it ->
        val d = (rnd.nextFloat() * 2 - 1) * (book.getOrNull(0)?.let { b -> (b.ask + b.bid) / 2f } ?: 100f) * 0.0001f
        it.copy(
            amount = (it.amount + (rnd.nextFloat() - 0.5f) * 0.02f).coerceIn(0.01f, 0.4f),
            bid = it.bid + d,
            ask = it.ask - d
        )
    }