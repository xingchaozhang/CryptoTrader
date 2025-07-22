package com.example.cryptotrader.data

import kotlin.random.Random

/**
 * Represents a simulated ticker item used for the mock “Spot → All” market
 * screen. Each instance holds a base price and volume from which random
 * fluctuations are generated. The [update] function applies a small random
 * delta to the price and updates the computed CNY price, volume and change percent.
 */
data class FakeTicker(
    val symbol: String,
    private val basePrice: Double,
    private val baseCnyPrice: Double,
    /** Trading volume in units of “万”. Mutable so it can drift during simulation. */
    var volume: Double
) {
    var price: Double = basePrice
    var cnyPrice: Double = baseCnyPrice
    var changePercent: Double = 0.0

    /** Randomly adjust price ±0.5% and volume ±1% each tick. */
    fun update() {
        val deltaPrice = Random.nextDouble(-0.005, 0.005)
        price *= (1.0 + deltaPrice)
        cnyPrice = price * 7.2
        changePercent = ((price - basePrice) / basePrice) * 100.0

        val deltaVol = Random.nextDouble(-0.01, 0.01)
        volume *= (1.0 + deltaVol)
    }
}

/**
 * Returns a list of simulated tickers. The major pairs are listed first.
 */
fun defaultFakeTickers(): List<FakeTicker> = listOf(
    FakeTicker("BTC/USDT", basePrice = 65000.0, baseCnyPrice = 65000.0 * 7.2, volume = 120000.0),
    FakeTicker("ETH/USDT", basePrice = 3500.0, baseCnyPrice = 3500.0 * 7.2, volume = 80000.0),
    FakeTicker("BNB/USDT", basePrice = 550.0, baseCnyPrice = 550.0 * 7.2, volume = 40000.0),
    FakeTicker("SOL/USDT", basePrice = 160.0, baseCnyPrice = 160.0 * 7.2, volume = 30000.0),

    FakeTicker("CRV/USDT", basePrice = 0.9796, baseCnyPrice = 7.04, volume = 2978.53),
    FakeTicker("ARB/USDT", basePrice = 0.4620, baseCnyPrice = 3.32, volume = 2602.52),
    FakeTicker("XLM/USDT", basePrice = 0.4644, baseCnyPrice = 3.33, volume = 2468.16),
    FakeTicker("ONDO/USDT", basePrice = 1.0236, baseCnyPrice = 7.35, volume = 2309.88),
    FakeTicker("AAVE/USDT", basePrice = 318.77, baseCnyPrice = 2291.15, volume = 2277.15),
    FakeTicker("AVAX/USDT", basePrice = 23.88, baseCnyPrice = 171.63, volume = 2273.19),
    FakeTicker("APT/USDT", basePrice = 5.326, baseCnyPrice = 38.35, volume = 2000.00)
)