package com.itechsolution.mufasapay.data.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.itechsolution.mufasapay.domain.model.DeliveryStatus
import com.itechsolution.mufasapay.domain.repository.DeliveryRepository
import com.itechsolution.mufasapay.domain.repository.SmsRepository
import com.itechsolution.mufasapay.domain.repository.WebhookRepository
import com.itechsolution.mufasapay.domain.usecase.ForwardSmsToWebhookUseCase
import com.itechsolution.mufasapay.util.Constants
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import timber.log.Timber
import java.util.concurrent.TimeUnit

/**
 * WorkManager worker for retrying failed webhook deliveries
 * Implements exponential backoff strategy
 * This is CRITICAL for reliability - ensures no SMS is lost
 */
class SmsForwardWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params), KoinComponent {

    private val deliveryRepository: DeliveryRepository by inject()
    private val smsRepository: SmsRepository by inject()
    private val webhookRepository: WebhookRepository by inject()
    private val forwardSmsToWebhookUseCase: ForwardSmsToWebhookUseCase by inject()

    override suspend fun doWork(): Result {
        val deliveryLogId = inputData.getLong(Constants.EXTRA_DELIVERY_LOG_ID, -1)
        if (deliveryLogId == -1L) {
            Timber.e("No delivery log ID provided to worker")
            return Result.failure()
        }

        Timber.d("SmsForwardWorker starting for delivery log ID: $deliveryLogId")

        return try {
            // 1. Get delivery log
            val logResult = deliveryRepository.getLogById(deliveryLogId)
            if (logResult.isError || logResult.getOrNull() == null) {
                Timber.e("Delivery log not found: $deliveryLogId")
                return Result.failure()
            }

            val deliveryLog = logResult.getOrNull()!!

            // 2. Check if we should retry
            val webhookConfig = webhookRepository.getConfig().getOrNull()
            if (webhookConfig == null || !webhookConfig.retryEnabled) {
                Timber.d("Retry disabled, marking as permanently failed")
                deliveryRepository.updateLogStatus(
                    id = deliveryLogId,
                    status = DeliveryStatus.FAILED,
                    attemptNumber = deliveryLog.attemptNumber,
                    errorMessage = "Retry disabled",
                    timestamp = System.currentTimeMillis()
                )
                return Result.failure()
            }

            if (deliveryLog.attemptNumber >= webhookConfig.maxRetries) {
                Timber.w("Max retries (${webhookConfig.maxRetries}) reached for delivery log: $deliveryLogId")
                deliveryRepository.updateLogStatus(
                    id = deliveryLogId,
                    status = DeliveryStatus.FAILED,
                    attemptNumber = deliveryLog.attemptNumber,
                    errorMessage = "Max retries exceeded",
                    timestamp = System.currentTimeMillis()
                )
                return Result.failure()
            }

            // 3. Update status to RETRYING
            val nextAttempt = deliveryLog.attemptNumber + 1
            deliveryRepository.updateLogStatus(
                id = deliveryLogId,
                status = DeliveryStatus.RETRYING,
                attemptNumber = nextAttempt,
                errorMessage = "Retry attempt $nextAttempt",
                timestamp = System.currentTimeMillis()
            )

            // 4. Get the SMS message
            val smsResult = smsRepository.getSmsById(deliveryLog.smsId)
            if (smsResult.isError || smsResult.getOrNull() == null) {
                Timber.e("SMS not found for delivery log: $deliveryLogId")
                return Result.failure()
            }

            val sms = smsResult.getOrNull()!!

            // 5. Retry forwarding
            Timber.i("Retrying SMS forwarding (attempt $nextAttempt/${webhookConfig.maxRetries})")
            val forwardResult = forwardSmsToWebhookUseCase(sms, deliveryLogId)

            if (forwardResult.isSuccess) {
                // Success! No need to schedule another retry
                Timber.i("Retry successful for delivery log: $deliveryLogId")
                return Result.success()
            } else {
                // Still failing
                Timber.w("Retry failed for delivery log: $deliveryLogId, attempt: $nextAttempt")

                // Check if we should retry again
                if (nextAttempt < webhookConfig.maxRetries) {
                    // Schedule next retry with exponential backoff
                    scheduleNextRetry(deliveryLogId, nextAttempt, webhookConfig.retryDelayMs)
                    return Result.success() // Worker succeeded in scheduling next retry
                } else {
                    // Max retries reached
                    Timber.e("All retry attempts exhausted for delivery log: $deliveryLogId")
                    deliveryRepository.updateLogStatus(
                        id = deliveryLogId,
                        status = DeliveryStatus.FAILED,
                        attemptNumber = nextAttempt,
                        errorMessage = "All retry attempts failed",
                        timestamp = System.currentTimeMillis()
                    )
                    return Result.failure()
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "Exception in SmsForwardWorker")
            return Result.failure()
        }
    }

    /**
     * Schedules the next retry with exponential backoff
     * Delay increases exponentially: baseDelay * 2^attemptNumber
     * Example with 5s base: 5s, 10s, 20s, 40s...
     */
    private fun scheduleNextRetry(deliveryLogId: Long, attemptNumber: Int, baseDelayMs: Long) {
        val delayMs = baseDelayMs * (1 shl attemptNumber) // Exponential: baseDelay * 2^attemptNumber

        Timber.d("Scheduling next retry in ${delayMs}ms for delivery log: $deliveryLogId")

        val inputData = Data.Builder()
            .putLong(Constants.EXTRA_DELIVERY_LOG_ID, deliveryLogId)
            .build()

        val retryWorkRequest = OneTimeWorkRequestBuilder<SmsForwardWorker>()
            .setInputData(inputData)
            .setInitialDelay(delayMs, TimeUnit.MILLISECONDS)
            .addTag(Constants.SMS_FORWARD_WORKER_TAG)
            .build()

        WorkManager.getInstance(applicationContext).enqueue(retryWorkRequest)
    }

    companion object {
        /**
         * Schedules the initial retry for a failed delivery
         */
        fun scheduleRetry(context: Context, deliveryLogId: Long, delayMs: Long) {
            Timber.d("Scheduling initial retry for delivery log: $deliveryLogId in ${delayMs}ms")

            val inputData = Data.Builder()
                .putLong(Constants.EXTRA_DELIVERY_LOG_ID, deliveryLogId)
                .build()

            val retryWorkRequest = OneTimeWorkRequestBuilder<SmsForwardWorker>()
                .setInputData(inputData)
                .setInitialDelay(delayMs, TimeUnit.MILLISECONDS)
                .addTag(Constants.SMS_FORWARD_WORKER_TAG)
                .build()

            WorkManager.getInstance(context).enqueue(retryWorkRequest)
        }
    }
}
