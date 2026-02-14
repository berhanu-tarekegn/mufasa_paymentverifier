package com.itechsolution.mufasapay.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.itechsolution.mufasapay.data.local.entity.DeliveryLogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DeliveryLogDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(log: DeliveryLogEntity): Long

    @Update
    suspend fun update(log: DeliveryLogEntity)

    @Delete
    suspend fun delete(log: DeliveryLogEntity)

    @Query("SELECT * FROM delivery_logs WHERE id = :id")
    suspend fun getById(id: Long): DeliveryLogEntity?

    @Query("SELECT * FROM delivery_logs WHERE smsId = :smsId ORDER BY timestamp DESC")
    suspend fun getBySmsId(smsId: Long): List<DeliveryLogEntity>

    @Query("SELECT * FROM delivery_logs WHERE smsId = :smsId ORDER BY timestamp DESC")
    fun getBySmsIdFlow(smsId: Long): Flow<List<DeliveryLogEntity>>

    @Query("SELECT * FROM delivery_logs ORDER BY timestamp DESC")
    fun getAllFlow(): Flow<List<DeliveryLogEntity>>

    @Query("SELECT * FROM delivery_logs WHERE status = :status ORDER BY timestamp DESC")
    suspend fun getByStatus(status: String): List<DeliveryLogEntity>

    @Query("SELECT * FROM delivery_logs WHERE status = :status ORDER BY timestamp DESC")
    fun getByStatusFlow(status: String): Flow<List<DeliveryLogEntity>>

    @Query("SELECT * FROM delivery_logs WHERE status = 'FAILED' AND attemptNumber < :maxRetries ORDER BY timestamp ASC")
    suspend fun getFailedWithRetriesLeft(maxRetries: Int): List<DeliveryLogEntity>

    @Query("UPDATE delivery_logs SET status = :status, attemptNumber = :attemptNumber, errorMessage = :errorMessage, timestamp = :timestamp WHERE id = :id")
    suspend fun updateStatus(id: Long, status: String, attemptNumber: Int, errorMessage: String?, timestamp: Long)

    @Query("UPDATE delivery_logs SET status = :status, responseCode = :responseCode, responseBody = :responseBody, duration = :duration, timestamp = :timestamp WHERE id = :id")
    suspend fun updateSuccess(id: Long, status: String, responseCode: Int, responseBody: String?, duration: Long, timestamp: Long)

    @Query("SELECT COUNT(*) FROM delivery_logs WHERE status = 'SUCCESS'")
    suspend fun countSuccess(): Int

    @Query("SELECT COUNT(*) FROM delivery_logs WHERE status = 'SUCCESS'")
    fun countSuccessFlow(): Flow<Int>

    @Query("SELECT COUNT(*) FROM delivery_logs WHERE status = 'FAILED'")
    suspend fun countFailed(): Int

    @Query("SELECT COUNT(*) FROM delivery_logs WHERE status = 'FAILED'")
    fun countFailedFlow(): Flow<Int>

    @Query("SELECT COUNT(*) FROM delivery_logs WHERE status = 'PENDING'")
    suspend fun countPending(): Int

    @Query("SELECT COUNT(*) FROM delivery_logs WHERE status = 'PENDING'")
    fun countPendingFlow(): Flow<Int>

    @Query("SELECT COUNT(*) FROM delivery_logs WHERE status = 'RETRYING'")
    suspend fun countRetrying(): Int

    @Query("SELECT COUNT(*) FROM delivery_logs WHERE status = 'RETRYING'")
    fun countRetryingFlow(): Flow<Int>
}
