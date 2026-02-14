package com.itechsolution.mufasapay.domain.usecase

import android.content.Context
import android.os.Build
import com.itechsolution.mufasapay.BuildConfig
import com.itechsolution.mufasapay.data.remote.WebhookClientFactory
import com.itechsolution.mufasapay.data.remote.dto.Metadata
import com.itechsolution.mufasapay.data.remote.dto.SmsData
import com.itechsolution.mufasapay.data.remote.dto.SmsWebhookPayload
import com.itechsolution.mufasapay.data.worker.SmsForwardWorker
import com.itechsolution.mufasapay.domain.model.DeliveryLog
import com.itechsolution.mufasapay.domain.model.DeliveryStatus
import com.itechsolution.mufasapay.domain.model.SmsMessage
import com.itechsolution.mufasapay.domain.model.WebhookConfig
import com.itechsolution.mufasapay.domain.repository.DeliveryRepository
import com.itechsolution.mufasapay.domain.repository.SmsRepository
import com.itechsolution.mufasapay.domain.repository.WebhookRepository
import com.itechsolution.mufasapay.util.Constants
import com.itechsolution.mufasapay.util.DateTimeUtils
import com.itechsolution.mufasapay.util.Result
import com.squareup.moshi.Moshi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.security.MessageDigest

/**
 * Use case for forwarding SMS to webhook endpoint
 * Handles HTTP request, response logging, and retry scheduling
 * This is a CRITICAL use case for reliability
 */
