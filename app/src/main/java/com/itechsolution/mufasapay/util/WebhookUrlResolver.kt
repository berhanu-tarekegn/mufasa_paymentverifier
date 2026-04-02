package com.itechsolution.mufasapay.util

import okhttp3.HttpUrl.Companion.toHttpUrlOrNull

object WebhookUrlResolver {
    private const val API_VERSION = "v1"
    private const val TRANSACTIONS = "transactions"

    fun uploadUrl(baseUrl: String): String = buildUrl(baseUrl)

    fun deleteUrl(baseUrl: String, transactionId: String): String = buildUrl(baseUrl, transactionId)

    private fun buildUrl(baseUrl: String, transactionId: String? = null): String {
        val httpUrl = baseUrl.toHttpUrlOrNull()
            ?: throw IllegalArgumentException("Invalid webhook URL: $baseUrl")

        val sanitizedSegments = sanitizeBasePath(httpUrl.pathSegments)
        val builder = httpUrl.newBuilder()
            .encodedPath("/")
            .query(null)
            .fragment(null)

        sanitizedSegments.forEach(builder::addPathSegment)
        builder.addPathSegment(API_VERSION)
        builder.addPathSegment(TRANSACTIONS)

        if (transactionId != null) {
            builder.addPathSegment(transactionId)
        }

        return builder.build().toString()
    }

    private fun sanitizeBasePath(pathSegments: List<String>): List<String> {
        val segments = pathSegments.filter { it.isNotBlank() }.toMutableList()

        if (segments.size >= 2 &&
            segments[segments.lastIndex - 1] == API_VERSION &&
            segments.last() == TRANSACTIONS
        ) {
            return segments.dropLast(2)
        }

        if (segments.size >= 3 &&
            segments[segments.lastIndex - 2] == API_VERSION &&
            segments[segments.lastIndex - 1] == TRANSACTIONS
        ) {
            return segments.dropLast(3)
        }

        return segments
    }
}
