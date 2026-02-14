package com.itechsolution.mufasapay.domain.usecase.sender

import com.itechsolution.mufasapay.domain.model.Sender
import com.itechsolution.mufasapay.domain.repository.SenderRepository
import com.itechsolution.mufasapay.util.DateTimeUtils
import com.itechsolution.mufasapay.util.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber

class AddSenderUseCase(
    private val senderRepository: SenderRepository
) {
    suspend operator fun invoke(
        senderId: String,
        displayName: String,
        pattern: String? = null
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            if (senderId.isBlank()) {
                return@withContext Result.error("Sender ID cannot be empty")
            }

            if (displayName.isBlank()) {
                return@withContext Result.error("Display name cannot be empty")
            }

            val sender = Sender(
                senderId = senderId.trim(),
                displayName = displayName.trim(),
                pattern = pattern?.trim(),
                isEnabled = true,
                addedAt = DateTimeUtils.getCurrentTimestamp(),
                messageCount = 0
            )

            Timber.d("Adding sender: $senderId")
            senderRepository.addSender(sender)
        } catch (e: Exception) {
            Timber.e(e, "Error adding sender")
            Result.error(e, "Failed to add sender")
        }
    }
}
