package com.example.cryptotrader.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.mikephil.charting.data.Entry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import kotlin.random.Random

class TradingViewModel : ViewModel() {
    private val _entries = MutableStateFlow<List<Entry>>(emptyList())
    val entries: StateFlow<List<Entry>> = _entries

    private val _latestPrice = MutableStateFlow(0f)
    val latestPrice: StateFlow<Float> = _latestPrice

    init {
        startGeneratingPrices()
    }

    private fun startGeneratingPrices() {
        viewModelScope.launch(Dispatchers.Default) {
            var price = 1000f
            var time = 0f
            val points = mutableListOf<Entry>()
            while (true) {
                price += Random.nextFloat() * 10f - 5f
                time += 1f
                points.add(Entry(time, price))
                if (points.size > 100) points.removeAt(0)
                _latestPrice.value = price
                _entries.value = points.toList()
                delay(500)
            }
        }
    }
}
