package com.itechsolution.mufasapay.data.repository

import com.itechsolution.mufasapay.data.local.dao.DeliveryLogDao
import com.itechsolution.mufasapay.data.local.entity.DeliveryLogEntity
import com.itechsolution.mufasapay.domain.model.DeliveryLog
import com.itechsolution.mufasapay.domain.model.DeliveryStatus
import com.itechsolution.mufasapay.domain.repository.DeliveryRepository
import com.itechsolution.mufasapay.util.Result
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import timber.log.Timber

class DeliveryRepositoryImpl(
    private val deliveryLogDao: DeliveryLogDao
) : DeliveryRepository {

    override suspend fun createLog(log: DeliveryLog): Result<Long> {
        return try {
            val id = deliveryLogDao.insert(log.toEntity())
            Result.success(id)
        } catch (e: Exception) {
            Timber.e(e, "Error creating delivery log")
            Result.error(e, "Failed to create delivery log")
        }
    }

    override suspend fun updateLog(log: DeliveryLog): Result<Unit> {
        return try {
            deliveryLogDao.update(log.toEntity())
            Result.success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Error updating delivery log")
            Result.error(e, "Failed to update delivery log")
        }
    }

    override suspend fun getLogById(id: Long): Result<DeliveryLog?> {
        return try {
            val entity = deliveryLogDao.getById(id)
            Result.success(entity?.toDomain())
        } catch (e: Exception) {
            Timber.e(e, "Error getting delivery log")
            Result.error(e, "Failed to get delivery log")
        }
    }

    override suspend fun getLogsBySmsId(smsId: Long): Result<List<DeliveryLog>> {
        return try {
            val entities = deliveryLogDao.getBySmsId(smsId)
            Result.success(entities.map { it.toDomain() })
        } catch (e: Exception) {
            Timber.e(e, "Error getting delivery logs for SMS")
            Result.error(e, "Failed to get delivery logs")
        }
    }

    override fun getLogsBySmsIdFlow(smsId: Long): Flow<List<DeliveryLog>> {
        return deliveryLogDao.getBySmsIdFlow(smsId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getAllLogsFlow(): Flow<List<DeliveryLog>> {
        return deliveryLogDao.getAllFlow().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getLogsByStatus(status: DeliveryStatus): Result<List<DeliveryLog>> {
        return try {
            val entities = deliveryLogDao.getByStatus(status.name)
            Result.success(entities.map { it.toDomain() })
        } catch (e: Exception) {
            Timber.e(e, "Error getting delivery logs by status")
            Result.error(e, "Failed to get delivery logs")
        }
    }

    override fun getLogsByStatusFlow(status: DeliveryStatus): Flow<List<DeliveryLog>> {
        return deliveryLogDao.getByStatusFlow(status.name).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getFailedLogsWithRetriesLeft(maxRetries: Int): Result<List<DeliveryLog>> {
        return try {
            val entities = deliveryLogDao.getFailedWithRetriesLeft(maxRetries)
            Result.success(entities.map { it.toDomain() })
        } catch (e: Exception) {
            Timber.e(e, "Error getting failed logs with retries")
            Result.error(e, "Failed to get delivery logs")
        }
    }

    override suspend fun updateLogStatus(
        id: Long,
        status: DeliveryStatus,
        attemptNumber: Int,
        errorMessage: String?,
        timestamp: Long
    ): Result<Unit> {
        return try {
            deliveryLogDao.updateStatus(id, status.name, attemptNumber, errorMessage, timestamp)
            Result.success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Error updating delivery log status")
            Result.error(e, "Failed to update log status")
        }
    }

    override suspend fun updateLogSuccess(
        id: Long,
        status: DeliveryStatus,
        responseCode: Int,
        responseBody: String?,
        duration: Long,
        timestamp: Long
    ): Result<Unit> {
        return try {
            deliveryLogDao.updateSuccess(id, status.name, responseCode, responseBody, duration, timestamp)
            Result.success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Error updating delivery log success")
            Result.error(e, "Failed to update log")
        }
    }

    override suspend fun getSuccessCount(): Result<Int> {
        return try {
            val count = deliveryLogDao.countSuccess()
            Result.success(count)
        } catch (e: Exception) {
            Timber.e(e, "Error getting success count")
            Result.error(e, "Failed to get count")
        }
    }

    override fun getSuccessCountFlow(): Flow<Int> = deliveryLogDao.countSuccessFlow()

    override suspend fun getFailedCount(): Result<Int> {
        return try {
            val count = deliveryLogDao.countFailed()
            Result.success(count)
        } catch (e: Exception) {
            Timber.e(e, "Error getting failed count")
            Result.error(e, "Failed to get count")
        }
    }

    override fun getFailedCountFlow(): Flow<Int> = deliveryLogDao.countFailedFlow()

    override suspend fun getPendingCount(): Result<Int> {
        return try {
            val count = deliveryLogDao.countPending()
            Result.success(count)
        } catch (e: Exception) {
            Timber.e(e, "Error getting pending count")
            Result.error(e, "Failed to get count")
        }
    }

    override fun getPendingCountFlow(): Flow<Int> = deliveryLogDao.countPendingFlow()

    override suspend fun getRetryingCount(): Result<Int> {
        return try {
            val count = deliveryLogDao.countRetrying()
            Result.success(count)
        } catch (e: Exception) {
            Timber.e(e, "Error getting retrying count")
            Result.error(e, "Failed to get count")
        }
    }

    override fun getRetryingCountFlow(): Flow<Int> = deliveryLogDao.countRetryingFlow()

    private fun DeliveryLog.toEntity() = DeliveryLogEntity(
        id = id,
        smsId = smsId,
        status = status.name,
        attemptNumber = attemptNumber,
        requestPayload = requestPayload,
        responseCode = responseCode,
        responseBody = responseBody,
        errorMessage = errorMessage,
        timestamp = timestamp,
        duration = duration
    )

    private fun DeliveryLogEntity.toDomain() = DeliveryLog(
        id = id,
        smsId = smsId,
        status = DeliveryStatus.fromString(status),
        attemptNumber = attemptNumber,
        requestPayload = requestPayload,
        responseCode = responseCode,
        responseBody = responseBody,
        errorMessage = errorMessage,
        timestamp = timestamp,
        duration = duration
    )
}
