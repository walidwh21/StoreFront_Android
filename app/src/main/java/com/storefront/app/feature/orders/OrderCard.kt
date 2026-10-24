package com.storefront.app.feature.orders

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.storefront.app.core.format.asDateTime
import com.storefront.app.core.format.asPrice
import com.storefront.app.core.ui.StatusChip
import com.storefront.app.domain.model.Order

private const val PREVIEW_LINES = 2

@Composable
fun OrderCard(order: Order, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "#${order.orderId.take(8).uppercase()}",
                    style = MaterialTheme.typography.titleMedium
                )
                StatusChip(order.status)
            }
            Text(
                order.orderDate.asDateTime(),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            order.items.take(PREVIEW_LINES).forEach { line ->
                Text(
                    "${line.product.title} × ${line.quantity}",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            val hidden = order.items.size - PREVIEW_LINES
            if (hidden > 0) {
                Text("+ $hidden more", style = MaterialTheme.typography.labelSmall)
            }

            Text(
                "Total ${order.totalAmount.asPrice()}",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
