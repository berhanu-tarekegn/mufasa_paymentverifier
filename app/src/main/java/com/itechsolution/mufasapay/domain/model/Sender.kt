package com.itechsolution.mufasapay.domain.model

data class Sender(
    val senderId: String,
    val displayName: String,
    val pattern: String? = null,
    val isEnabled: Boolean = true,
    val addedAt: Long,
    val lastMessageAt: Long? = null,
    val messageCount: Int = 0
)
