package com.example.cryptotrader.ui

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cryptotrader.data.OrderSide
import com.example.cryptotrader.data.OrderType
import com.example.cryptotrader.data.local.OrderEntity
import com.example.cryptotrader.domain.usecase.PlaceOrderUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel backing the order entry screen.
 */
@HiltViewModel
class OrderViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val placeOrderUseCase: PlaceOrderUseCase
) : ViewModel() {
    val symbol: String = savedStateHandle["symbol"] ?: ""

    private val _price = MutableStateFlow(0.0)
    val price: StateFlow<Double> = _price
    private val _amount = MutableStateFlow(0.0)
    val amount: StateFlow<Double> = _amount
    private val _side = MutableStateFlow(OrderSide.BUY)
    val side: StateFlow<OrderSide> = _side
    private val _type = MutableStateFlow(OrderType.MARKET)
    val type: StateFlow<OrderType> = _type

    private val _statusMessage = MutableStateFlow("")
    val statusMessage: StateFlow<String> = _statusMessage

    fun setPrice(value: String) {
        _price.value = value.toDoubleOrNull() ?: 0.0
    }
    fun setAmount(value: String) {
        _amount.value = value.toDoubleOrNull() ?: 0.0
    }
    fun setSide(newSide: OrderSide) {
        _side.value = newSide
    }
    fun setType(newType: OrderType) {
        _type.value = newType
    }

    fun confirmOrder() {
        viewModelScope.launch {
            val order = OrderEntity(
                symbol = symbol,
                price = price.value,
                amount = amount.value,
                side = side.value.name,
                type = type.value.name,
                status = "Filled"
            )
            placeOrderUseCase(order)
            _statusMessage.value = "Order placed!"
        }
    }
}
