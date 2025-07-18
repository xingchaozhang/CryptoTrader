package com.example.cryptotrader.domain.usecase

import com.example.cryptotrader.data.local.WatchlistDao
import com.example.cryptotrader.data.local.WatchlistEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Use case for removing a crypto pair from the watchlist.
 */
class RemoveFromWatchlistUseCase(private val dao: WatchlistDao) {
    // RemoveFromWatchlistUseCase.kt
    suspend operator fun invoke(entity: WatchlistEntity) {
        withContext(Dispatchers.IO) {
            dao.delete(entity)
        }
    }
}
