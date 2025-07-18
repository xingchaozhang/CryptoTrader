package com.example.cryptotrader.ui

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cryptotrader.data.Candle
import com.example.cryptotrader.data.PriceUpdate
import com.example.cryptotrader.domain.usecase.GetCandlesUseCase
import com.example.cryptotrader.domain.usecase.SubscribePriceUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getCandlesUseCase: GetCandlesUseCase,
    private val subscribePriceUseCase: SubscribePriceUseCase
) : ViewModel() {
    val symbol: String = savedStateHandle["symbol"] ?: ""

    private val _candles = MutableStateFlow<List<Candle>>(emptyList())
    val candles: StateFlow<List<Candle>> = _candles

    val priceUpdate: StateFlow<PriceUpdate> = subscribePriceUseCase(symbol)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), PriceUpdate(symbol, 0.0, 0.0, 0.0))

    init {
        viewModelScope.launch {
            _candles.value = getCandlesUseCase(symbol)
        }
    }
}
