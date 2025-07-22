package com.example.cryptotrader.data

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import java.util.concurrent.atomic.AtomicLong

object OrderRepository {

    private val _orders = MutableStateFlow<List<Order>>(emptyList())
    val orders: StateFlow<List<Order>> = _orders

    private val idGen = AtomicLong(1)

    /* ------------ 下单 ------------ */
    fun place(
        symbol: String,
        price: Float,
        qty: Float,
        side: Order.Side,
        type: Order.Type
    ) {
        val now = System.currentTimeMillis()
        val order = Order(
            id = idGen.getAndIncrement(),
            symbol = symbol,
            price = price,
            qty = qty,
            side = side,
            type = type,
            status = if (type == Order.Type.MARKET) Order.Status.FILLED
            else Order.Status.OPEN,
            timeMillis = now
        )
        _orders.update { it + order }
    }

    /* ------------ 撤单 ------------ */
    fun cancel(id: Long) = _orders.update { list ->
        list.map {
            if (it.id == id && it.status == Order.Status.OPEN)
                it.copy(status = Order.Status.CANCELED) else it
        }
    }

    fun cancelAll(symbol: String) = _orders.update { list ->
        list.map {
            if (it.symbol == symbol && it.status == Order.Status.OPEN)
                it.copy(status = Order.Status.CANCELED) else it
        }
    }

    /* ------------ 撮合：在最新 BBO 变化时调用 ------------ */
    fun matchWith(bestBid: Float, bestAsk: Float) = _orders.update { list ->
        list.map { o ->
            if (o.status == Order.Status.OPEN) {
                when (o.side) {
                    Order.Side.BUY -> if (bestAsk <= o.price) o.copy(status = Order.Status.FILLED) else o
                    Order.Side.SELL -> if (bestBid >= o.price) o.copy(status = Order.Status.FILLED) else o
                }
            } else o
        }
    }
}
