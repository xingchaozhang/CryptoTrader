package com.example.cryptotrader

import android.app.Application
import android.util.Log
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.example.cryptotrader.data.FavoritesRepository
import com.example.cryptotrader.data.TickerRepository
import com.example.cryptotrader.data.local.ApiKeyStorage
import dagger.hilt.android.HiltAndroidApp     // ← 新增

@HiltAndroidApp
class TradingApplication : Application(), DefaultLifecycleObserver {
    private val TAG = "TradingApplication"

    override fun onCreate() {
        super<Application>.onCreate()
        Log.d(TAG, "onCreate")
        FavoritesRepository.init(this)
        // 启动实时行情流
        TickerRepository.start()
        // 初始化安全存储并保存演示用的 API 凭证
        ApiKeyStorage.init(this)
        ApiKeyStorage.saveCredentials("demo_key", "demo_secret")
    }

    override fun onStart(owner: LifecycleOwner) {
        TickerRepository.start()
    }

    override fun onStop(owner: LifecycleOwner) {
        TickerRepository.stop()
    }
}