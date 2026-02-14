package com.itechsolution.mufasapay.domain.usecase.dashboard

import com.itechsolution.mufasapay.domain.repository.DeliveryRepository
import com.itechsolution.mufasapay.domain.repository.SenderRepository
import com.itechsolution.mufasapay.domain.repository.SmsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

data class DeliveryStats(
    val totalSms: Int,
    val forwardedSms: Int,
    val successfulDeliveries: Int,
    val failedDeliveries: Int,
    val pendingDeliveries: Int,
    val retryingDeliveries: Int,
    val totalSenders: Int,
    val enabledSenders: Int
)

class GetDeliveryStatsUseCase(
    private val smsRepository: SmsRepository,
    private val deliveryRepository: DeliveryRepository,
    private val senderRepository: SenderRepository
) {
    operator fun invoke(): Flow<DeliveryStats> {
        return combine(
            smsRepository.getTotalCountFlow(),
            smsRepository.getForwardedCountFlow(),
            deliveryRepository.getSuccessCountFlow(),
            deliveryRepository.getFailedCountFlow(),
            deliveryRepository.getPendingCountFlow(),
        ) { totalSms, forwardedSms, successCount, failedCount, pendingCount ->
            PartialStats(totalSms, forwardedSms, successCount, failedCount, pendingCount)
        }.combine(
            combine(
                deliveryRepository.getRetryingCountFlow(),
                senderRepository.getTotalCountFlow(),
                senderRepository.getEnabledCountFlow()
            ) { retryingCount, totalSenders, enabledSenders ->
                Triple(retryingCount, totalSenders, enabledSenders)
            }
        ) { partial, (retryingCount, totalSenders, enabledSenders) ->
            DeliveryStats(
                totalSms = partial.totalSms,
                forwardedSms = partial.forwardedSms,
                successfulDeliveries = partial.successCount,
                failedDeliveries = partial.failedCount,
                pendingDeliveries = partial.pendingCount,
                retryingDeliveries = retryingCount,
                totalSenders = totalSenders,
                enabledSenders = enabledSenders
            )
        }
    }

    private data class PartialStats(
        val totalSms: Int,
        val forwardedSms: Int,
        val successCount: Int,
        val failedCount: Int,
        val pendingCount: Int
    )
}
