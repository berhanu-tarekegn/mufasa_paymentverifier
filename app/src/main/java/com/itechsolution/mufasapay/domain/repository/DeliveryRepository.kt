package com.itechsolution.mufasapay.domain.repository

import com.itechsolution.mufasapay.domain.model.DeliveryLog
import com.itechsolution.mufasapay.domain.model.DeliveryStatus
import com.itechsolution.mufasapay.util.Result
import kotlinx.coroutines.flow.Flow

interface DeliveryRepository {
    suspend fun createLog(log: DeliveryLog): Result<Long>
    suspend fun updateLog(log: DeliveryLog): Result<Unit>
    suspend fun getLogById(id: Long): Result<DeliveryLog?>
    suspend fun getLogsBySmsId(smsId: Long): Result<List<DeliveryLog>>
    fun getLogsBySmsIdFlow(smsId: Long): Flow<List<DeliveryLog>>
    fun getAllLogsFlow(): Flow<List<DeliveryLog>>
    suspend fun getLogsByStatus(status: DeliveryStatus): Result<List<DeliveryLog>>
    fun getLogsByStatusFlow(status: DeliveryStatus): Flow<List<DeliveryLog>>
    suspend fun getFailedLogsWithRetriesLeft(maxRetries: Int): Result<List<DeliveryLog>>
    suspend fun updateLogStatus(id: Long, status: DeliveryStatus, attemptNumber: Int, errorMessage: String?, timestamp: Long): Result<Unit>
    suspend fun updateLogSuccess(id: Long, status: DeliveryStatus, responseCode: Int, responseBody: String?, duration: Long, timestamp: Long): Result<Unit>
    suspend fun getSuccessCount(): Result<Int>
    fun getSuccessCountFlow(): Flow<Int>
    suspend fun getFailedCount(): Result<Int>
    fun getFailedCountFlow(): Flow<Int>
    suspend fun getPendingCount(): Result<Int>
    fun getPendingCountFlow(): Flow<Int>
    suspend fun getRetryingCount(): Result<Int>
    fun getRetryingCountFlow(): Flow<Int>
}
