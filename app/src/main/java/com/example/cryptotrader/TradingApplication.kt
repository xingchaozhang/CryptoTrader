package com.example.cryptotrader

import android.app.Application
import com.example.cryptotrader.data.FavoritesRepository
import com.example.cryptotrader.data.TickerRepository
import dagger.hilt.android.HiltAndroidApp

/**
 * Custom application class required for Hilt initialization.
 */
@HiltAndroidApp
class TradingApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        FavoritesRepository.init(this)
        TickerRepository.startMock()            // 启动行情流// 初始化 Room
    }
}
