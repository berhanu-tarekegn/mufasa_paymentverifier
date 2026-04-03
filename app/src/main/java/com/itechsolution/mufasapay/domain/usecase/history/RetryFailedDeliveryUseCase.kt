package com.itechsolution.mufasapay.domain.usecase.history

import com.itechsolution.mufasapay.domain.repository.DeliveryRepository
import com.itechsolution.mufasapay.domain.repository.SmsRepository
import com.itechsolution.mufasapay.domain.usecase.ForwardSmsToWebhookUseCase
import com.itechsolution.mufasapay.util.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber

/**
 * Use case for manually retrying a failed delivery
 */
class RetryFailedDeliveryUseCase(
    private val smsRepository: SmsRepository,
    private val deliveryRepository: DeliveryRepository,
    private val forwardSmsToWebhookUseCase: ForwardSmsToWebhookUseCase
) {
    suspend operator fun invoke(smsId: Long): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            Timber.d("Manually retrying failed delivery for SMS ID: $smsId")

            val smsResult = smsRepository.getSmsById(smsId)
            if (smsResult.isError || smsResult.getOrNull() == null) {
                return@withContext Result.error("SMS not found")
            }

            val sms = smsResult.getOrNull()!!
            val latestLogId = deliveryRepository.getLogsBySmsId(smsId)
                .getOrNull()
                ?.firstOrNull()
                ?.id

            val result = forwardSmsToWebhookUseCase(sms, latestLogId)

            if (result.isSuccess) {
                Timber.i("Manual retry successful for SMS ID: $smsId")
                Result.success(Unit)
            } else {
                Timber.w("Manual retry failed for SMS ID: $smsId")
                Result.error("Retry failed: ${result.exceptionOrNull()?.message}")
            }
        } catch (e: Exception) {
            Timber.e(e, "Error retrying delivery")
            Result.error(e, "Failed to retry delivery")
        }
    }
}
