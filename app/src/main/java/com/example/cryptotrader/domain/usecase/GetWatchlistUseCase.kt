package com.example.cryptotrader.domain.usecase

import com.example.cryptotrader.data.local.WatchlistDao
import com.example.cryptotrader.data.local.WatchlistEntity
import kotlinx.coroutines.flow.Flow

/**
 * Use case for retrieving the current watchlist as a Flow.
 */
class GetWatchlistUseCase(private val dao: WatchlistDao) {
    operator fun invoke(): Flow<List<WatchlistEntity>> = dao.getAll()
}
