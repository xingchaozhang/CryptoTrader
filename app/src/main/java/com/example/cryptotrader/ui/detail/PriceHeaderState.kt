package com.example.cryptotrader.ui.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cryptotrader.data.TickerRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

data class PriceHeaderState(
    val close: Float,
    val open: Float,
    val high24h: Float,
    val low24h: Float,
    val cny: Int,
    val pct: Float
)

class SpotDetailViewModel : ViewModel() {

    private val _pairSymbolSlash = MutableStateFlow("")
    val pairSymbolSlash: StateFlow<String> = _pairSymbolSlash

    private val _period = MutableStateFlow(CandlePeriod.D1)
    val period: StateFlow<CandlePeriod> = _period

    private val _candles = MutableStateFlow<List<Candle>>(emptyList())
    val candles: StateFlow<List<Candle>> = _candles

    private val _orderBook = MutableStateFlow<List<OrderBookEntry>>(emptyList())
    val orderBook: StateFlow<List<OrderBookEntry>> = _orderBook

    private val _header = MutableStateFlow<PriceHeaderState?>(null)
    val header: StateFlow<PriceHeaderState?> = _header

    private var priceJob: Job? = null
    private var bookJob: Job? = null

    /** 进入页面/切换币对时调用 */
    fun start(symbol: String) {
        val slash = normalizeToSlash(symbol)
        _pairSymbolSlash.value = slash

        priceJob?.cancel()
        bookJob?.cancel()

        priceJob = viewModelScope.launch {
            val priceFlow = TickerRepository.observe(slash).filterNotNull()
            val seed = priceFlow.first().price.toFloat()

            _candles.value = genInitialCandles(seed, _period.value)
            _orderBook.value = defaultOrderBookAround(seed)
            recomputeHeader()

            priceFlow.collect { tk ->
                val p = tk.price.toFloat()
                _candles.value = updateByTick(
                    _candles.value, _period.value, System.currentTimeMillis(), p
                )
                recomputeHeader()
            }
        }

        bookJob = viewModelScope.launch {
            while (true) {
                _orderBook.value = jitterOrderBook(_orderBook.value)
                delay(500)
            }
        }
    }

    /** 切换周期 */
    fun setPeriodTab(index: Int) {
        val p = when (index) {
            0 -> CandlePeriod.M15
            1 -> CandlePeriod.H1
            2 -> CandlePeriod.H4
            else -> CandlePeriod.D1
        }
        if (_period.value == p) return
        _period.value = p

        val anchor = _candles.value.lastOrNull()?.close ?: 100f
        _candles.value = genInitialCandles(anchor, p)
        recomputeHeader()
    }

    private fun recomputeHeader() {
        val c = _candles.value.lastOrNull() ?: return
        val hi = _candles.value.maxOf { it.high }
        val lo = _candles.value.minOf { it.low }
        val pct = (c.close - c.open) / c.open * 100f
        _header.value = PriceHeaderState(
            close = c.close,
            open = c.open,
            high24h = hi,
            low24h = lo,
            cny = (c.close * 7.18f).toInt(),
            pct = pct
        )
    }

    private fun normalizeToSlash(symbol: String): String {
        return if (symbol.contains("/")) symbol.uppercase()
        else {
            val quotes = listOf("USDT", "BUSD", "FDUSD", "USDC")
            val up = symbol.uppercase()
            val quote = quotes.firstOrNull { up.endsWith(it) }
            if (quote != null) up.dropLast(quote.length) + "/" + quote else up
        }
    }

    override fun onCleared() {
        priceJob?.cancel()
        bookJob?.cancel()
        super.onCleared()
    }
}