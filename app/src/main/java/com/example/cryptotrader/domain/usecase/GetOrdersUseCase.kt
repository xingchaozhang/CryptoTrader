package com.example.cryptotrader.domain.usecase

import com.example.cryptotrader.data.local.OrderDao
import com.example.cryptotrader.data.local.OrderEntity
import kotlinx.coroutines.flow.Flow

/**
 * Use case for observing the list of orders.
 */
class GetOrdersUseCase(private val orderDao: OrderDao) {
    operator fun invoke(): Flow<List<OrderEntity>> = orderDao.getOrders()
}
