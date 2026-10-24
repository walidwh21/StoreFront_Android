package com.storefront.app.navigation

import android.widget.Toast
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.storefront.app.domain.model.Product
import com.storefront.app.feature.account.AccountAction
import com.storefront.app.feature.account.AccountViewModel
import com.storefront.app.feature.account.ProfileEditScreen
import com.storefront.app.feature.account.ProfileScreen
import com.storefront.app.feature.account.SignInScreen
import com.storefront.app.feature.account.SignUpScreen
import com.storefront.app.feature.account.UserDirectoryScreen
import com.storefront.app.feature.admin.AdminHubScreen
import com.storefront.app.feature.cart.CartAction
import com.storefront.app.feature.cart.CartScreen
import com.storefront.app.feature.cart.CartViewModel
import com.storefront.app.feature.catalog.CatalogAction
import com.storefront.app.feature.catalog.CatalogScreen
import com.storefront.app.feature.catalog.CatalogViewModel
import com.storefront.app.feature.catalog.ProductDetailScreen
import com.storefront.app.feature.catalog.StockScreen
import com.storefront.app.feature.orders.CheckoutScreen
import com.storefront.app.feature.orders.OrdersAction
import com.storefront.app.feature.orders.OrdersScreen
import com.storefront.app.feature.orders.OrdersViewModel

