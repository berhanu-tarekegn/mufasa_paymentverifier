package com.itechsolution.mufasapay.domain.usecase.history

import com.itechsolution.mufasapay.domain.model.SmsMessage
import com.itechsolution.mufasapay.domain.repository.SmsRepository
import kotlinx.coroutines.flow.Flow

class GetSmsHistoryUseCase(
    private val smsRepository: SmsRepository
) {
    operator fun invoke(): Flow<List<SmsMessage>> {
        return smsRepository.getAllSmsFlow()
    }
}
