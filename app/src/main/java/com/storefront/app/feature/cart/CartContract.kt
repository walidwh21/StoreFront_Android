package com.storefront.app.feature.cart

import com.storefront.app.domain.model.CartLine
import com.storefront.app.domain.model.Product

data class CartUiState(
    val lines: List<CartLine> = emptyList()
) {
    val isEmpty: Boolean get() = lines.isEmpty()
    val itemCount: Int get() = lines.sumOf { it.quantity }
    val total: Double get() = lines.sumOf { it.lineTotal }
}

sealed interface CartAction {
    data class Add(val product: Product) : CartAction
    data class Remove(val productId: String) : CartAction
    data class SetQuantity(val productId: String, val quantity: Int) : CartAction
    data object Clear : CartAction
}
