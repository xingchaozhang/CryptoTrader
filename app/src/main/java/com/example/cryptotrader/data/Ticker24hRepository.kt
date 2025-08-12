package com.example.cryptotrader.data

import com.example.cryptotrader.di.ApplicationScope
import com.example.cryptotrader.di.IODispatcher
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class Ticker24hRepository @Inject constructor(
    private val ws: Binance24hTickerWs,
    @ApplicationScope private val appScope: CoroutineScope,
    @IODispatcher private val io: CoroutineDispatcher
) {
    private val _map = MutableStateFlow<Map<String, Ticker24h>>(emptyMap())
    val map: StateFlow<Map<String, Ticker24h>> = _map.asStateFlow()

    private var job: Job? = null
    private var current: Set<String> = emptySet()

    @Synchronized
    fun start(symbols: Collection<String>) {
        val wanted = symbols.map { it.uppercase(Locale.US) }.toSet()
        if (wanted.isEmpty()) { stop(); return }
        if (wanted == current && job?.isActive == true) return

        stop()
        current = wanted
        job = ws.streamSpotCombined(wanted.toList())
            .flowOn(io)
            .onEach { t ->
                _map.update { old ->
                    val m = HashMap(old)
                    m[t.symbol] = t
                    m
                }
            }
            .launchIn(appScope)
    }

    @Synchronized
    fun stop() {
        job?.cancel(); job = null
        current = emptySet()
        _map.value = emptyMap()
    }

    fun observe(symbol: String): Flow<Ticker24h?> =
        map.map { it[symbol.uppercase(Locale.US)] }.distinctUntilChanged()
}