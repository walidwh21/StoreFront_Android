package com.storefront.app.feature.catalog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import com.storefront.app.core.ui.EmptyView
import com.storefront.app.core.ui.ErrorView
import com.storefront.app.core.ui.LoadingView

@Composable
fun StockScreen(
    state: CatalogUiState,
    category: String?,
    brand: String?,
    onRetry: () -> Unit,
    onRestock: (productId: String, amount: Int) -> Unit,
    onMarkSoldOut: (productId: String) -> Unit
) {
    val visible = CatalogFilter(category = category, brand = brand).filter(state.products)

    when {
        state.loading && state.products.isEmpty() -> LoadingView()
        state.error != null && state.products.isEmpty() -> ErrorView(state.error, onRetry)
        visible.isEmpty() -> EmptyView("No products match the selected filters")
        else -> LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(visible, key = { it.id }) { product ->
                StockRow(
                    product = product,
                    onRestock = { amount -> onRestock(product.id, amount) },
                    onMarkSoldOut = { onMarkSoldOut(product.id) }
                )
            }
        }
    }
}
