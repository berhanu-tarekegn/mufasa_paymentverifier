package com.itechsolution.mufasapay.domain.model

data class WebhookConfig(
    val id: Int = 1,
    val url: String,
    val method: String = "POST",
    val headers: Map<String, String> = emptyMap(),
    val authType: String = "NONE",
    val authValue: String? = null,
    val timeout: Int = 30000,
    val retryEnabled: Boolean = true,
    val maxRetries: Int = 3,
    val retryDelayMs: Long = 5000,
    val isEnabled: Boolean = true,
    val updatedAt: Long
)
