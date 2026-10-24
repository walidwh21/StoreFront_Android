package com.storefront.app.feature.orders

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.dp
import com.storefront.app.core.ui.EmptyView
import com.storefront.app.core.ui.ErrorView
import com.storefront.app.core.ui.LoadingView

/** Order history. Tapping a card swaps the list for that order's detail view. */
@Composable
fun OrdersScreen(
    state: OrdersUiState,
    userId: String,
    onLoad: (String) -> Unit
) {
    var openOrderId by rememberSaveable { mutableStateOf<String?>(null) }

    LaunchedEffect(userId) { onLoad(userId) }
    BackHandler(enabled = openOrderId != null) { openOrderId = null }

    val open = state.orders.firstOrNull { it.orderId == openOrderId }

    when {
        open != null -> OrderDetail(order = open, onBack = { openOrderId = null })
        state.loading && state.orders.isEmpty() -> LoadingView()
        state.loadError != null && state.orders.isEmpty() ->
            ErrorView(state.loadError, onRetry = { onLoad(userId) })
        state.orders.isEmpty() -> EmptyView("You haven't placed any orders yet")
        else -> LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(state.orders, key = { it.orderId }) { order ->
                OrderCard(order = order, onClick = { openOrderId = order.orderId })
            }
        }
    }
}
