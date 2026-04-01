package com.itechsolution.mufasapay.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.itechsolution.mufasapay.ui.screens.onboarding.ProviderSelectionScreen
import com.itechsolution.mufasapay.ui.screens.permissions.PermissionScreen
import com.itechsolution.mufasapay.ui.util.PermissionUtils
import com.itechsolution.mufasapay.ui.util.PreferencesManager
import org.koin.compose.koinInject

/**
 * Main navigation graph for the app
 * Determines start destination based on first-run and permission status
 */
@Composable
fun NavGraph(
    navController: NavHostController,
    preferencesManager: PreferencesManager = koinInject()
) {
    val context = LocalContext.current

    // Determine start destination based on app state
    val startDestination = when {
        // First priority: Check permissions
        !PermissionUtils.hasAllPermissions(context) -> Screen.Permissions.route

        // Second priority: Check if first run (onboarding not complete)
        preferencesManager.isFirstRun() -> Screen.ProviderSelection.route

        // Default: Go to dashboard
        else -> Screen.AppShell.route
    }

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        // Permissions screen
        composable(Screen.Permissions.route) {
            PermissionScreen(
                onPermissionsGranted = {
                    // After permissions, check if first run
                    if (preferencesManager.isFirstRun()) {
                        navController.navigate(Screen.ProviderSelection.route) {
                            popUpTo(Screen.Permissions.route) { inclusive = true }
                        }
                    } else {
                        navController.navigate(Screen.AppShell.route) {
                            popUpTo(Screen.Permissions.route) { inclusive = true }
                        }
                    }
                }
            )
        }

        // Provider selection (onboarding)
        composable(Screen.ProviderSelection.route) {
            ProviderSelectionScreen(
                onComplete = {
                    navController.navigate(Screen.AppShell.route) {
                        popUpTo(Screen.ProviderSelection.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.AppShell.route) {
            AppShellScreen()
        }
    }
}
