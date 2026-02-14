package com.itechsolution.mufasapay.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class WebhookResponse(
    @Json(name = "success")
    val success: Boolean = true,

    @Json(name = "message")
    val message: String? = null,

    @Json(name = "data")
    val data: Map<String, Any?>? = null
)
