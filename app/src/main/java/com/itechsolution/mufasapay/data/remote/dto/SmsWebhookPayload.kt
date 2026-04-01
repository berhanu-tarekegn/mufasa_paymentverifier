package com.itechsolution.mufasapay.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class SmsWebhookPayload(
    @Json(name = "data")
    val data: SmsWebhookData
)

@JsonClass(generateAdapter = true)
data class SmsWebhookData(
    @Json(name = "sender")
    val sender: String,

    @Json(name = "amount")
    val amount: Double,

    @Json(name = "transactionId")
    val transactionId: String
)
