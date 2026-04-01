package com.itechsolution.mufasapay.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.itechsolution.mufasapay.data.local.entity.SmsMessageEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SmsMessageDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(sms: SmsMessageEntity): Long

    @Update
    suspend fun update(sms: SmsMessageEntity)

    @Delete
    suspend fun delete(sms: SmsMessageEntity)

    @Query("SELECT * FROM sms_messages WHERE id = :id")
    suspend fun getById(id: Long): SmsMessageEntity?

    @Query("SELECT * FROM sms_messages ORDER BY timestamp DESC")
    fun getAllFlow(): Flow<List<SmsMessageEntity>>

    @Query("SELECT * FROM sms_messages ORDER BY timestamp DESC LIMIT :limit")
    suspend fun getRecent(limit: Int = 50): List<SmsMessageEntity>

    @Query("SELECT * FROM sms_messages WHERE sender = :sender ORDER BY timestamp DESC")
    fun getBySenderFlow(sender: String): Flow<List<SmsMessageEntity>>

    @Query("SELECT * FROM sms_messages WHERE isForwarded = :isForwarded ORDER BY timestamp DESC")
    fun getByForwardedStatusFlow(isForwarded: Boolean): Flow<List<SmsMessageEntity>>

    @Query("SELECT * FROM sms_messages WHERE timestamp >= :startTime AND timestamp <= :endTime ORDER BY timestamp DESC")
    suspend fun getBetweenDates(startTime: Long, endTime: Long): List<SmsMessageEntity>

    @Query("DELETE FROM sms_messages WHERE timestamp < :cutoffTimestamp")
    suspend fun deleteOlderThan(cutoffTimestamp: Long): Int

    @Query("SELECT COUNT(*) FROM sms_messages")
    suspend fun count(): Int

    @Query("SELECT COUNT(*) FROM sms_messages")
    fun countFlow(): Flow<Int>

    @Query("SELECT COUNT(*) FROM sms_messages WHERE isForwarded = 1")
    suspend fun countForwarded(): Int

    @Query("SELECT COUNT(*) FROM sms_messages WHERE isForwarded = 1")
    fun countForwardedFlow(): Flow<Int>

    @Query("SELECT COALESCE(SUM(amount), 0) FROM sms_messages WHERE timestamp >= :startTime AND timestamp < :endTime")
    fun sumAmountBetweenFlow(startTime: Long, endTime: Long): Flow<Double>

    @Query("SELECT COUNT(*) FROM sms_messages WHERE sender = :sender")
    suspend fun countBySender(sender: String): Int

    @Query("UPDATE sms_messages SET isForwarded = 1, forwardedAt = :forwardedAt WHERE id = :id")
    suspend fun markAsForwarded(id: Long, forwardedAt: Long)
}