class ForwardSmsToWebhookUseCase(
    private val context: Context,
    private val webhookRepository: WebhookRepository,
    private val deliveryRepository: DeliveryRepository,
    private val smsRepository: SmsRepository,
    private val webhookClientFactory: WebhookClientFactory,
    private val moshi: Moshi
) {

    suspend operator fun invoke(sms: SmsMessage): Result<DeliveryLog> = withContext(Dispatchers.IO) {
        val startTime = System.currentTimeMillis()
        Timber.d("Starting webhook forwarding for SMS ID: ${sms.id}")

        try {
            // 1. Get webhook configuration
            val configResult = webhookRepository.getConfig()
            if (configResult.isError) {
                return@withContext Result.error("Webhook configuration not found")
            }

            val config = configResult.getOrNull()
            if (config == null || !config.isEnabled) {
                return@withContext Result.error("Webhook is not configured or disabled")
            }

            // 2. Validate webhook URL
            if (config.url.isBlank()) {
                return@withContext Result.error("Webhook URL is empty")
            }

            // 3. Build webhook payload
            val payload = buildPayload(sms)
            val payloadJson = moshi.adapter(SmsWebhookPayload::class.java).toJson(payload)

            // 4. Create initial delivery log
            val deliveryLog = DeliveryLog(
                smsId = sms.id,
                status = DeliveryStatus.PENDING,
                attemptNumber = 1,
                requestPayload = payloadJson,
                timestamp = System.currentTimeMillis()
            )

            val logIdResult = deliveryRepository.createLog(deliveryLog)
            if (logIdResult.isError) {
                return@withContext Result.error("Failed to create delivery log")
            }
            val logId = logIdResult.getOrNull() ?: 0L

            // 5. Make HTTP request
            val response = makeWebhookRequest(config, payload)

            // 6. Calculate duration
            val duration = System.currentTimeMillis() - startTime

            // 7. Handle response
            if (response.isSuccess) {
                // Success - update delivery log and mark SMS as forwarded
                deliveryRepository.updateLogSuccess(
                    id = logId,
                    status = DeliveryStatus.SUCCESS,
                    responseCode = response.code,
                    responseBody = response.body,
                    duration = duration,
                    timestamp = System.currentTimeMillis()
                )

                smsRepository.markAsForwarded(sms.id, System.currentTimeMillis())

                Timber.i("SMS forwarded successfully. ID: ${sms.id}, Duration: ${duration}ms")

                return@withContext Result.success(
                    deliveryLog.copy(
                        id = logId,
                        status = DeliveryStatus.SUCCESS,
                        responseCode = response.code,
                        responseBody = response.body,
                        duration = duration
                    )
                )
            } else {
                // Failure - update delivery log with error
                deliveryRepository.updateLogStatus(
                    id = logId,
                    status = DeliveryStatus.FAILED,
                    attemptNumber = 1,
                    errorMessage = response.errorMessage,
                    timestamp = System.currentTimeMillis()
                )

                Timber.w("SMS forwarding failed. ID: ${sms.id}, Error: ${response.errorMessage}")

                // Schedule retry if enabled
                if (config.retryEnabled && config.maxRetries > 1) {
                    Timber.d("Scheduling retry for delivery log: $logId")
                    SmsForwardWorker.scheduleRetry(context, logId, config.retryDelayMs)
                } else {
                    Timber.d("Retry disabled or max retries is 1, not scheduling retry")
                }

                return@withContext Result.error(
                    Exception(response.errorMessage),
                    "Webhook request failed: ${response.errorMessage}"
                )
            }
        } catch (e: Exception) {
            Timber.e(e, "Exception during SMS forwarding")
            return@withContext Result.error(e, "Failed to forward SMS: ${e.message}")
        }
    }

    /**
     * Makes the actual HTTP request to the webhook
     */
    private suspend fun makeWebhookRequest(
        config: WebhookConfig,
        payload: SmsWebhookPayload
    ): WebhookResult {
        return try {
            // Create configured API service
            val apiService = webhookClientFactory.createService(config)

            // Combine config headers with payload headers
            val headers = buildHeaders(config)

            // Make request based on HTTP method
            val response = when (config.method.uppercase()) {
                Constants.HTTP_METHOD_POST -> apiService.forwardSmsPost(config.url, headers, payload)
                Constants.HTTP_METHOD_PUT -> apiService.forwardSmsPut(config.url, headers, payload)
                Constants.HTTP_METHOD_PATCH -> apiService.forwardSmsPatch(config.url, headers, payload)
                else -> apiService.forwardSmsPost(config.url, headers, payload)
            }

            if (response.isSuccessful) {
                val body = response.body()?.string() ?: ""
                WebhookResult(
                    isSuccess = true,
                    code = response.code(),
                    body = body
                )
            } else {
                val errorBody = response.errorBody()?.string() ?: "Unknown error"
                WebhookResult(
                    isSuccess = false,
                    code = response.code(),
                    errorMessage = "HTTP ${response.code()}: $errorBody"
                )
            }
        } catch (e: Exception) {
            Timber.e(e, "Exception during webhook HTTP request")
            WebhookResult(
                isSuccess = false,
                errorMessage = "Network error: ${e.message}"
            )
        }
    }

    /**
     * Builds the webhook payload from SMS message
     */
    private fun buildPayload(sms: SmsMessage): SmsWebhookPayload {
        return SmsWebhookPayload(
            event = Constants.WEBHOOK_EVENT_SMS_RECEIVED,
            timestamp = System.currentTimeMillis(),
            data = SmsData(
                sender = sms.sender,
                message = sms.message,
                receivedAt = sms.timestamp,
                originalFormat = sms.rawJson
            ),
            metadata = Metadata(
                deviceId = getDeviceId(),
                appVersion = BuildConfig.VERSION_NAME,
                sdkVersion = Build.VERSION.SDK_INT,
                forwardedAt = System.currentTimeMillis()
            )
        )
    }

    /**
     * Builds HTTP headers from webhook config
     */
    private fun buildHeaders(config: WebhookConfig): Map<String, String> {
        val headers = mutableMapOf<String, String>()

        // Add Content-Type
        headers["Content-Type"] = "application/json"

        // Add User-Agent
        headers["User-Agent"] = "MufasaPay-SMS-Gateway/${BuildConfig.VERSION_NAME}"

        // Add custom headers from config
        headers.putAll(config.headers)

        return headers
    }

    /**
     * Generates a hashed device ID for privacy
     */
    private fun getDeviceId(): String {
        return try {
            val value = "${Build.MANUFACTURER}_${Build.MODEL}_${Build.DEVICE}"
            val digest = MessageDigest.getInstance("SHA-256")
            val hash = digest.digest(value.toByteArray())
            hash.joinToString("") { "%02x".format(it) }.take(16)
        } catch (e: Exception) {
            "unknown_device"
        }
    }

    /**
     * Data class to hold webhook HTTP response
     */
    private data class WebhookResult(
        val isSuccess: Boolean,
        val code: Int = 0,
        val body: String? = null,
        val errorMessage: String? = null
    )
}
