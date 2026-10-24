package com.storefront.app.feature.catalog

import com.storefront.app.domain.model.Product

/** What the shopper has narrowed the list down to. `null` means "no restriction". */
data class CatalogFilter(
    val query: String = "",
    val category: String? = null,
    val brand: String? = null
) {
    fun matches(product: Product): Boolean {
        val categoryOk = category == null || product.category == category
        val brandOk = brand == null || product.brand == brand
        val q = query.trim()
        val queryOk = q.isEmpty() ||
            product.title.contains(q, ignoreCase = true) ||
            product.category.contains(q, ignoreCase = true) ||
            product.brand.contains(q, ignoreCase = true)
        return categoryOk && brandOk && queryOk
    }

    fun filter(products: List<Product>): List<Product> = products.filter { matches(it) }
}
