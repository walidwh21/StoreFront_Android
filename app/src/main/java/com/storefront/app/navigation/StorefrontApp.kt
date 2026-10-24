package com.storefront.app.navigation

import android.widget.Toast
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.storefront.app.core.ui.DropdownField
import com.storefront.app.feature.account.AccountAction
import com.storefront.app.feature.account.AccountViewModel
import com.storefront.app.feature.cart.CartViewModel
import com.storefront.app.feature.catalog.CatalogViewModel
import com.storefront.app.feature.orders.OrdersViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StorefrontApp(
    catalogVm: CatalogViewModel,
    cartVm: CartViewModel,
    ordersVm: OrdersViewModel,
    accountVm: AccountViewModel
) {
    val nav = rememberNavController()
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    val catalog by catalogVm.state.collectAsState()
    val cart by cartVm.state.collectAsState()
    val account by accountVm.state.collectAsState()

    var category by rememberSaveable { mutableStateOf<String?>(null) }
    var brand by rememberSaveable { mutableStateOf<String?>(null) }

    val route = nav.currentBackStackEntryAsState().value?.destination?.route

    // Auth errors surface as a single toast, wherever the shopper happens to be.
    LaunchedEffect(account.error) {
        account.error?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            accountVm.dispatch(AccountAction.DismissError)
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Text(
                    "Filters",
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 20.dp)
                )
                DropdownField(
                    label = "Category",
                    selected = category,
                    options = catalog.categories,
                    display = { it },
                    onSelect = { category = it; scope.launch { drawerState.close() } },
                    allowClear = true,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                Spacer(Modifier.height(12.dp))
                DropdownField(
                    label = "Brand",
                    selected = brand,
                    options = catalog.brands,
                    display = { it },
                    onSelect = { brand = it; scope.launch { drawerState.close() } },
                    allowClear = true,
                    modifier = Modifier.padding(horizontal = 16.dp).fillMaxWidth()
                )
            }
        }
    ) {
        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text("Storefront", color = MaterialTheme.colorScheme.onPrimary) },
                    navigationIcon = {
                        IconButton(onClick = {
                            scope.launch { if (drawerState.isClosed) drawerState.open() else drawerState.close() }
                        }) {
                            Icon(
                                Icons.Default.Menu,
                                contentDescription = "Filters",
                                tint = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                )
            },
            bottomBar = {
                NavigationBar {
                    NavigationBarItem(
                        selected = route == Destination.Home.route,
                        onClick = { nav.navigateTopLevel(Destination.Home.route) },
                        icon = { Icon(Icons.Default.Home, contentDescription = null) },
                        label = { Text("Shop") }
                    )
                    NavigationBarItem(
                        selected = route == Destination.Cart.route,
                        onClick = { nav.navigateTopLevel(Destination.Cart.route) },
                        icon = {
                            BadgedBox(badge = {
                                if (cart.itemCount > 0) Badge { Text("${cart.itemCount}") }
                            }) {
                                Icon(Icons.Default.ShoppingCart, contentDescription = null)
                            }
                        },
                        label = { Text("Cart") }
                    )
                    if (account.isSignedIn) {
                        NavigationBarItem(
                            selected = route == Destination.Orders.route,
                            onClick = { nav.navigateTopLevel(Destination.Orders.route) },
                            icon = { Icon(Icons.AutoMirrored.Filled.List, contentDescription = null) },
                            label = { Text("Orders") }
                        )
                    }
                    if (account.isAdmin) {
                        NavigationBarItem(
                            selected = route?.startsWith("admin") == true,
                            onClick = { nav.navigateTopLevel(Destination.Admin.route) },
                            icon = { Icon(Icons.Default.Settings, contentDescription = null) },
                            label = { Text("Admin") }
                        )
                    }
                    NavigationBarItem(
                        selected = route?.startsWith(Destination.Profile.PREFIX) == true ||
                            route == Destination.SignIn.route || route == Destination.SignUp.route,
                        onClick = {
                            val uid = account.currentUser?.userId
                            if (uid != null) nav.navigateTopLevel(Destination.Profile.create(uid))
                            else nav.navigateTopLevel(Destination.SignIn.route)
                        },
                        icon = { Icon(Icons.Default.Person, contentDescription = null) },
                        label = { Text(if (account.isSignedIn) "Profile" else "Sign in") }
                    )
                }
            }
        ) { padding ->
            StorefrontNavHost(
                nav = nav,
                padding = padding,
                category = category,
                brand = brand,
                catalogVm = catalogVm,
                cartVm = cartVm,
                ordersVm = ordersVm,
                accountVm = accountVm
            )
        }
    }
}
