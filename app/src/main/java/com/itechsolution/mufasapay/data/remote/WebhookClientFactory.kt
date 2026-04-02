package com.itechsolution.mufasapay.data.remote

import com.itechsolution.mufasapay.BuildConfig
import com.itechsolution.mufasapay.data.remote.api.WebhookApiService
import com.itechsolution.mufasapay.data.remote.interceptor.AuthInterceptor
import com.itechsolution.mufasapay.domain.model.WebhookConfig
import com.squareup.moshi.Moshi
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import timber.log.Timber
import java.util.concurrent.TimeUnit

/**
 * Factory for creating configured Retrofit instances for webhook forwarding
 * Supports dynamic base URLs and authentication based on webhook configuration
 */
class WebhookClientFactory(
    private val moshi: Moshi
) {

    /**
     * Creates a WebhookApiService configured with the provided webhook settings
     */
    fun createService(requestUrl: String, config: WebhookConfig): WebhookApiService {
        val okHttpClient = createOkHttpClient(config)

        // Extract base URL from the request URL
        val baseUrl = extractBaseUrl(requestUrl)

        val retrofit = Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()

        return retrofit.create(WebhookApiService::class.java)
    }

    /**
     * Creates an OkHttpClient with authentication and timeout configuration
     */
    private fun createOkHttpClient(config: WebhookConfig): OkHttpClient {
        val builder = OkHttpClient.Builder()

        // Add logging interceptor in debug builds
        if (BuildConfig.DEBUG) {
            val loggingInterceptor = HttpLoggingInterceptor { message ->
                Timber.tag("OkHttp").d(message)
            }.apply {
                level = HttpLoggingInterceptor.Level.BODY
            }
            builder.addInterceptor(loggingInterceptor)
        }

        // Add authentication interceptor
        if (config.authType != "NONE" && !config.authValue.isNullOrBlank()) {
            val authInterceptor = AuthInterceptor(config.authType, config.authValue)
            builder.addInterceptor(authInterceptor)
        }

        // Configure timeouts
        builder.connectTimeout(config.timeout.toLong(), TimeUnit.MILLISECONDS)
        builder.readTimeout(config.timeout.toLong(), TimeUnit.MILLISECONDS)
        builder.writeTimeout(config.timeout.toLong(), TimeUnit.MILLISECONDS)

        return builder.build()
    }

    /**
     * Extracts base URL from full request URL
     * Example: https://example.com/api/webhook -> https://example.com/
     */
    private fun extractBaseUrl(fullUrl: String): String {
        return try {
            val url = java.net.URL(fullUrl)
            "${url.protocol}://${url.host}/"
        } catch (e: Exception) {
            Timber.e(e, "Error extracting base URL from: $fullUrl")
            // Fallback to placeholder
            "https://api.placeholder.com/"
        }
    }
}
