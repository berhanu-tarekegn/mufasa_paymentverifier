package com.itechsolution.mufasapay.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class SmsWebhookPayload(
    @Json(name = "sender")
    val sender: String,

    @Json(name = "provider")
    val provider: String,

    @Json(name = "amount")
    val amount: Double,

    @Json(name = "transaction_id")
    val transactionId: String
)
