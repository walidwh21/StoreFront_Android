package com.storefront.app.navigation

/** Every screen the app can show, with the route pattern it is registered under. */
sealed class Destination(val route: String) {
    data object Home : Destination("home")
    data object Cart : Destination("cart")
    data object Checkout : Destination("checkout")
    data object Orders : Destination("orders")
    data object SignIn : Destination("sign_in")
    data object SignUp : Destination("sign_up")

    data object Admin : Destination("admin")
    data object AdminUsers : Destination("admin/users")
    data object AdminStock : Destination("admin/stock")

    data object ProductDetail : Destination("product/{$ARG_PRODUCT_ID}") {
        fun create(productId: String) = "product/$productId"
    }

    data object Profile : Destination("profile/{$ARG_USER_ID}") {
        const val PREFIX = "profile/"
        fun create(userId: String) = "profile/$userId"
    }

    data object ProfileEdit : Destination("profile/{$ARG_USER_ID}/edit") {
        fun create(userId: String) = "profile/$userId/edit"
    }

    companion object {
        const val ARG_PRODUCT_ID = "productId"
        const val ARG_USER_ID = "userId"
    }
}
