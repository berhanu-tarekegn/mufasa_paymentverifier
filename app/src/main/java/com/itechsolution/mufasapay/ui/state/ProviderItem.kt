package com.itechsolution.mufasapay.ui.state

/**
 * Represents a payment provider in the onboarding selection screen
 */
data class ProviderItem(
    val senderId: String,
    val displayName: String,
    val description: String,
    val isSelected: Boolean = false
)
