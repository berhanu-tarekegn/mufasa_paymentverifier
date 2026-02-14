package com.itechsolution.mufasapay.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class SmsWebhookPayload(
    @Json(name = "event")
    val event: String,

    @Json(name = "timestamp")
    val timestamp: Long,

    @Json(name = "data")
    val data: SmsData,

    @Json(name = "metadata")
    val metadata: Metadata
)

@JsonClass(generateAdapter = true)
data class SmsData(
    @Json(name = "sender")
    val sender: String,

    @Json(name = "message")
    val message: String,

    @Json(name = "receivedAt")
    val receivedAt: Long,

    @Json(name = "originalFormat")
    val originalFormat: String? = null
)

@JsonClass(generateAdapter = true)
data class Metadata(
    @Json(name = "deviceId")
    val deviceId: String,

    @Json(name = "appVersion")
    val appVersion: String,

    @Json(name = "sdkVersion")
    val sdkVersion: Int,

    @Json(name = "forwardedAt")
    val forwardedAt: Long
)
