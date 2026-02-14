package com.itechsolution.mufasapay.data.repository

import com.itechsolution.mufasapay.data.local.dao.SmsMessageDao
import com.itechsolution.mufasapay.data.local.entity.SmsMessageEntity
import com.itechsolution.mufasapay.domain.model.SmsMessage
import com.itechsolution.mufasapay.domain.repository.SmsRepository
import com.itechsolution.mufasapay.util.Result
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import timber.log.Timber

class SmsRepositoryImpl(
    private val smsMessageDao: SmsMessageDao
) : SmsRepository {

    override suspend fun saveSms(sms: SmsMessage): Result<Long> {
        return try {
            val id = smsMessageDao.insert(sms.toEntity())
            Result.success(id)
        } catch (e: Exception) {
            Timber.e(e, "Error saving SMS")
            Result.error(e, "Failed to save SMS")
        }
    }

    override suspend fun getSmsById(id: Long): Result<SmsMessage?> {
        return try {
            val entity = smsMessageDao.getById(id)
            Result.success(entity?.toDomain())
        } catch (e: Exception) {
            Timber.e(e, "Error getting SMS by ID")
            Result.error(e, "Failed to get SMS")
        }
    }

    override fun getAllSmsFlow(): Flow<List<SmsMessage>> {
        return smsMessageDao.getAllFlow().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getRecentSms(limit: Int): Result<List<SmsMessage>> {
        return try {
            val entities = smsMessageDao.getRecent(limit)
            Result.success(entities.map { it.toDomain() })
        } catch (e: Exception) {
            Timber.e(e, "Error getting recent SMS")
            Result.error(e, "Failed to get recent SMS")
        }
    }

    override fun getSmsBySenderFlow(sender: String): Flow<List<SmsMessage>> {
        return smsMessageDao.getBySenderFlow(sender).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getSmsByForwardedStatusFlow(isForwarded: Boolean): Flow<List<SmsMessage>> {
        return smsMessageDao.getByForwardedStatusFlow(isForwarded).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun markAsForwarded(id: Long, forwardedAt: Long): Result<Unit> {
        return try {
            smsMessageDao.markAsForwarded(id, forwardedAt)
            Result.success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Error marking SMS as forwarded")
            Result.error(e, "Failed to update SMS")
        }
    }

    override suspend fun getTotalCount(): Result<Int> {
        return try {
            val count = smsMessageDao.count()
            Result.success(count)
        } catch (e: Exception) {
            Timber.e(e, "Error getting total SMS count")
            Result.error(e, "Failed to get count")
        }
    }

    override fun getTotalCountFlow(): Flow<Int> = smsMessageDao.countFlow()

    override suspend fun getForwardedCount(): Result<Int> {
        return try {
            val count = smsMessageDao.countForwarded()
            Result.success(count)
        } catch (e: Exception) {
            Timber.e(e, "Error getting forwarded SMS count")
            Result.error(e, "Failed to get count")
        }
    }

    override fun getForwardedCountFlow(): Flow<Int> = smsMessageDao.countForwardedFlow()

    private fun SmsMessage.toEntity() = SmsMessageEntity(
        id = id,
        sender = sender,
        message = message,
        timestamp = timestamp,
        rawJson = rawJson,
        isForwarded = isForwarded,
        forwardedAt = forwardedAt
    )

    private fun SmsMessageEntity.toDomain() = SmsMessage(
        id = id,
        sender = sender,
        message = message,
        timestamp = timestamp,
        rawJson = rawJson,
        isForwarded = isForwarded,
        forwardedAt = forwardedAt
    )
}
