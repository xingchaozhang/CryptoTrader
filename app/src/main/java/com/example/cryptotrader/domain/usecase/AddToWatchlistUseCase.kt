package com.example.cryptotrader.domain.usecase

import com.example.cryptotrader.data.local.WatchlistDao
import com.example.cryptotrader.data.local.WatchlistEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Use case for adding a crypto pair to the user's watchlist.
 */
class AddToWatchlistUseCase(private val dao: WatchlistDao) {
    suspend operator fun invoke(symbol: String, name: String) {
        withContext(Dispatchers.IO) {
            dao.insert(WatchlistEntity(symbol, name))
        }
    }
}
