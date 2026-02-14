package com.itechsolution.mufasapay.data.remote.interceptor

import com.itechsolution.mufasapay.util.Constants
import okhttp3.Credentials
import okhttp3.Interceptor
import okhttp3.Response
import timber.log.Timber

/**
 * OkHttp interceptor for adding authentication headers
 * Supports Bearer, Basic, and API Key authentication
 */
class AuthInterceptor(
    private val authType: String,
    private val authValue: String?
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()

        // If no auth or no auth value, proceed without auth
        if (authType == Constants.AUTH_TYPE_NONE || authValue.isNullOrBlank()) {
            return chain.proceed(originalRequest)
        }

        val requestBuilder = originalRequest.newBuilder()

        try {
            when (authType) {
                Constants.AUTH_TYPE_BEARER -> {
                    // Bearer Token authentication
                    requestBuilder.addHeader("Authorization", "Bearer $authValue")
                    Timber.d("Added Bearer token authentication")
                }

                Constants.AUTH_TYPE_BASIC -> {
                    // Basic authentication (expects username:password format)
                    val parts = authValue.split(":", limit = 2)
                    if (parts.size == 2) {
                        val credentials = Credentials.basic(parts[0], parts[1])
                        requestBuilder.addHeader("Authorization", credentials)
                        Timber.d("Added Basic authentication")
                    } else {
                        Timber.w("Invalid Basic auth format, expected username:password")
                    }
                }

                Constants.AUTH_TYPE_API_KEY -> {
                    // API Key authentication (expects header_name:api_key format)
                    val parts = authValue.split(":", limit = 2)
                    if (parts.size == 2) {
                        requestBuilder.addHeader(parts[0], parts[1])
                        Timber.d("Added API Key authentication with header: ${parts[0]}")
                    } else {
                        // Fallback to X-API-Key if no header name specified
                        requestBuilder.addHeader("X-API-Key", authValue)
                        Timber.d("Added API Key authentication (X-API-Key)")
                    }
                }

                else -> {
                    Timber.w("Unknown auth type: $authType")
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "Error adding authentication header")
        }

        return chain.proceed(requestBuilder.build())
    }
}
