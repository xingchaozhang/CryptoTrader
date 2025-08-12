package com.example.cryptotrader.ui.screens.trade

import androidx.compose.runtime.Immutable
import com.example.cryptotrader.data.Order
import com.example.cryptotrader.ui.detail.OrderBookEntry

@Immutable
data class TradeUiState(
    val symbol: String = "",
    val latestPrice: Float = 0f,

    val priceField: String = "",
    val qtyField: String = "",
    val sliderPos: Float = 0f,               // 0‒1

    val orderType: OrderType = OrderType.LIMIT,
    val isBuy: Boolean = true,
    val stopLossEnabled: Boolean = false,

    val availableBalance: Float = 1_000f,
    val loggedIn: Boolean = true,

    val orderBook: List<OrderBookEntry> = emptyList(),
    val allOrders: List<Order> = emptyList()
) {
    val amount: Float
        get() = (priceField.replace(",", "").toFloatOrNull() ?: 0f) *
                (qtyField.replace(",", "").toFloatOrNull() ?: 0f)
}

enum class OrderType { LIMIT, MARKET }