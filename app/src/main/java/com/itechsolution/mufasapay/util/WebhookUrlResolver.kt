package com.itechsolution.mufasapay.util

import okhttp3.HttpUrl.Companion.toHttpUrlOrNull

object WebhookUrlResolver {
    private const val TRANSACTION_ID_PLACEHOLDER = "{transaction_id}"

    fun validateHttpUrl(url: String): String {
        val parsed = url.toHttpUrlOrNull()
            ?: throw IllegalArgumentException("Invalid webhook URL: $url")

        return parsed.toString()
    }

    fun resolveDeleteUrl(deleteUrlTemplate: String, transactionId: String): String {
        if (!deleteUrlTemplate.contains(TRANSACTION_ID_PLACEHOLDER)) {
            throw IllegalArgumentException("Delete webhook URL must include $TRANSACTION_ID_PLACEHOLDER")
        }

        return validateHttpUrl(deleteUrlTemplate.replace(TRANSACTION_ID_PLACEHOLDER, transactionId))
    }
}
