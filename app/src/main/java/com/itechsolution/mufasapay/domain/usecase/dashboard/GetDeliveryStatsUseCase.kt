package com.itechsolution.mufasapay.domain.usecase.dashboard

import com.itechsolution.mufasapay.domain.repository.DeliveryRepository
import com.itechsolution.mufasapay.domain.repository.SenderRepository
import com.itechsolution.mufasapay.domain.repository.SmsRepository
import com.itechsolution.mufasapay.util.DateTimeUtils
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

data class DeliveryStats(
    val totalSms: Int,
    val dailyAmountTotal: Double,
    val weeklyAmountTotal: Double,
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
        val dayStart = DateTimeUtils.startOfCurrentDay()
        val nextDayStart = DateTimeUtils.startOfNextDay()
        val weekStart = DateTimeUtils.startOfCurrentWeek()

        return combine(
            combine(
                smsRepository.getTotalCountFlow(),
                smsRepository.getAmountSumBetweenFlow(dayStart, nextDayStart),
                smsRepository.getAmountSumBetweenFlow(weekStart, Long.MAX_VALUE)
            ) { totalSms, dailyAmountTotal, weeklyAmountTotal ->
                Triple(totalSms, dailyAmountTotal, weeklyAmountTotal)
            },
            combine(
                smsRepository.getForwardedCountFlow(),
                deliveryRepository.getSuccessCountFlow(),
                deliveryRepository.getFailedCountFlow(),
                deliveryRepository.getPendingCountFlow()
            ) { forwardedSms, successCount, failedCount, pendingCount ->
                Quadruple(forwardedSms, successCount, failedCount, pendingCount)
            }
        ) { (totalSms, dailyAmountTotal, weeklyAmountTotal), deliveryStats ->
            PartialStats(
                totalSms = totalSms,
                dailyAmountTotal = dailyAmountTotal,
                weeklyAmountTotal = weeklyAmountTotal,
                forwardedSms = deliveryStats.forwardedSms,
                successCount = deliveryStats.successCount,
                failedCount = deliveryStats.failedCount,
                pendingCount = deliveryStats.pendingCount
            )
        }.combine(
            combine(
                deliveryRepository.getRetryingCountFlow(),
                senderRepository.getTotalCountFlow(),
                senderRepository.getEnabledCountFlow()
            ) { retryingCount, totalSenders, enabledSenders ->
                Triple(retryingCount, totalSenders, enabledSenders)
            }
        ) { partial, (retryingCount, totalSenders, enabledSenders) ->
            // forwardedSms comes from smsRepository.getForwardedCountFlow() (unique SMS count)
            // successCount comes from deliveryRepository.getSuccessCountFlow() (unique log count now)
            
            DeliveryStats(
                totalSms = partial.totalSms,
                dailyAmountTotal = partial.dailyAmountTotal,
                weeklyAmountTotal = partial.weeklyAmountTotal,
                forwardedSms = partial.forwardedSms,
                successfulDeliveries = partial.forwardedSms, // SMS marked as forwarded is the gold standard for success
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
        val dailyAmountTotal: Double,
        val weeklyAmountTotal: Double,
        val forwardedSms: Int,
        val successCount: Int,
        val failedCount: Int,
        val pendingCount: Int
    )

    private data class Quadruple(
        val forwardedSms: Int,
        val successCount: Int,
        val failedCount: Int,
        val pendingCount: Int
    )
}
