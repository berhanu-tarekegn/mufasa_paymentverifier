package com.itechsolution.mufasapay.domain.usecase.sender

import com.itechsolution.mufasapay.domain.repository.SenderRepository
import com.itechsolution.mufasapay.util.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber

class ToggleSenderStatusUseCase(
    private val senderRepository: SenderRepository
) {
    suspend operator fun invoke(senderId: String, isEnabled: Boolean): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            Timber.d("Toggling sender status: $senderId -> $isEnabled")
            senderRepository.toggleSenderStatus(senderId, isEnabled)
//            senderRepository.getAllSenders()
        } catch (e: Exception) {
            Timber.e(e, "Error toggling sender status")
            Result.error(e, "Failed to update sender status")
        }
    }
}
