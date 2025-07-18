package com.example.cryptotrader.domain.usecase

import com.example.cryptotrader.data.local.OrderEntity
import com.example.cryptotrader.data.remote.MarketRepository

/**
 * Use case for placing a mock order.
 */
class PlaceOrderUseCase(private val repository: MarketRepository) {
    suspend operator fun invoke(order: OrderEntity) {
        repository.placeOrder(order)
    }
}
