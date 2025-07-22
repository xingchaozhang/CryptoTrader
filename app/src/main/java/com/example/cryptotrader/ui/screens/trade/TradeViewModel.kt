package com.example.cryptotrader.ui.screens.trade

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cryptotrader.data.Order
import com.example.cryptotrader.data.OrderRepository
import com.example.cryptotrader.ui.detail.OrderBookEntry
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.Locale
import kotlin.random.Random

class TradeViewModel(
    private val symbol: String,
    initPrice: Float
) : ViewModel() {

    private val _ui = MutableStateFlow(
        TradeUiState(
            symbol = symbol,
            latestPrice = initPrice,
            priceField = initPrice.noComma(),
            orderBook = makeBook(initPrice)
        )
    )
    val ui: StateFlow<TradeUiState> = _ui

    init {
        /** ------ 伪实时行情 ------ */
        viewModelScope.launch {
            var price = initPrice
            while (isActive) {
                price = (price + Random.nextFloat() * 60f - 30f).coerceAtLeast(1f)
                val book = makeBook(price)
                val bestAsk = book.minByOrNull { it.ask }?.ask ?: price
                val bestBid = book.maxByOrNull { it.bid }?.bid ?: price

                /* 更新 UI */
                _ui.update {
                    val s1 = it.copy(latestPrice = price, orderBook = book)
                    /* 市价单时同步价格框 */
                    if (s1.orderType == OrderType.MARKET)
                        s1.copy(priceField = price.noComma())
                    else s1
                }

                /* 撮合限价单 */
                OrderRepository.matchWith(bestBid, bestAsk)

                kotlinx.coroutines.delay(500)
            }
        }

        /** ------ 监听订单仓库 ------ */
        viewModelScope.launch {
            OrderRepository.orders.collect { list ->
                _ui.update { it.copy(allOrders = list.filter { o -> o.symbol == symbol }) }
            }
        }
    }

    /* ---------- 表单交互 ---------- */
    fun switchSide(buy: Boolean) = _ui.update { it.copy(isBuy = buy) }
    fun setOrderType(t: OrderType) = _ui.update { it.copy(orderType = t) }
    fun onPriceChange(v: String) = _ui.update { it.copy(priceField = v) }
    fun onQtyChange(txt: String) = _ui.update { syncSlider(it = it.copy(qtyField = txt)) }
    fun onSliderChange(v: Float) = _ui.update { syncQty(it.copy(sliderPos = v)) }
    fun toggleStop() = _ui.update { it.copy(stopLossEnabled = !it.stopLossEnabled) }

    /* 双向同步 qty ↔ slider */
    private fun syncSlider(it: TradeUiState): TradeUiState {
        val price = it.priceField.parseFloat() ?: return it
        val qty = it.qtyField.parseFloat() ?: return it
        val pos = (price * qty / it.availableBalance).coerceIn(0f, 1f)
        return it.copy(sliderPos = pos)
    }

    private fun syncQty(it: TradeUiState): TradeUiState {
        val price = it.priceField.parseFloat() ?: return it
        val qty = (it.availableBalance * it.sliderPos / price)
        return it.copy(qtyField = qty.noComma(4))
    }

    /* ---------- 下单 ---------- */
    fun placeOrder() {
        val price = _ui.value.priceField.parseFloat() ?: return
        val qty = _ui.value.qtyField.parseFloat() ?: return
        if (price <= 0f || qty <= 0f) return

        OrderRepository.place(
            symbol = symbol,
            price = price,
            qty = qty,
            side = if (_ui.value.isBuy) Order.Side.BUY else Order.Side.SELL,
            type = if (_ui.value.orderType == OrderType.LIMIT) Order.Type.LIMIT
            else Order.Type.MARKET
        )
    }

    fun cancelOrder(id: Long) = OrderRepository.cancel(id)
    fun cancelAll() = OrderRepository.cancelAll(symbol)

    /* ---------- 假盘口生成 ---------- */
    private fun makeBook(mid: Float): List<OrderBookEntry> = List(10) { i ->
        val spread = 2f + i
        OrderBookEntry(
            amount = Random.nextFloat() * 1f,
            bid = mid - spread,
            ask = mid + spread
        )
    }
}

/* ---------- 扩展 ---------- */
private fun String.parseFloat() = replace(",", "").toFloatOrNull()
private fun Float.noComma(dec: Int = 2) =
    "%.${dec}f".format(Locale.US, this)
