package com.example.cryptotrader.domain.usecase

import com.example.cryptotrader.data.Candle
import com.example.cryptotrader.data.remote.MarketRepository

/**
 * Use case for retrieving historical candle data for a given crypto pair.
 */
class GetCandlesUseCase(private val repository: MarketRepository) {
    suspend operator fun invoke(symbol: String): List<Candle> = repository.getHistoricalCandles(symbol)
}
