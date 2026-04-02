package com.itechsolution.mufasapay.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "webhook_config")
data class WebhookConfigEntity(
    @PrimaryKey
    val id: Int = 1, // Single row configuration

    val uploadUrl: String,

    val deleteUrlTemplate: String,

    val headers: String, // JSON string of headers map

    val authType: String = "NONE", // NONE, BEARER, BASIC, API_KEY

    val authValue: String? = null,

    val timeout: Int = 30000, // milliseconds

    val retryEnabled: Boolean = true,

    val maxRetries: Int = 3,

    val retryDelayMs: Long = 5000,

    val isEnabled: Boolean = true,

    val updatedAt: Long
)
