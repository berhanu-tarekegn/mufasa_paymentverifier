package com.itechsolution.mufasapay.domain.usecase.webhook

import com.itechsolution.mufasapay.domain.model.WebhookConfig
import com.itechsolution.mufasapay.domain.repository.WebhookRepository
import com.itechsolution.mufasapay.util.DateTimeUtils
import com.itechsolution.mufasapay.util.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber

class SaveWebhookConfigUseCase(
    private val webhookRepository: WebhookRepository
) {
    suspend operator fun invoke(
        url: String,
        method: String = "POST",
        headers: Map<String, String> = emptyMap(),
        authType: String = "NONE",
        authValue: String? = null,
        timeout: Int = 30000,
        retryEnabled: Boolean = true,
        maxRetries: Int = 3,
        retryDelayMs: Long = 5000,
        isEnabled: Boolean = true
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            // Validate URL
            if (url.isBlank()) {
                return@withContext Result.error("Webhook URL cannot be empty")
            }

            // Validate URL format
            if (!url.startsWith("http://") && !url.startsWith("https://")) {
                return@withContext Result.error("Webhook URL must start with http:// or https://")
            }

            // Warn if using HTTP instead of HTTPS
            if (url.startsWith("http://") && !url.startsWith("https://")) {
                Timber.w("Warning: Using HTTP instead of HTTPS for webhook URL")
            }

            val config = WebhookConfig(
                url = url.trim(),
                method = method.uppercase(),
                headers = headers,
                authType = authType,
                authValue = authValue,
                timeout = timeout,
                retryEnabled = retryEnabled,
                maxRetries = maxRetries,
                retryDelayMs = retryDelayMs,
                isEnabled = isEnabled,
                updatedAt = DateTimeUtils.getCurrentTimestamp()
            )

            Timber.d("Saving webhook config: $url")
            webhookRepository.saveConfig(config)
        } catch (e: Exception) {
            Timber.e(e, "Error saving webhook config")
            Result.error(e, "Failed to save webhook configuration")
        }
    }
}
