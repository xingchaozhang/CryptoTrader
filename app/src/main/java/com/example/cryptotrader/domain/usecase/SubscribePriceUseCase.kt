package com.example.cryptotrader.domain.usecase

import com.example.cryptotrader.data.PriceUpdate
import com.example.cryptotrader.data.remote.MarketRepository
import kotlinx.coroutines.flow.Flow

/**
 * Use case for subscribing to price updates for a given crypto pair.
 */
class SubscribePriceUseCase(private val repository: MarketRepository) {
    operator fun invoke(symbol: String): Flow<PriceUpdate> = repository.subscribePriceStream(symbol)
}
