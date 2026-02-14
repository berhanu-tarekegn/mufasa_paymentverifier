package com.itechsolution.mufasapay.domain.model

data class SmsMessage(
    val id: Long = 0,
    val sender: String,
    val message: String,
    val timestamp: Long,
    val rawJson: String? = null,
    val isForwarded: Boolean = false,
    val forwardedAt: Long? = null
)
