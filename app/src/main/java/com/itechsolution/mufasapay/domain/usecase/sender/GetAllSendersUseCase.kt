package com.itechsolution.mufasapay.domain.usecase.sender

import com.itechsolution.mufasapay.domain.model.Sender
import com.itechsolution.mufasapay.domain.repository.SenderRepository
import kotlinx.coroutines.flow.Flow

class GetAllSendersUseCase(
    private val senderRepository: SenderRepository
) {
    operator fun invoke(): Flow<List<Sender>> {
        return senderRepository.getAllSendersFlow()
    }
}
