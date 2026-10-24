package com.storefront.app.navigation

import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController

/** Switch between bottom-bar tabs, preserving each tab's own back stack. */
fun NavHostController.navigateTopLevel(route: String) {
    navigate(route) {
        popUpTo(graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}

/** Send a guest to the sign-in page while keeping Home underneath for "back". */
fun NavHostController.redirectToSignIn() {
    navigate(Destination.SignIn.route) {
        popUpTo(Destination.Home.route)
        launchSingleTop = true
    }
}
