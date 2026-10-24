package com.storefront.app.domain.model

/** One product and how many of it the shopper wants. Lives in memory only. */
data class CartLine(
    val product: Product,
    val quantity: Int
) {
    val lineTotal: Double get() = product.finalPrice * quantity
}
