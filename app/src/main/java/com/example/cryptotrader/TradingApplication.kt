package com.example.cryptotrader

import android.app.Application
import android.util.Log
import com.example.cryptotrader.data.FavoritesRepository
import com.example.cryptotrader.data.TickerRepository
import com.example.cryptotrader.data.local.ApiKeyStorage
import dagger.hilt.android.HiltAndroidApp

/**
 * Custom application class required for Hilt initialization.
 */
@HiltAndroidApp
class TradingApplication : Application() {
    companion object {
        private const val TAG = "TradingApp"
    }
    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "onCreate")
        FavoritesRepository.init(this)
        TickerRepository.start()            // 启动行情流// 初始化 Room
        // Initialize secure storage and save demo credentials
        ApiKeyStorage.init(this)
        ApiKeyStorage.saveCredentials("demo_key", "demo_secret")
    }
}
