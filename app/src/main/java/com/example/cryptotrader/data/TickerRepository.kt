package com.example.cryptotrader.data

import android.util.Log
import com.example.cryptotrader.BinancePriceStreamer
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.util.Locale

/**
 * 全局行情仓库：维护 Map<symbol, FakeTicker> 并广播给 UI。
 */
object TickerRepository {
    private const val TAG = "TickerRepo"
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    /* --- Binance 行情流：只订阅 6 个交易对 --- */
    private val streamer = BinancePriceStreamer(
        listOf("BTCUSDT", "ETHUSDT", "BNBUSDT", "SOLUSDT", "SUIUSDT", "ARBUSDT")
    )

    private val _map = MutableStateFlow<Map<String, FakeTicker>>(emptyMap())
    val tickers: StateFlow<Map<String, FakeTicker>> = _map.asStateFlow()

    /** 在 Application.onCreate() 调一次即可 */
    fun start() {
        if (_map.value.isNotEmpty()) return   // 避免重复启动
        _map.value = defaultFakeTickers().associateBy { it.symbol }
        streamer.start()

        scope.launch {
            streamer.tickerFlow.collect { (sym, priceStr) ->
                val price = priceStr.toDoubleOrNull() ?: return@collect
                val key = _map.value.keys.firstOrNull {
                    it.replace("/", "").equals(sym, ignoreCase = true)
                } ?: return@collect

                _map.update { old ->
                    val oldTk = old[key]!!
                    val diff  = if (oldTk.price != 0.0)
                        (price - oldTk.price) / oldTk.price * 100.0 else 0.0

                    val newTk = oldTk.copy(
                        price         = price,
                        cnyPrice      = price * 7.2,
                        changePercent = diff
                    )

                    Log.i(TAG, "✔ $key -> $price (${String.format(Locale.US, "%.2f", diff)}%)")
                    old + (key to newTk)      // 返回全新 Map
                }
            }
        }
    }

    /** 监听单个交易对 */
    fun observe(symbol: String) = tickers
        .map { map ->
            val key = map.keys.firstOrNull {
                it.equals(symbol, true) ||
                        it.replace("/", "").equals(symbol.replace("/", ""), true)
            }
            key?.let { map[it] }
        }
        .distinctUntilChanged()
}
