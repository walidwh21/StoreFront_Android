package com.storefront.app.core.ui

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.storefront.app.core.format.asPrice
import com.storefront.app.domain.model.Product
import com.storefront.app.domain.model.discount
import com.storefront.app.domain.model.finalPrice
import com.storefront.app.domain.model.hasDiscount

/** Final price, plus the struck-through list price and percentage when discounted. */
@Composable
fun PriceTag(product: Product, modifier: Modifier = Modifier) {
    Row(modifier, verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = product.finalPrice.asPrice(),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary
        )
        if (product.hasDiscount) {
            Spacer(Modifier.width(8.dp))
            Text(
                text = product.price.asPrice(),
                style = MaterialTheme.typography.bodySmall.copy(textDecoration = TextDecoration.LineThrough),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.width(6.dp))
            Text(
                text = "-${product.discount.toInt()}%",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.secondary
            )
        }
    }
}
