package com.storefront.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.storefront.app.core.theme.StorefrontTheme
import com.storefront.app.feature.account.AccountViewModel
import com.storefront.app.feature.cart.CartViewModel
import com.storefront.app.feature.catalog.CatalogViewModel
import com.storefront.app.feature.orders.OrdersViewModel
import com.storefront.app.navigation.StorefrontApp
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    // Activity-scoped so every screen shares one catalogue, cart, order list and session.
    private val catalogVm: CatalogViewModel by viewModels()
    private val cartVm: CartViewModel by viewModels()
    private val ordersVm: OrdersViewModel by viewModels()
    private val accountVm: AccountViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            StorefrontTheme {
                StorefrontApp(catalogVm, cartVm, ordersVm, accountVm)
            }
        }
    }
}
