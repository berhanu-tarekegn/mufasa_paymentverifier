package com.itechsolution.mufasapay.domain.usecase.webhook

import com.itechsolution.mufasapay.domain.model.WebhookConfig
import com.itechsolution.mufasapay.domain.repository.WebhookRepository
import kotlinx.coroutines.flow.Flow

class GetWebhookConfigUseCase(
    private val webhookRepository: WebhookRepository
) {
    operator fun invoke(): Flow<WebhookConfig?> {
        return webhookRepository.getConfigFlow()
    }
}
