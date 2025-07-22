package com.example.cryptotrader.di

import android.content.Context
import androidx.room.Room
import com.example.cryptotrader.data.local.AppDatabase
import com.example.cryptotrader.data.local.OrderDao
import com.example.cryptotrader.data.local.WatchlistDao
import com.example.cryptotrader.data.remote.MarketRepository
import com.example.cryptotrader.data.remote.MarketRepositoryImpl
import com.example.cryptotrader.domain.usecase.AddToWatchlistUseCase
import com.example.cryptotrader.domain.usecase.GetCandlesUseCase
import com.example.cryptotrader.domain.usecase.GetOrdersUseCase
import com.example.cryptotrader.domain.usecase.GetWatchlistUseCase
import com.example.cryptotrader.domain.usecase.PlaceOrderUseCase
import com.example.cryptotrader.domain.usecase.RemoveFromWatchlistUseCase
import com.example.cryptotrader.domain.usecase.SubscribePriceUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "trading_db"
        ).build()
    }

    @Provides
    fun provideWatchlistDao(db: AppDatabase): WatchlistDao = db.watchlistDao()

    @Provides
    fun provideOrderDao(db: AppDatabase): OrderDao = db.orderDao()

    @Provides
    @Singleton
    fun provideMarketRepository(
        watchlistDao: WatchlistDao,
        orderDao: OrderDao
    ): MarketRepository = MarketRepositoryImpl(watchlistDao, orderDao)

    // Use cases provisioning
    @Provides
    fun provideSubscribePriceUseCase(repository: MarketRepository): SubscribePriceUseCase =
        SubscribePriceUseCase(repository)

    @Provides
    fun provideGetCandlesUseCase(repository: MarketRepository): GetCandlesUseCase =
        GetCandlesUseCase(repository)

    @Provides
    fun providePlaceOrderUseCase(repository: MarketRepository): PlaceOrderUseCase =
        PlaceOrderUseCase(repository)

    @Provides
    fun provideGetOrdersUseCase(orderDao: OrderDao): GetOrdersUseCase =
        GetOrdersUseCase(orderDao)

    @Provides
    fun provideAddToWatchlistUseCase(watchlistDao: WatchlistDao): AddToWatchlistUseCase =
        AddToWatchlistUseCase(watchlistDao)

    @Provides
    fun provideRemoveFromWatchlistUseCase(watchlistDao: WatchlistDao): RemoveFromWatchlistUseCase =
        RemoveFromWatchlistUseCase(watchlistDao)

    @Provides
    fun provideGetWatchlistUseCase(watchlistDao: WatchlistDao): GetWatchlistUseCase =
        GetWatchlistUseCase(watchlistDao)
}
