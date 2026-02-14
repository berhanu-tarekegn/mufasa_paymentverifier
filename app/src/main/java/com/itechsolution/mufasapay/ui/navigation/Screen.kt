package com.itechsolution.mufasapay.ui.navigation

/**
 * Sealed class representing all navigation routes in the app
 */
sealed class Screen(val route: String) {
    object Permissions : Screen("permissions")
    object ProviderSelection : Screen("provider_selection")
    object Dashboard : Screen("dashboard")
    object SenderManagement : Screen("sender_management")
    object WebhookConfig : Screen("webhook_config")
    object SmsHistory : Screen("sms_history")
}
