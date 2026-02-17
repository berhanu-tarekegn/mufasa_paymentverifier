package com.itechsolution.mufasapay.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class SimpleMessagePayload(
    @Json(name = "message")
    val message: String
)
