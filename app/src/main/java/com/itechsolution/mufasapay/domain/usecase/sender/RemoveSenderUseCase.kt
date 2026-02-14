package com.itechsolution.mufasapay.domain.usecase.sender

import com.itechsolution.mufasapay.domain.repository.SenderRepository
import com.itechsolution.mufasapay.util.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber

class RemoveSenderUseCase(
    private val senderRepository: SenderRepository
) {
    suspend operator fun invoke(senderId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            Timber.d("Removing sender: $senderId")
            senderRepository.removeSender(senderId)
        } catch (e: Exception) {
            Timber.e(e, "Error removing sender")
            Result.error(e, "Failed to remove sender")
        }
    }
}
