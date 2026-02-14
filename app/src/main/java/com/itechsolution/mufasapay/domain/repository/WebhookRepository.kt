package com.itechsolution.mufasapay.domain.repository

import com.itechsolution.mufasapay.domain.model.WebhookConfig
import com.itechsolution.mufasapay.util.Result
import kotlinx.coroutines.flow.Flow

interface WebhookRepository {
    suspend fun saveConfig(config: WebhookConfig): Result<Unit>
    suspend fun getConfig(): Result<WebhookConfig?>
    fun getConfigFlow(): Flow<WebhookConfig?>
    suspend fun isEnabled(): Result<Boolean>
    suspend fun updateEnabled(isEnabled: Boolean): Result<Unit>
    suspend fun deleteConfig(): Result<Unit>
}
