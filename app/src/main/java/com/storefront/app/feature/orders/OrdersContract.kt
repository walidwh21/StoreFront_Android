package com.storefront.app.feature.orders

import com.storefront.app.domain.model.Order

data class OrdersUiState(
    val orders: List<Order> = emptyList(),
    val loading: Boolean = false,
    val loadError: String? = null,
    /** True while a checkout is in flight. */
    val placing: Boolean = false,
    val placeError: String? = null,
    /** Set once when an order has been saved; the UI acknowledges it to clear it. */
    val placedOrderId: String? = null
)

sealed interface OrdersAction {
    data class Load(val userId: String) : OrdersAction
    data class Place(val order: Order) : OrdersAction
    data object AckPlaced : OrdersAction
    data object DismissPlaceError : OrdersAction
}
