package com.itechsolution.mufasapay.domain.usecase

import com.itechsolution.mufasapay.domain.model.SmsMessage
import com.itechsolution.mufasapay.domain.repository.SenderRepository
import com.itechsolution.mufasapay.domain.repository.SmsRepository
import com.itechsolution.mufasapay.util.DateTimeUtils
import com.itechsolution.mufasapay.util.Result
import com.itechsolution.mufasapay.util.SmsPatternExtractor
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber

/**
 * Core use case for processing incoming SMS messages
 * Orchestrates the entire SMS processing pipeline:
 * 1. Check sender whitelist
 * 2. Save SMS to database
 * 3. Update sender statistics
 * 4. Forward to webhook
 *
 * This is the MAIN ENTRY POINT for all SMS processing
 */
class ProcessIncomingSmsUseCase(
    private val senderRepository: SenderRepository,
    private val smsRepository: SmsRepository,
    private val forwardSmsToWebhookUseCase: ForwardSmsToWebhookUseCase,
    moshi: Moshi
) {
    private val parsedFieldsAdapter = moshi.adapter<Map<String, String>>(
        Types.newParameterizedType(Map::class.java, String::class.java, String::class.java)
    )

    suspend operator fun invoke(
        sender: String,
        message: String,
        timestamp: Long = DateTimeUtils.getCurrentTimestamp()
    ): Result<Unit> = withContext(Dispatchers.IO) {
        Timber.d("Processing incoming SMS from: $sender")

        try {
            // 1. Check if sender is whitelisted
            val isWhitelisted = senderRepository.isSenderWhitelisted(sender)
            if (isWhitelisted.isError) {
                Timber.e("Error checking sender whitelist: ${isWhitelisted.exceptionOrNull()}")
                return@withContext Result.error("Failed to check sender whitelist")
            }

            if (isWhitelisted.getOrNull() != true) {
                Timber.d("Sender not whitelisted, discarding SMS: $sender")
                return@withContext Result.success(Unit)
            }

            Timber.i("Sender whitelisted, processing SMS: $sender")

            // 1.5 Check if message matches any sender template
            val senderResult = senderRepository.getSenderById(sender)
            if (senderResult.isError) {
                Timber.e("Failed to load sender details: ${senderResult.exceptionOrNull()}")
                return@withContext Result.error("Failed to load sender configuration")
            }

            val senderData = senderResult.getOrNull()
            if (senderData == null) {
                Timber.w("Sender was whitelisted but not found in repository: $sender")
                return@withContext Result.error("Sender configuration is missing")
            }

            // Get enabled template patterns for this sender
            val templatePatterns = senderData.templates
                .filter { it.isEnabled }
                .map { it.pattern }

            if (templatePatterns.isEmpty()) {
                Timber.i("No enabled templates configured for sender, skipping: $sender")
                return@withContext Result.success(Unit)
            }

            val parsedMatch = SmsPatternExtractor.extractFirstMatch(message, templatePatterns)
            if (parsedMatch == null) {
                Timber.d("Message does not match any template pattern, skipping: $sender")
                return@withContext Result.success(Unit)
            }

            val parsedAmount = parsedMatch.amount
            val parsedName = parsedMatch.values["name"]?.trim()
            val parsedTransactionId = parsedMatch.transactionId?.trim()
            if (parsedAmount == null || parsedTransactionId.isNullOrBlank() || parsedName.isNullOrBlank()) {
                Timber.w("Matched template did not extract required name/amount/transaction fields for sender: $sender")
                return@withContext Result.error("Matched template must extract sender name, amount, and transaction ID")
            }

            Timber.i("Message matches template and extracted name, amount, and transaction ID, continuing processing")

            // 2. Save SMS to database
            val sms = SmsMessage(
                sender = sender,
                message = message,
                timestamp = timestamp,
                amount = parsedAmount,
                transactionId = parsedTransactionId,
                rawJson = parsedFieldsAdapter.toJson(parsedMatch.values),
                isForwarded = false
            )

            val saveSmsResult = smsRepository.saveSms(sms)
            if (saveSmsResult.isError) {
                Timber.e("Failed to save SMS: ${saveSmsResult.exceptionOrNull()}")
                return@withContext Result.error("Failed to save SMS")
            }

            val smsId = saveSmsResult.getOrNull() ?: 0L
            if (smsId == -1L) {
                Timber.i("SMS already processed, skipping duplicate: $sender")
                return@withContext Result.success(Unit)
            }
            Timber.d("SMS saved with ID: $smsId")

            // 3. Update sender statistics
            val updateStatsResult = senderRepository.updateSenderStatistics(sender, timestamp)
            if (updateStatsResult.isError) {
                Timber.w("Failed to update sender statistics: ${updateStatsResult.exceptionOrNull()}")
                // Non-critical, continue processing
            }

            // 4. Forward to webhook
            val smsWithId = sms.copy(id = smsId)
            val forwardResult = forwardSmsToWebhookUseCase(smsWithId)

            if (forwardResult.isSuccess) {
                Timber.i("SMS processed and forwarded successfully. ID: $smsId")
                return@withContext Result.success(Unit)
            } else {
                Timber.w("SMS saved but forwarding failed. ID: $smsId, Error: ${forwardResult.exceptionOrNull()?.message}")
                // Return success because SMS was saved, forwarding will be retried by WorkManager
                return@withContext Result.success(Unit)
            }
        } catch (e: Exception) {
            Timber.e(e, "Exception during SMS processing")
            return@withContext Result.error(e, "Failed to process SMS: ${e.message}")
        }
    }
}
