package com.example.cryptotrader.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cryptotrader.data.local.OrderEntity
import com.example.cryptotrader.domain.usecase.GetOrdersUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

/**
 * ViewModel exposing the list of mock orders.
 */
@HiltViewModel
class OrderHistoryViewModel @Inject constructor(
    getOrdersUseCase: GetOrdersUseCase
) : ViewModel() {
    val orders: StateFlow<List<OrderEntity>> = getOrdersUseCase()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
}
