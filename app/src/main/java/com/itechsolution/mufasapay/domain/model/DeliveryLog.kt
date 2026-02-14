package com.itechsolution.mufasapay.domain.model

data class DeliveryLog(
    val id: Long = 0,
    val smsId: Long,
    val status: DeliveryStatus,
    val attemptNumber: Int = 1,
    val requestPayload: String,
    val responseCode: Int? = null,
    val responseBody: String? = null,
    val errorMessage: String? = null,
    val timestamp: Long,
    val duration: Long? = null
)
