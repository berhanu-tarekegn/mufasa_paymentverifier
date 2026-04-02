package com.itechsolution.mufasapay.domain.usecase.history

import com.itechsolution.mufasapay.data.remote.WebhookClientFactory
import com.itechsolution.mufasapay.domain.model.WebhookConfig
import com.itechsolution.mufasapay.domain.repository.SenderRepository
import com.itechsolution.mufasapay.domain.repository.SmsRepository
import com.itechsolution.mufasapay.domain.repository.WebhookRepository
import com.itechsolution.mufasapay.util.Result
import com.itechsolution.mufasapay.util.WebhookUrlResolver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber

class DeleteSmsTransactionUseCase(
    private val smsRepository: SmsRepository,
    private val senderRepository: SenderRepository,
    private val webhookRepository: WebhookRepository,
    private val webhookClientFactory: WebhookClientFactory
) {
    suspend operator fun invoke(smsId: Long): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val smsResult = smsRepository.getSmsById(smsId)
            if (smsResult.isError || smsResult.getOrNull() == null) {
                return@withContext Result.error("SMS not found")
            }

            val sms = smsResult.getOrNull()!!

            if (sms.isForwarded) {
                val deleteRemoteResult = deleteRemoteTransaction(sms.transactionId)
                if (deleteRemoteResult.isError) {
                    return@withContext deleteRemoteResult
                }
            }

            val deleteLocalResult = smsRepository.deleteSms(smsId)
            if (deleteLocalResult.isError) {
                return@withContext deleteLocalResult
            }

            val countResult = smsRepository.getCountBySender(sms.sender)
            if (countResult.isError) {
                Timber.w("Deleted SMS but failed to count sender messages for ${sms.sender}")
                return@withContext Result.success(Unit)
            }

            val lastTimestampResult = smsRepository.getLatestTimestampBySender(sms.sender)
            if (lastTimestampResult.isError) {
                Timber.w("Deleted SMS but failed to get latest sender timestamp for ${sms.sender}")
                return@withContext Result.success(Unit)
            }

            val replaceStatsResult = senderRepository.replaceSenderStatistics(
                senderId = sms.sender,
                messageCount = countResult.getOrNull() ?: 0,
                lastMessageAt = lastTimestampResult.getOrNull()
            )
            if (replaceStatsResult.isError) {
                Timber.w("Deleted SMS but failed to refresh sender statistics for ${sms.sender}")
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Error deleting SMS transaction")
            Result.error(e, "Failed to delete transaction")
        }
    }

    private suspend fun deleteRemoteTransaction(
        transactionId: String?
    ): Result<Unit> {
        val parsedTransactionId = transactionId?.takeIf { it.isNotBlank() }
            ?: return Result.error("Cannot delete synced transaction without transaction ID")

        val configResult = webhookRepository.getConfig()
        if (configResult.isError) {
            return Result.error("Webhook configuration not found")
        }

        val config = configResult.getOrNull()
        if (config == null || config.deleteUrlTemplate.isBlank()) {
            return Result.error("Delete webhook URL is not configured")
        }

        return try {
            val deleteUrl = WebhookUrlResolver.resolveDeleteUrl(config.deleteUrlTemplate, parsedTransactionId)
            val apiService = webhookClientFactory.createService(deleteUrl, config)
            val response = apiService.deleteSms(
                url = deleteUrl,
                headers = buildHeaders(config)
            )

            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                val errorBody = response.errorBody()?.string() ?: "Unknown error"
                Result.error("Server delete failed: HTTP ${response.code()} - $errorBody")
            }
        } catch (e: Exception) {
            Timber.e(e, "Error deleting transaction from server")
            Result.error(e, "Failed to delete from server: ${e.message}")
        }
    }

    private fun buildHeaders(config: WebhookConfig): Map<String, String> {
        val headers = mutableMapOf<String, String>()
        headers["Content-Type"] = "application/json"
        headers["User-Agent"] = "MufasaPay-SMS-Gateway"
        headers.putAll(config.headers)
        return headers
    }
}
