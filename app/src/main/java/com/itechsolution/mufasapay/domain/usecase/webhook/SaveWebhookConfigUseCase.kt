package com.itechsolution.mufasapay.domain.usecase.webhook

import com.itechsolution.mufasapay.domain.model.WebhookConfig
import com.itechsolution.mufasapay.domain.repository.WebhookRepository
import com.itechsolution.mufasapay.util.DateTimeUtils
import com.itechsolution.mufasapay.util.Result
import com.itechsolution.mufasapay.util.WebhookUrlResolver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber

class SaveWebhookConfigUseCase(
    private val webhookRepository: WebhookRepository
) {
    suspend operator fun invoke(
        uploadUrl: String,
        deleteUrlTemplate: String,
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
            if (uploadUrl.isBlank()) {
                return@withContext Result.error("Upload webhook URL cannot be empty")
            }

            if (deleteUrlTemplate.isBlank()) {
                return@withContext Result.error("Delete webhook URL cannot be empty")
            }

            val normalizedUploadUrl = try {
                WebhookUrlResolver.validateHttpUrl(uploadUrl.trim())
            } catch (_: IllegalArgumentException) {
                return@withContext Result.error("Upload webhook URL must be a valid http:// or https:// URL")
            }

            val normalizedDeleteUrlTemplate = try {
                WebhookUrlResolver.validateHttpUrl(
                    deleteUrlTemplate.trim().replace("{transaction_id}", "TEST_TRANSACTION_ID")
                )
                deleteUrlTemplate.trim()
            } catch (_: IllegalArgumentException) {
                return@withContext Result.error("Delete webhook URL must be a valid http:// or https:// URL and include {transaction_id}")
            }

            if (!normalizedDeleteUrlTemplate.contains("{transaction_id}")) {
                return@withContext Result.error("Delete webhook URL must include {transaction_id}")
            }

            if (normalizedUploadUrl.startsWith("http://")) {
                Timber.w("Warning: Using HTTP instead of HTTPS for upload webhook URL")
            }

            if (normalizedDeleteUrlTemplate.startsWith("http://")) {
                Timber.w("Warning: Using HTTP instead of HTTPS for delete webhook URL")
            }

            val config = WebhookConfig(
                uploadUrl = normalizedUploadUrl,
                deleteUrlTemplate = normalizedDeleteUrlTemplate,
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

            Timber.d("Saving webhook config for upload URL: $uploadUrl")
            webhookRepository.saveConfig(config)
        } catch (e: Exception) {
            Timber.e(e, "Error saving webhook config")
            Result.error(e, "Failed to save webhook configuration")
        }
    }
}
