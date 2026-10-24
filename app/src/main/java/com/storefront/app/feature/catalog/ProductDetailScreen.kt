package com.storefront.app.feature.catalog

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.storefront.app.core.format.asRating
import com.storefront.app.core.ui.ErrorView
import com.storefront.app.core.ui.LoadingView
import com.storefront.app.core.ui.PriceTag
import com.storefront.app.domain.model.Product
import com.storefront.app.domain.model.gallery
import com.storefront.app.domain.model.isSoldOut

/**
 * @param product the product to show, or null while it is still being fetched
 * @param error shown only when there is nothing cached to display
 */
@Composable
fun ProductDetailScreen(
    product: Product?,
    error: String?,
    onAddToCart: (Product) -> Unit
) {
    when {
        product != null -> DetailContent(product, onAddToCart)
        error != null -> ErrorView(error)
        else -> LoadingView()
    }
}

@Composable
private fun DetailContent(product: Product, onAddToCart: (Product) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        ProductGallery(images = product.gallery)
        Spacer(Modifier.height(16.dp))

        Text(
            text = product.title,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(6.dp))
        PriceTag(product)

        Spacer(Modifier.height(12.dp))
        Row {
            Text("★ ${product.rating.asRating()}", style = MaterialTheme.typography.bodyMedium)
            Spacer(Modifier.width(16.dp))
            Text(
                text = if (product.isSoldOut) "Sold out" else "${product.stock} in stock",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = if (product.isSoldOut) MaterialTheme.colorScheme.error
                else MaterialTheme.colorScheme.onSurface
            )
        }

        product.description?.takeIf { it.isNotBlank() }?.let {
            Spacer(Modifier.height(12.dp))
            Text(it, style = MaterialTheme.typography.bodyMedium)
        }

        Spacer(Modifier.height(12.dp))
        if (product.brand.isNotBlank()) {
            Text("Brand: ${product.brand}", style = MaterialTheme.typography.bodyMedium)
        }
        Text("Category: ${product.category}", style = MaterialTheme.typography.bodyMedium)

        Spacer(Modifier.height(20.dp))
        Button(
            onClick = { onAddToCart(product) },
            enabled = !product.isSoldOut,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.ShoppingCart, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("Add to cart")
        }
    }
}
