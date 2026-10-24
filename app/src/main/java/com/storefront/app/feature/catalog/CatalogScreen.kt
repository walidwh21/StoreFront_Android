package com.storefront.app.feature.catalog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.storefront.app.core.ui.EmptyView
import com.storefront.app.core.ui.ErrorView
import com.storefront.app.core.ui.LoadingView
import com.storefront.app.domain.model.Product

@Composable
fun CatalogScreen(
    state: CatalogUiState,
    category: String?,
    brand: String?,
    onRetry: () -> Unit,
    onOpenProduct: (String) -> Unit,
    onAddToCart: (Product) -> Unit
) {
    var query by rememberSaveable { mutableStateOf("") }

    when {
        state.loading && state.products.isEmpty() -> LoadingView()
        state.error != null && state.products.isEmpty() -> ErrorView(state.error, onRetry)
        else -> Column(Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                placeholder = { Text("Search by name, category or brand") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp)
            )

            val visible = CatalogFilter(query, category, brand).filter(state.products)
            if (visible.isEmpty()) {
                EmptyView("No products match your filters")
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    items(visible, key = { it.id }) { product ->
                        ProductCard(
                            product = product,
                            onOpen = { onOpenProduct(product.id) },
                            onAddToCart = { onAddToCart(product) }
                        )
                    }
                }
            }
        }
    }
}
