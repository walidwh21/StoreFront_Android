package com.storefront.app.feature.cart

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.storefront.app.core.format.asPrice
import com.storefront.app.core.ui.EmptyView

@Composable
fun CartScreen(
    state: CartUiState,
    onAction: (CartAction) -> Unit,
    onCheckout: () -> Unit
) {
    if (state.isEmpty) {
        EmptyView("Your cart is empty")
        return
    }

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(state.lines, key = { it.product.id }) { line ->
                CartLineRow(
                    line = line,
                    onQuantityChange = { qty ->
                        onAction(CartAction.SetQuantity(line.product.id, qty))
                    },
                    onRemove = { onAction(CartAction.Remove(line.product.id)) }
                )
            }
        }

        Spacer(Modifier.height(12.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "${state.itemCount} item${if (state.itemCount == 1) "" else "s"}",
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                state.total.asPrice(),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(Modifier.height(12.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = { onAction(CartAction.Clear) },
                modifier = Modifier.weight(1f)
            ) { Text("Clear cart") }
            Button(onClick = onCheckout, modifier = Modifier.weight(1f)) { Text("Checkout") }
        }
    }
}
