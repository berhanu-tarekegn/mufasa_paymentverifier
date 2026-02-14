package com.itechsolution.mufasapay.domain.model

enum class DeliveryStatus {
    PENDING,
    SUCCESS,
    FAILED,
    RETRYING;

    companion object {
        fun fromString(value: String): DeliveryStatus {
            return entries.find { it.name == value } ?: PENDING
        }
    }
}
