package com.itechsolution.mufasapay.domain.repository

import com.itechsolution.mufasapay.domain.model.SmsMessage
import com.itechsolution.mufasapay.util.Result
import kotlinx.coroutines.flow.Flow

interface SmsRepository {
    suspend fun saveSms(sms: SmsMessage): Result<Long>
    suspend fun getSmsById(id: Long): Result<SmsMessage?>
    fun getAllSmsFlow(): Flow<List<SmsMessage>>
    suspend fun getRecentSms(limit: Int = 50): Result<List<SmsMessage>>
    fun getSmsBySenderFlow(sender: String): Flow<List<SmsMessage>>
    fun getSmsByForwardedStatusFlow(isForwarded: Boolean): Flow<List<SmsMessage>>
    suspend fun markAsForwarded(id: Long, forwardedAt: Long): Result<Unit>
    suspend fun getTotalCount(): Result<Int>
    fun getTotalCountFlow(): Flow<Int>
    suspend fun getForwardedCount(): Result<Int>
    fun getForwardedCountFlow(): Flow<Int>
}
