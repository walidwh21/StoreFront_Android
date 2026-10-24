package com.storefront.app.feature.orders

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.storefront.app.core.util.attempt
import com.storefront.app.core.util.userMessage
import com.storefront.app.data.OrderRepository
import com.storefront.app.domain.model.Order
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OrdersViewModel @Inject constructor(
    private val repository: OrderRepository
) : ViewModel() {

    private val _state = MutableStateFlow(OrdersUiState())
    val state: StateFlow<OrdersUiState> = _state.asStateFlow()

    fun dispatch(action: OrdersAction) {
        when (action) {
            is OrdersAction.Load -> load(action.userId)
            is OrdersAction.Place -> place(action.order)
            OrdersAction.AckPlaced -> _state.update { it.copy(placedOrderId = null) }
            OrdersAction.DismissPlaceError -> _state.update { it.copy(placeError = null) }
        }
    }

    private fun load(userId: String) = viewModelScope.launch {
        _state.update { it.copy(loading = true, loadError = null) }
        attempt { repository.fetchForUser(userId) }
            .onSuccess { list -> _state.update { it.copy(loading = false, orders = list) } }
            .onFailure { e ->
                _state.update { it.copy(loading = false, loadError = e.userMessage("Could not load orders.")) }
            }
    }

    private fun place(order: Order) {
        if (_state.value.placing) return          // ignore double taps
        viewModelScope.launch {
            _state.update { it.copy(placing = true, placeError = null) }
            attempt { repository.place(order) }
                .onSuccess {
                    _state.update {
                        it.copy(
                            placing = false,
                            orders = listOf(order) + it.orders,
                            placedOrderId = order.orderId
                        )
                    }
                }
                .onFailure { e ->
                    _state.update {
                        it.copy(placing = false, placeError = e.userMessage("Could not place the order."))
                    }
                }
        }
    }
}
