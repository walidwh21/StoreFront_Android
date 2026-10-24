package com.storefront.app.feature.orders

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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

@Composable
fun OrderDetail(order: Order, onBack: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Order details", style = MaterialTheme.typography.headlineSmall)
            StatusChip(order.status)
        }
        Text("Reference: ${order.orderId}", style = MaterialTheme.typography.bodySmall)
        Text("Placed: ${order.orderDate.asDateTime()}", style = MaterialTheme.typography.bodyMedium)
        Text("Payment: ${order.paymentMethod.label}", style = MaterialTheme.typography.bodyMedium)
        order.deliveryAddress.oneLine().takeIf { it.isNotBlank() }?.let {
            Text("Deliver to: $it", style = MaterialTheme.typography.bodyMedium)
        }

        Spacer(Modifier.height(4.dp))
        Text("Items", style = MaterialTheme.typography.titleMedium)

        order.items.forEach { line ->
            Card(shape = RoundedCornerShape(10.dp), modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(12.dp)) {
                    Text(line.product.title, fontWeight = FontWeight.Medium)
                    Text("Quantity: ${line.quantity}")
                    Text("Unit price: ${line.unitPrice.asPrice()}")
                    if (line.discountApplied > 0) {
                        Text(
                            "Discount: −${line.discountApplied.toInt()}%",
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                }
            }
        }

        HorizontalDivider(Modifier.padding(vertical = 4.dp))
        if (order.shippingFee > 0) {
            Text("Shipping: ${order.shippingFee.asPrice()}")
        }
        Text(
            "Total: ${order.totalAmount.asPrice()}",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        OutlinedButton(onClick = onBack, modifier = Modifier.align(Alignment.End)) {
            Text("Back to orders")
        }
    }
}
