package com.itechsolution.mufasapay.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.rememberNavController

/**
 * Main navigation host for the app
 */
@Composable
fun NavigationHost() {
    val navController = rememberNavController()
    NavGraph(navController = navController)
}
