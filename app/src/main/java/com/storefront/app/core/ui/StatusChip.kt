package com.storefront.app.core.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.storefront.app.domain.model.OrderStatus

@Composable
fun StatusChip(status: OrderStatus, modifier: Modifier = Modifier) {
    val scheme = MaterialTheme.colorScheme
    val tint = when (status) {
        OrderStatus.PENDING -> scheme.secondary
        OrderStatus.CONFIRMED -> scheme.tertiary
        OrderStatus.SHIPPED, OrderStatus.DELIVERED -> scheme.primary
        OrderStatus.CANCELLED -> scheme.error
    }
    Surface(
        modifier = modifier,
        color = tint.copy(alpha = 0.12f),
        contentColor = tint,
        shape = RoundedCornerShape(50),
        border = BorderStroke(1.dp, tint)
    ) {
        Text(
            text = status.label,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelMedium
        )
    }
}
