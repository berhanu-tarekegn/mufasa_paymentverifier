package com.itechsolution.mufasapay.domain.usecase.webhook

import com.itechsolution.mufasapay.BuildConfig
import com.itechsolution.mufasapay.data.remote.WebhookClientFactory
import com.itechsolution.mufasapay.data.remote.dto.SmsWebhookData
import com.itechsolution.mufasapay.data.remote.dto.SmsWebhookPayload
import com.itechsolution.mufasapay.domain.model.WebhookConfig
import com.itechsolution.mufasapay.domain.repository.WebhookRepository
import com.itechsolution.mufasapay.util.Result
import com.itechsolution.mufasapay.util.WebhookUrlResolver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber

/**
 * Use case for testing webhook connection
 * Only tests HTTP connectivity — does not create delivery logs or touch SMS records
 */
class TestWebhookConnectionUseCase(
    private val webhookRepository: WebhookRepository,
    private val webhookClientFactory: WebhookClientFactory
) {
    suspend operator fun invoke(): Result<String> = withContext(Dispatchers.IO) {
        try {
            val configResult = webhookRepository.getConfig()
            if (configResult.isError || configResult.getOrNull() == null) {
                return@withContext Result.error("Webhook not configured")
            }

            val config = configResult.getOrNull()!!
            if (config.url.isBlank()) {
                return@withContext Result.error("Webhook URL is empty")
            }

            Timber.d("Testing webhook connection to: ${config.url}")

            val testPayload = buildTestPayload()
            val apiService = webhookClientFactory.createService(config)
            val headers = buildHeaders(config)

            val response = apiService.forwardSmsPost(
                WebhookUrlResolver.uploadUrl(config.url),
                headers,
                testPayload
            )

            if (response.isSuccessful) {
                Timber.i("Webhook test successful. HTTP ${response.code()}")
                Result.success("Webhook test successful! (HTTP ${response.code()})")
            } else {
                val errorBody = response.errorBody()?.string() ?: "Unknown error"
                Timber.w("Webhook test failed. HTTP ${response.code()}: $errorBody")
                Result.error("Webhook test failed: HTTP ${response.code()} - $errorBody")
            }
        } catch (e: Exception) {
            Timber.e(e, "Error testing webhook connection")
            Result.error(e, "Connection failed: ${e.message}")
        }
    }

    private fun buildTestPayload(): SmsWebhookPayload {
        return SmsWebhookPayload(
            data = SmsWebhookData(
                sender = "TEST_SENDER",
                provider = "Test Provider",
                amount = 123.45,
                transactionId = "TEST12345"
            )
        )
    }

    private fun buildHeaders(config: WebhookConfig): Map<String, String> {
        val headers = mutableMapOf<String, String>()
        headers["Content-Type"] = "application/json"
        headers["User-Agent"] = "MufasaPay-SMS-Gateway/${BuildConfig.VERSION_NAME}"
        headers.putAll(config.headers)
        return headers
    }
}
