package com.itechsolution.mufasapay.ui.util

import com.itechsolution.mufasapay.ui.state.ProviderItem

/**
 * Predefined list of Ethiopian payment providers for onboarding selection
 */
object EthiopianProviders {

    val ALL = listOf(
        ProviderItem(
            senderId = "CBE BIRR",
            displayName = "CBE Birr",
            description = "Commercial Bank of Ethiopia Mobile Banking"
        ),
        ProviderItem(
            senderId = "127",
            displayName = "Telebirr",
            description = "Ethio Telecom Mobile Money"
        ),
        ProviderItem(
            senderId = "MPESA",
            displayName = "M-PESA Ethiopia",
            description = "Vodafone Mobile Money"
        ),
        ProviderItem(
            senderId = "HelloCash",
            displayName = "HelloCash",
            description = "Mobile Payment Service"
        ),
        ProviderItem(
            senderId = "CBE",
            displayName = "Commercial Bank of Ethiopia",
            description = "Bank SMS Notifications"
        ),
        ProviderItem(
            senderId = "Awash Bank",
            displayName = "Awash Bank",
            description = "Bank SMS Notifications"
        ),
        ProviderItem(
            senderId = "BOA",
            displayName = "Bank of Abyssinia",
            description = "Bank SMS Notifications"
        ),
        ProviderItem(
            senderId = "Dashen Bank",
            displayName = "Dashen Bank",
            description = "Bank SMS Notifications"
        ),
        ProviderItem(
            senderId = "Wegagen Bank",
            displayName = "Wegagen Bank",
            description = "Bank SMS Notifications"
        ),
        ProviderItem(
            senderId = "Abyssinia Bank",
            displayName = "Abyssinia Bank",
            description = "Bank SMS Notifications"
        )
    )
}
