package com.example.cryptotrader.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cryptotrader.data.PriceUpdate
import com.example.cryptotrader.data.local.WatchlistEntity
import com.example.cryptotrader.domain.usecase.GetWatchlistUseCase
import com.example.cryptotrader.domain.usecase.SubscribePriceUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import javax.inject.Inject

/**
 * ViewModel responsible for producing a list of watched crypto pairs along with their current price updates.
 */
@HiltViewModel
class MarketOverviewViewModel @Inject constructor(
    private val getWatchlistUseCase: GetWatchlistUseCase,
    private val subscribePriceUseCase: SubscribePriceUseCase
) : ViewModel() {

    data class PairWithPrice(val entity: WatchlistEntity, val priceUpdate: PriceUpdate?)

    // StateFlow combining watchlist and price updates
    val pairsWithPrices: StateFlow<List<PairWithPrice>> =
        getWatchlistUseCase().flatMapLatest { list ->
            if (list.isEmpty()) {
                flowOf(emptyList())
            } else {
                val priceFlows: List<Flow<Pair<String, PriceUpdate>>> = list.map { pair ->
                    subscribePriceUseCase(pair.symbol)
                        .map { update -> pair.symbol to update }
                        .onStart { emit(pair.symbol to PriceUpdate(pair.symbol, 0.0, 0.0, 0.0)) }
                }
                combine(priceFlows) { arrayOfPairs ->
                    val map = arrayOfPairs.associate { it.first to it.second }
                    list.map { PairWithPrice(it, map[it.symbol]) }
                }
            }
        }
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

}
