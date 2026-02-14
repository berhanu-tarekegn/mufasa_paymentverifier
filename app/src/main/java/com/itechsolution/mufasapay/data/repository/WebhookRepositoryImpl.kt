package com.itechsolution.mufasapay.data.repository

import com.itechsolution.mufasapay.data.local.dao.WebhookConfigDao
import com.itechsolution.mufasapay.data.local.entity.WebhookConfigEntity
import com.itechsolution.mufasapay.domain.model.WebhookConfig
import com.itechsolution.mufasapay.domain.repository.WebhookRepository
import com.itechsolution.mufasapay.util.Result
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import timber.log.Timber

class WebhookRepositoryImpl(
    private val webhookConfigDao: WebhookConfigDao,
    private val moshi: Moshi
) : WebhookRepository {

    private val headersAdapter = moshi.adapter<Map<String, String>>(
        Types.newParameterizedType(Map::class.java, String::class.java, String::class.java)
    )

    override suspend fun saveConfig(config: WebhookConfig): Result<Unit> {
        return try {
            webhookConfigDao.insert(config.toEntity())
            Result.success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Error saving webhook config")
            Result.error(e, "Failed to save webhook configuration")
        }
    }

    override suspend fun getConfig(): Result<WebhookConfig?> {
        return try {
            val entity = webhookConfigDao.get()
            Result.success(entity?.toDomain())
        } catch (e: Exception) {
            Timber.e(e, "Error getting webhook config")
            Result.error(e, "Failed to get webhook configuration")
        }
    }

    override fun getConfigFlow(): Flow<WebhookConfig?> {
        return webhookConfigDao.getFlow().map { entity ->
            entity?.toDomain()
        }
    }

    override suspend fun isEnabled(): Result<Boolean> {
        return try {
            val enabled = webhookConfigDao.isEnabled()
            Result.success(enabled)
        } catch (e: Exception) {
            Timber.e(e, "Error checking webhook enabled status")
            Result.error(e, "Failed to check webhook status")
        }
    }

    override suspend fun updateEnabled(isEnabled: Boolean): Result<Unit> {
        return try {
            webhookConfigDao.updateEnabled(isEnabled)
            Result.success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Error updating webhook enabled status")
            Result.error(e, "Failed to update webhook status")
        }
    }

    override suspend fun deleteConfig(): Result<Unit> {
        return try {
            webhookConfigDao.delete()
            Result.success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Error deleting webhook config")
            Result.error(e, "Failed to delete webhook configuration")
        }
    }

    private fun WebhookConfig.toEntity(): WebhookConfigEntity {
        val headersJson = headersAdapter.toJson(headers)
        return WebhookConfigEntity(
            id = id,
            url = url,
            method = method,
            headers = headersJson,
            authType = authType,
            authValue = authValue,
            timeout = timeout,
            retryEnabled = retryEnabled,
            maxRetries = maxRetries,
            retryDelayMs = retryDelayMs,
            isEnabled = isEnabled,
            updatedAt = updatedAt
        )
    }

    private fun WebhookConfigEntity.toDomain(): WebhookConfig {
        val headersMap = try {
            headersAdapter.fromJson(headers) ?: emptyMap()
        } catch (e: Exception) {
            Timber.e(e, "Error parsing headers JSON")
            emptyMap()
        }
        return WebhookConfig(
            id = id,
            url = url,
            method = method,
            headers = headersMap,
            authType = authType,
            authValue = authValue,
            timeout = timeout,
            retryEnabled = retryEnabled,
            maxRetries = maxRetries,
            retryDelayMs = retryDelayMs,
            isEnabled = isEnabled,
            updatedAt = updatedAt
        )
    }
}
