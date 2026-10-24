package com.storefront.app.feature.orders

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.storefront.app.core.format.asPrice
import com.storefront.app.core.ui.DropdownField
import com.storefront.app.core.ui.EmptyView
import com.storefront.app.core.ui.FormField
import com.storefront.app.domain.model.Address
import com.storefront.app.domain.model.Order
import com.storefront.app.domain.model.OrderFactory
import com.storefront.app.domain.model.PaymentMethod
import com.storefront.app.feature.cart.CartUiState

@Composable
fun CheckoutScreen(
    cart: CartUiState,
    orders: OrdersUiState,
    userId: String,
    onPlaceOrder: (Order) -> Unit,
    onOrderPlaced: () -> Unit,
    onDismissError: () -> Unit,
    onCancel: () -> Unit
) {
    val context = LocalContext.current

    var street by rememberSaveable { mutableStateOf("") }
    var city by rememberSaveable { mutableStateOf("") }
    var postalCode by rememberSaveable { mutableStateOf("") }
    var country by rememberSaveable { mutableStateOf("") }
    var payment by rememberSaveable { mutableStateOf(PaymentMethod.CASH_ON_DELIVERY) }

    // Failure (e.g. someone else bought the last unit): tell the shopper, keep the cart.
    LaunchedEffect(orders.placeError) {
        orders.placeError?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            onDismissError()
        }
    }

    // Success: the parent clears the cart and navigates; we only report it.
    LaunchedEffect(orders.placedOrderId) {
        if (orders.placedOrderId != null) {
            Toast.makeText(context, "Order placed — thank you!", Toast.LENGTH_SHORT).show()
            onOrderPlaced()
        }
    }

    if (cart.isEmpty && orders.placedOrderId == null) {
        EmptyView("Your cart is empty")
        return
    }

    val addressComplete = listOf(street, city, postalCode, country).all { it.isNotBlank() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text("Delivery address", style = MaterialTheme.typography.titleMedium)
        FormField(street, { street = it }, "Street")
        FormField(city, { city = it }, "City")
        FormField(postalCode, { postalCode = it }, "Postal code", keyboardType = KeyboardType.Number)
        FormField(country, { country = it }, "Country")

        Spacer(Modifier.height(8.dp))
        Text("Payment", style = MaterialTheme.typography.titleMedium)
        DropdownField(
            label = "Payment method",
            selected = payment,
            options = PaymentMethod.entries,
            display = { it.label },
            onSelect = { it?.let { method -> payment = method } }
        )

        HorizontalDivider(Modifier.padding(vertical = 8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("${cart.itemCount} item${if (cart.itemCount == 1) "" else "s"}")
            Text(
                cart.total.asPrice(),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }

        Button(
            onClick = {
                if (!addressComplete) {
                    Toast.makeText(context, "Please fill in every address field", Toast.LENGTH_SHORT).show()
                } else {
                    val order = OrderFactory.fromCart(
                        userId = userId,
                        lines = cart.lines,
                        address = Address(street.trim(), city.trim(), postalCode.trim(), country.trim()),
                        paymentMethod = payment
                    )
                    onPlaceOrder(order)
                }
            },
            enabled = !orders.placing && !cart.isEmpty,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (orders.placing) "Placing order…" else "Place order")
        }
        TextButton(onClick = onCancel, modifier = Modifier.fillMaxWidth()) { Text("Back to cart") }
    }
}
