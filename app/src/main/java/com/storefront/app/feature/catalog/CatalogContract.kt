package com.storefront.app.feature.catalog

import com.storefront.app.domain.model.Product

data class CatalogUiState(
    val loading: Boolean = false,
    val products: List<Product> = emptyList(),
    val error: String? = null
) {
    val categories: List<String>
        get() = products.map { it.category }.filter { it.isNotBlank() }.distinct().sorted()

    val brands: List<String>
        get() = products.map { it.brand }.filter { it.isNotBlank() }.distinct().sorted()

    fun byId(id: String): Product? = products.firstOrNull { it.id == id }
}

sealed interface CatalogAction {
    /** Reload the whole catalogue. */
    data object Refresh : CatalogAction

    /** Fetch one product fresh (used by the detail screen so stock is current). */
    data class Open(val productId: String) : CatalogAction

    data class Restock(val productId: String, val amount: Int) : CatalogAction

    data class MarkSoldOut(val productId: String) : CatalogAction
}
