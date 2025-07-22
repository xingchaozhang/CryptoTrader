package com.example.cryptotrader.data

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

/**
 * 单例：维护全局最新价 Map<symbol, FakeTicker>。
 * 现在用默认数据 + 1s 随机波动模拟；接入真实 WS 时在 updateMap() 中写入即可。
 */
object TickerRepository {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val _map = MutableStateFlow<Map<String, FakeTicker>>(emptyMap())
    val tickers: StateFlow<Map<String, FakeTicker>> = _map.asStateFlow()

    fun startMock() {
        if (_map.value.isNotEmpty()) return
        _map.value = defaultFakeTickers().associateBy { it.symbol }

        scope.launch {
            while (isActive) {
                _map.update { old ->
                    old.mapValues { (_, t) -> t.copy().apply { update() } }
                }
                delay(1_000)
            }
        }
    }

    /** 单币对 Flow */
    fun observe(symbol: String): Flow<FakeTicker?> =
        tickers.map { it[symbol] }.distinctUntilChanged()
}