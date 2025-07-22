package com.example.cryptotrader.ui.screens.trade

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cryptotrader.data.Order
import com.example.cryptotrader.data.OrderRepository
import com.example.cryptotrader.ui.detail.OrderBookEntry
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Locale
import kotlin.random.Random

class TradeViewModel(private val symbol: String) : ViewModel() {

    /* ---------- UI State ---------- */
    private val _ui = MutableStateFlow(
        TradeUiState(
            symbol = symbol,
            latestPrice = 0f,
            priceField  = "",
            orderBook   = emptyList()
        )
    )
    val ui: StateFlow<TradeUiState> = _ui

    /* ---------- 订单仓库监听 ---------- */
    init {
        viewModelScope.launch {
            OrderRepository.orders.collect { list ->
                _ui.update { it.copy(allOrders = list.filter { o -> o.symbol == symbol }) }
            }
        }
    }

    /* ---------- 由外部(TradeScreen)推送最新价 ---------- */
    fun updateLatestPrice(p: Float) {
        val book = makeBook(p)
        val bestAsk = book.minByOrNull { it.ask }?.ask ?: p
        val bestBid = book.maxByOrNull { it.bid }?.bid ?: p

        _ui.update {
            var s = it.copy(latestPrice = p, orderBook = book)

            // 市价单实时同步输入框
            if (s.orderType == OrderType.MARKET) s = s.copy(priceField = p.noComma())

            // 若价格或余额变化，需要同步滑块/数量
            s = syncSlider(s)
            s
        }

        // 撮合限价
        OrderRepository.matchWith(bestBid, bestAsk)
    }

    /* ---------- 表单交互 ---------- */
    fun switchSide(buy: Boolean)          = _ui.update { it.copy(isBuy = buy) }
    fun setOrderType(t: OrderType)        = _ui.update { it.copy(orderType = t) }
    fun onPriceChange(v: String)          = _ui.update { it.copy(priceField = v) }
    fun onQtyChange(txt: String)          = _ui.update { syncSlider(it.copy(qtyField = txt)) }
    fun onSliderChange(v: Float)          = _ui.update { syncQty(it.copy(sliderPos = v)) }
    fun toggleStop()                      = _ui.update { it.copy(stopLossEnabled = !it.stopLossEnabled) }

    /* ---- qty ↔ slider 双向同步 ---- */
    private fun syncSlider(it: TradeUiState): TradeUiState {
        val price = it.priceField.parseFloat() ?: return it
        val qty   = it.qtyField.parseFloat()   ?: return it
        val pos = (price * qty / it.availableBalance).coerceIn(0f, 1f)
        return it.copy(sliderPos = pos)
    }
    private fun syncQty(it: TradeUiState): TradeUiState {
        val price = it.priceField.parseFloat() ?: return it
        val qty   = (it.availableBalance * it.sliderPos / price)
        return it.copy(qtyField = qty.noComma(4))
    }

    /* ---------- 下单 --------- */
    fun placeOrder() {
        val price = _ui.value.priceField.parseFloat() ?: return
        val qty   = _ui.value.qtyField.parseFloat()   ?: return
        if (price <= 0f || qty <= 0f) return

        OrderRepository.place(
            symbol = symbol,
            price  = price,
            qty    = qty,
            side   = if (_ui.value.isBuy) Order.Side.BUY else Order.Side.SELL,
            type   = if (_ui.value.orderType == OrderType.LIMIT)
                Order.Type.LIMIT else Order.Type.MARKET
        )
    }
    fun cancelOrder(id: Long) = OrderRepository.cancel(id)
    fun cancelAll()           = OrderRepository.cancelAll(symbol)

    /* ---------- 生成假盘口 ---------- */
    private fun makeBook(mid: Float): List<OrderBookEntry> = List(10) { i ->
        val spread = 2f + i
        OrderBookEntry(
            amount = Random.nextFloat(),
            bid    = mid - spread,
            ask    = mid + spread
        )
    }
}

/* ---------- 简易扩展 ---------- */
private fun String.parseFloat() = replace(",", "").toFloatOrNull()
private fun Float.noComma(dec: Int = 2) = "%.${dec}f".format(Locale.US, this)
