package com.example.cryptotrader.data

import android.util.Log
import com.example.cryptotrader.data.remote.BinancePriceStreamer
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

/**
 * 单例：维护全局最新价 Map<symbol, FakeTicker>。
 * 使用 [BinancePriceStreamer] 提供的实时逐笔数据更新价格。
 */
object TickerRepository {

    private const val TAG = "TickerRepo"
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val streamer = BinancePriceStreamer(
        defaultFakeTickers().map { it.symbol.replace("/", "") }.take(6)
    )

    private val _map = MutableStateFlow<Map<String, FakeTicker>>(emptyMap())
    val tickers: StateFlow<Map<String, FakeTicker>> = _map.asStateFlow()

    fun start() {
        if (_map.value.isNotEmpty()) return
        Log.d(TAG, "start")
        _map.value = defaultFakeTickers().associateBy { it.symbol }

        streamer.start()
        scope.launch {
            streamer.tickerFlow.collect { (sym, priceStr) ->
                val price = priceStr.toDoubleOrNull() ?: return@collect
                val key = _map.value.keys.firstOrNull {
                    it.replace("/", "").equals(sym, ignoreCase = true)
                } ?: return@collect
                _map.update { old ->
                    val oldTicker = old[key]!!
                    val diff = if (oldTicker.price != 0.0)
                        (price - oldTicker.price) / oldTicker.price * 100.0 else 0.0
                    val newTicker = oldTicker.copy().apply {
                        this.price = price
                        this.cnyPrice = price * 7.2
                        this.changePercent = diff
                    }
                    old + (key to newTicker)
                }
            }
        }
    }

    /** 单币对 Flow */
    fun observe(symbol: String): Flow<FakeTicker?> =
        tickers.map { it[symbol] }.distinctUntilChanged()
}