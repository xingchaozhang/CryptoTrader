package com.example.cryptotrader.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cryptotrader.data.Binance24hTickerWs
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject
import dagger.hilt.android.lifecycle.HiltViewModel

data class TickerUiState(
    val symbol: String = "",
    val lastPrice: String? = null,
    val changePercent: String? = null, // 不含 % 的字符串
    val error: String? = null
)

@HiltViewModel
class TickerTestViewModel @Inject constructor(
    private val ws: Binance24hTickerWs
) : ViewModel() {

    private val _state = MutableStateFlow(TickerUiState())
    val state: StateFlow<TickerUiState> = _state

    private var job: Job? = null

    fun start(symbol: String) {
        if (_state.value.symbol.equals(symbol, ignoreCase = true) && job != null) return
        job?.cancel()

        _state.value = TickerUiState(symbol = symbol.uppercase(), lastPrice = null, changePercent = null, error = null)

        job = viewModelScope.launch {
            ws.streamSpot(symbol)
                .onEach {
                    _state.value = _state.value.copy(
                        lastPrice = it.lastPrice,
                        changePercent = it.changePercent,
                        error = null
                    )
                }
                .catch { e ->
                    _state.value = _state.value.copy(error = (e.message ?: "WebSocket error"))
                }
                .collect { /* handled in onEach */ }
        }
    }
}
