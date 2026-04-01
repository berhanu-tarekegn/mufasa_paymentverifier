package com.itechsolution.mufasapay.domain.model

data class SenderTemplate(
    val id: Long = 0,
    val senderId: String,
    val label: String,
    val pattern: String,
    val isEnabled: Boolean = true,
    val createdAt: Long
)
