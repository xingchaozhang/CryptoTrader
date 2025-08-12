package com.example.cryptotrader.data

import android.util.Log
import com.example.cryptotrader.BinancePriceStreamer
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.util.Locale

/**
 * 全局行情仓库：维护 Map<symbol, FakeTicker> 并广播给 UI。
 * - start() 只会真正启动一次
 * - stop() 可安全停止（取消收集 + 关闭 streamer）
 */
object TickerRepository {
    private const val TAG = "TickerRepo"

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    // 用 Job 记录收集任务，便于 stop()
    private var collectJob: Job? = null

    /* --- Binance 行情流：只订阅 6 个交易对 --- */
    private val streamer = BinancePriceStreamer(
        listOf("BTCUSDT", "ETHUSDT", "BNBUSDT", "SOLUSDT", "SUIUSDT", "ARBUSDT")
    )

    private val _map = MutableStateFlow<Map<String, FakeTicker>>(emptyMap())
    val tickers: StateFlow<Map<String, FakeTicker>> = _map.asStateFlow()

    fun start() {
        if (collectJob?.isActive == true) return
        if (_map.value.isEmpty()) {
            _map.value = defaultFakeTickers().associateBy { it.symbol }
        }

        streamer.start()

        collectJob = streamer.tickerFlow
            .onEach { (sym, priceStr) ->
                val price = priceStr.toDoubleOrNull() ?: return@onEach

                _map.update { old ->
                    val key = old.keys.firstOrNull {
                        it.replace("/", "").equals(sym, ignoreCase = true)
                    } ?: return@update old

                    val oldTk = old[key] ?: return@update old
                    val diff  = if (oldTk.price != 0.0)
                        (price - oldTk.price) / oldTk.price * 100.0 else 0.0

                    Log.i(TAG, "✔ $key -> $price (${String.format(Locale.US, "%.2f", diff)}%)")

                    old + (key to oldTk.copy(
                        price         = price,
                        cnyPrice      = price * 7.2,
                        changePercent = diff
                    ))
                }
            }
            .launchIn(scope)
    }

    fun stop() {
        collectJob?.cancel()
        collectJob = null
        try { streamer.stop() } catch (_: Throwable) {}
    }

    fun restart() {
        stop()
        start()
    }

    /** 监听单个交易对 */
    fun observe(symbol: String): Flow<FakeTicker?> = tickers
        .map { map ->
            val key = map.keys.firstOrNull {
                it.equals(symbol, true) ||
                        it.replace("/", "").equals(symbol.replace("/", ""), true)
            }
            key?.let { map[it] }
        }
        .distinctUntilChanged()
}