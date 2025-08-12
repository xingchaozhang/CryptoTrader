package com.example.cryptotrader.di

import dagger.Module
import dagger.Provides
import dagger.hilt.components.SingletonComponent
import dagger.hilt.InstallIn
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient =
        OkHttpClient.Builder()
            .readTimeout(0, TimeUnit.MILLISECONDS)  // WebSocket 长连
            .pingInterval(20, TimeUnit.SECONDS)    // 定期 ping，防断链
            .build()
}
