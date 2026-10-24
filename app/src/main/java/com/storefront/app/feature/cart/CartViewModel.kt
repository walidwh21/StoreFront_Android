package com.storefront.app.feature.cart

import androidx.lifecycle.ViewModel
import com.storefront.app.domain.model.CartLine
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

/**
 * In-memory shopping cart. Quantities are always kept within `1..stock`
 * as known when the product was added, so the cart can't ask for more than exists.
 */
@HiltViewModel
class CartViewModel @Inject constructor() : ViewModel() {

    private val _state = MutableStateFlow(CartUiState())
    val state: StateFlow<CartUiState> = _state.asStateFlow()

    fun dispatch(action: CartAction) {
        _state.update { current ->
            current.copy(lines = reduce(current.lines, action))
        }
    }

    private fun reduce(lines: List<CartLine>, action: CartAction): List<CartLine> = when (action) {
        is CartAction.Add -> {
            val product = action.product
            val existing = lines.firstOrNull { it.product.id == product.id }
            when {
                product.stock <= 0 -> lines
                existing == null -> lines + CartLine(product, 1)
                else -> lines.map {
                    if (it === existing) {
                        // Refresh the product snapshot too, so stock/price stay current.
                        CartLine(product, (it.quantity + 1).coerceAtMost(product.stock))
                    } else it
                }
            }
        }

        is CartAction.Remove -> lines.filterNot { it.product.id == action.productId }

        is CartAction.SetQuantity -> lines.mapNotNull { line ->
            when {
                line.product.id != action.productId -> line
                action.quantity <= 0 -> null
                else -> line.copy(quantity = action.quantity.coerceAtMost(line.product.stock))
            }
        }

        CartAction.Clear -> emptyList()
    }
}