@Composable
fun StorefrontNavHost(
    nav: NavHostController,
    padding: PaddingValues,
    category: String?,
    brand: String?,
    catalogVm: CatalogViewModel,
    cartVm: CartViewModel,
    ordersVm: OrdersViewModel,
    accountVm: AccountViewModel
) {
    val context = LocalContext.current
    val catalog by catalogVm.state.collectAsState()
    val cart by cartVm.state.collectAsState()
    val orders by ordersVm.state.collectAsState()
    val account by accountVm.state.collectAsState()

    val addToCart: (Product) -> Unit = { product ->
        cartVm.dispatch(CartAction.Add(product))
        Toast.makeText(context, "Added to cart", Toast.LENGTH_SHORT).show()
    }

    NavHost(
        navController = nav,
        startDestination = Destination.Home.route,
        modifier = Modifier.padding(padding)
    ) {
        // ---- shopping -------------------------------------------------------------
        composable(Destination.Home.route) {
            CatalogScreen(
                state = catalog,
                category = category,
                brand = brand,
                onRetry = { catalogVm.dispatch(CatalogAction.Refresh) },
                onOpenProduct = { id -> nav.navigate(Destination.ProductDetail.create(id)) },
                onAddToCart = addToCart
            )
        }

        composable(
            route = Destination.ProductDetail.route,
            arguments = listOf(navArgument(Destination.ARG_PRODUCT_ID) { type = NavType.StringType })
        ) { entry ->
            val id = entry.arguments?.getString(Destination.ARG_PRODUCT_ID) ?: return@composable
            LaunchedEffect(id) { catalogVm.dispatch(CatalogAction.Open(id)) }
            ProductDetailScreen(
                product = catalog.byId(id),
                error = catalog.error,
                onAddToCart = addToCart
            )
        }

        composable(Destination.Cart.route) {
            CartScreen(
                state = cart,
                onAction = cartVm::dispatch,
                onCheckout = {
                    if (account.isSignedIn) nav.navigate(Destination.Checkout.route)
                    else nav.redirectToSignIn()
                }
            )
        }

        composable(Destination.Checkout.route) {
            RequireAccess(
                allowed = account.isSignedIn,
                onDenied = { nav.redirectToSignIn() }
            ) {
                CheckoutScreen(
                    cart = cart,
                    orders = orders,
                    userId = account.currentUser?.userId.orEmpty(),
                    onPlaceOrder = { order -> ordersVm.dispatch(OrdersAction.Place(order)) },
                    onOrderPlaced = {
                        cartVm.dispatch(CartAction.Clear)
                        catalogVm.dispatch(CatalogAction.Refresh)   // stock changed server-side
                        ordersVm.dispatch(OrdersAction.AckPlaced)
                        nav.navigate(Destination.Orders.route) {
                            popUpTo(Destination.Home.route)
                            launchSingleTop = true
                        }
                    },
                    onDismissError = { ordersVm.dispatch(OrdersAction.DismissPlaceError) },
                    onCancel = { nav.popBackStack() }
                )
            }
        }

        composable(Destination.Orders.route) {
            RequireAccess(account.isSignedIn, onDenied = { nav.redirectToSignIn() }) {
                OrdersScreen(
                    state = orders,
                    userId = account.currentUser?.userId.orEmpty(),
                    onLoad = { id -> ordersVm.dispatch(OrdersAction.Load(id)) }
                )
            }
        }

        // ---- account --------------------------------------------------------------
        composable(Destination.SignIn.route) {
            SignInScreen(
                state = account,
                onSubmit = { email, password -> accountVm.dispatch(AccountAction.SignIn(email, password)) },
                onSwitchToSignUp = {
                    nav.navigate(Destination.SignUp.route) {
                        popUpTo(Destination.SignIn.route) { inclusive = true }
                    }
                },
                onSignedIn = { nav.openOwnProfile(account.currentUser?.userId) }
            )
        }

        composable(Destination.SignUp.route) {
            SignUpScreen(
                state = account,
                onSubmit = { name, email, password ->
                    accountVm.dispatch(AccountAction.SignUp(name, email, password))
                },
                onSwitchToSignIn = {
                    nav.navigate(Destination.SignIn.route) {
                        popUpTo(Destination.SignUp.route) { inclusive = true }
                    }
                },
                onSignedIn = { nav.openOwnProfile(account.currentUser?.userId) }
            )
        }

        composable(
            route = Destination.Profile.route,
            arguments = listOf(navArgument(Destination.ARG_USER_ID) { type = NavType.StringType })
        ) { entry ->
            val userId = entry.arguments?.getString(Destination.ARG_USER_ID) ?: return@composable
            // Signing out lands here with no session; the guard sends the shopper to sign-in.
            RequireAccess(account.isSignedIn, onDenied = { nav.redirectToSignIn() }) {
                ProfileScreen(
                    userId = userId,
                    state = account,
                    onLoad = { accountVm.dispatch(AccountAction.LoadProfile(it)) },
                    onEdit = { nav.navigate(Destination.ProfileEdit.create(userId)) },
                    onSignOut = { accountVm.dispatch(AccountAction.SignOut) }
                )
            }
        }

        composable(
            route = Destination.ProfileEdit.route,
            arguments = listOf(navArgument(Destination.ARG_USER_ID) { type = NavType.StringType })
        ) { entry ->
            val userId = entry.arguments?.getString(Destination.ARG_USER_ID) ?: return@composable
            RequireAccess(account.isSignedIn, onDenied = { nav.redirectToSignIn() }) {
                ProfileEditScreen(
                    userId = userId,
                    state = account,
                    onLoad = { accountVm.dispatch(AccountAction.LoadProfile(it)) },
                    onSave = { accountVm.dispatch(AccountAction.SaveProfile(it)) },
                    onSaved = {
                        accountVm.dispatch(AccountAction.AckSaved)
                        nav.popBackStack()
                    },
                    onBack = { nav.popBackStack() }
                )
            }
        }

        // ---- back office ----------------------------------------------------------
        composable(Destination.Admin.route) {
            RequireAccess(account.isAdmin, onDenied = { nav.redirectToSignIn() }) {
                AdminHubScreen(
                    onManageUsers = { nav.navigate(Destination.AdminUsers.route) },
                    onManageStock = { nav.navigate(Destination.AdminStock.route) }
                )
            }
        }

        composable(Destination.AdminUsers.route) {
            RequireAccess(account.isAdmin, onDenied = { nav.redirectToSignIn() }) {
                UserDirectoryScreen(
                    state = account,
                    onLoad = { accountVm.dispatch(AccountAction.LoadUsers) },
                    onOpenUser = { user -> nav.navigate(Destination.Profile.create(user.userId)) }
                )
            }
        }

        composable(Destination.AdminStock.route) {
            RequireAccess(account.isAdmin, onDenied = { nav.redirectToSignIn() }) {
                // Always show fresh numbers when an admin opens the inventory.
                LaunchedEffect(Unit) { catalogVm.dispatch(CatalogAction.Refresh) }
                StockScreen(
                    state = catalog,
                    category = category,
                    brand = brand,
                    onRetry = { catalogVm.dispatch(CatalogAction.Refresh) },
                    onRestock = { id, amount -> catalogVm.dispatch(CatalogAction.Restock(id, amount)) },
                    onMarkSoldOut = { id -> catalogVm.dispatch(CatalogAction.MarkSoldOut(id)) }
                )
            }
        }
    }
}

/** Shows [content] only when [allowed]; otherwise runs [onDenied] once (e.g. to redirect). */
@Composable
private fun RequireAccess(
    allowed: Boolean,
    onDenied: () -> Unit,
    content: @Composable () -> Unit
) {
    if (allowed) {
        content()
    } else {
        LaunchedEffect(Unit) { onDenied() }
    }
}

private fun NavHostController.openOwnProfile(userId: String?) {
    if (userId == null) return
    navigate(Destination.Profile.create(userId)) {
        popUpTo(Destination.Home.route)
        launchSingleTop = true
    }
}
