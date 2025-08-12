package com.example.cryptotrader.data

/**
 * 本地初始化用的默认行情对象。字段全部 val，保证不可变；后续
 * 更新价格时使用 copy() 生成新实例，便于 Compose 正确比较。
 */
data class FakeTicker(
    val symbol: String,
    val price: Double,
    val cnyPrice: Double,
    val changePercent: Double,
    val volume: Double       // “万”为单位
)

/** 默认 6 个币 + 若干备用，仅前 6 个会被 Binance 订阅覆盖。 */
fun defaultFakeTickers(): List<FakeTicker> = listOf(
    FakeTicker("BTC/USDT", 115000.0, 115000.0 * 7.2, 0.0, 120000.0),
    FakeTicker("ETH/USDT", 4200.0, 4200.0 * 7.2, 0.0, 80000.0),
    FakeTicker("BNB/USDT", 800.0, 800.0 * 7.2, 0.0, 40000.0),
    FakeTicker("SOL/USDT", 170.0, 170.0 * 7.2, 0.0, 30000.0),
    FakeTicker("SUI/USDT", 3.6, 3.6 * 7.2, 0.0, 25000.0),
    FakeTicker("ARB/USDT", 0.43, 0.43 * 7.2, 0.0, 26000.0),
    FakeTicker("XLM/USDT", 0.4344, 0.4344 * 7.2, 0.0, 2468.16),
    FakeTicker("ONDO/USDT", 1.0, 1 * 7.2, 0.0, 2309.88)
)
