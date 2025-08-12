package com.example.cryptotrader.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cryptotrader.data.FavoritesRepository
import com.example.cryptotrader.data.Ticker24h
import com.example.cryptotrader.data.Ticker24hRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.Locale
import javax.inject.Inject

data class TickerRowUi(
    val symbol: String,     // 展示：BTC/USDT
    val last: String,       // 最新价
    val changeText: String, // +0.23%
    val up: Boolean,
    val volumeText: String, // 成交额（万）
    val raw: Ticker24h? = null
)

@HiltViewModel
class TickerListViewModel @Inject constructor(private val repo24h: Ticker24hRepository) : ViewModel() {

    private val symbolsFlow = MutableStateFlow<List<String>>(emptyList())

    val rows: StateFlow<List<TickerRowUi>> =
        combine(symbolsFlow, repo24h.map) { syms, map ->
            syms.map { s ->
                val key = s.uppercase(Locale.US)
                val t = map[key]
                toRowUi(key, t)
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun setSymbols(symbols: List<String>) {
        viewModelScope.launch {
            symbolsFlow.emit(symbols)
            repo24h.start(symbols)
        }
        favoritesJob?.cancel()
        favoritesJob = null                 // 手动模式，停止跟随收藏
        if (symbols == currentSymbols) return   // 幂等：相同列表就不重建订阅
        currentSymbols = symbols
    }

    private fun toRowUi(symbolU: String, t: Ticker24h?): TickerRowUi {
        val display = toPair(symbolU)
        val last = t?.lastPrice ?: "--"
        val pctStr = t?.changePercent ?: "0"
        val up = pctStr.toBigDecimalOrNull()?.let { it >= BigDecimal.ZERO } ?: true
        val pctText = (if (up) "+" else "") + (pctStr.toBigDecimalOrNull()
            ?.setScale(2, RoundingMode.HALF_UP)?.toPlainString() ?: "0.00") + "%"

        val volWan = t?.quoteVolume?.let { formatWan(it) } ?: "--"

        return TickerRowUi(
            symbol = display,
            last = last,
            changeText = pctText,
            up = up,
            volumeText = volWan,
            raw = t
        )
    }

    private fun toPair(s: String): String =
        if (s.uppercase(Locale.US).endsWith("USDT")) s.dropLast(4) + "/USDT" else s

    private fun formatWan(q: String): String {
        val bd = q.toBigDecimalOrNull() ?: return "--"
        val wan = bd.divide(BigDecimal("10000"))
        return "%,.2f万".format(Locale.US, wan)
    }

    fun useFavorites() {
        if (favoritesJob != null) return   // 已经在跟随收藏，无需重复
        favoritesJob = viewModelScope.launch {
            FavoritesRepository.symbols
                .map { set -> set.map { it.replace("/", "") } }  // BTC/USDT -> BTCUSDT
                .distinctUntilChanged()
                .collect { list ->
                    // 收藏集合变化才切换订阅
                    if (list != currentSymbols) {
                        currentSymbols = list
                        // 复用你原来的逻辑
                        setSymbols(list)
                    }
                }
        }
    }
}


private var currentSymbols: List<String> = emptyList()
private var favoritesJob: Job? = null

/**
 * 让 VM 跟随收藏（FavoritesRepository.symbols）。
 * 只在第一次调用时生效，后面重复调用不产生额外订阅。
 */

private fun String.toBigDecimalOrNull(): BigDecimal? = try {
    BigDecimal(this)
} catch (_: Throwable) {
    null
}