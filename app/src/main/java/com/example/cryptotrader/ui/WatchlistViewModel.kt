package com.example.cryptotrader.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cryptotrader.data.local.WatchlistEntity
import com.example.cryptotrader.domain.usecase.AddToWatchlistUseCase
import com.example.cryptotrader.domain.usecase.GetWatchlistUseCase
import com.example.cryptotrader.domain.usecase.RemoveFromWatchlistUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel providing access to the user's watchlist and operations to modify it.
 */
@HiltViewModel
class WatchlistViewModel @Inject constructor(
    private val getWatchlistUseCase: GetWatchlistUseCase,
    private val addToWatchlistUseCase: AddToWatchlistUseCase,
    private val removeFromWatchlistUseCase: RemoveFromWatchlistUseCase
) : ViewModel() {
    val watchlist: StateFlow<List<WatchlistEntity>> = getWatchlistUseCase()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    fun addPair(symbol: String, name: String) {
        viewModelScope.launch {
            addToWatchlistUseCase(symbol.uppercase(), name)
        }
    }

    fun removePair(entity: WatchlistEntity) {
        viewModelScope.launch {
            removeFromWatchlistUseCase(entity)
        }
    }
}
