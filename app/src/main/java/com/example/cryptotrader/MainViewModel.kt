package com.example.cryptotrader

import androidx.lifecycle.ViewModel

class MainViewModel : ViewModel() {
    private val streamer = BinancePriceStreamer()

    val priceFlow = streamer.price          // UI 直接 collectAsState

    init {
        streamer.start()
    }

    override fun onCleared() {
        streamer.stop()
    }
}
