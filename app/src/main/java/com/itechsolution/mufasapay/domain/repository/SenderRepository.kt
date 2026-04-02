package com.itechsolution.mufasapay.domain.repository

import com.itechsolution.mufasapay.domain.model.Sender
import com.itechsolution.mufasapay.domain.model.SenderTemplate
import com.itechsolution.mufasapay.util.Result
import kotlinx.coroutines.flow.Flow

interface SenderRepository {
    suspend fun addSender(sender: Sender): Result<Unit>
    suspend fun removeSender(senderId: String): Result<Unit>
    suspend fun updateSender(sender: Sender): Result<Unit>
    suspend fun getSenderById(senderId: String): Result<Sender?>
    fun getAllSendersFlow(): Flow<List<Sender>>
    suspend fun getAllSenders(): Result<List<Sender>>
    suspend fun getEnabledSenders(): Result<List<Sender>>
    fun getEnabledSendersFlow(): Flow<List<Sender>>
    suspend fun isSenderWhitelisted(senderId: String): Result<Boolean>
    suspend fun toggleSenderStatus(senderId: String, isEnabled: Boolean): Result<Unit>
    suspend fun updateSenderStatistics(senderId: String, timestamp: Long): Result<Unit>
    suspend fun replaceSenderStatistics(senderId: String, messageCount: Int, lastMessageAt: Long?): Result<Unit>
    suspend fun getTotalCount(): Result<Int>
    fun getTotalCountFlow(): Flow<Int>
    suspend fun getEnabledCount(): Result<Int>
    fun getEnabledCountFlow(): Flow<Int>

    // Template management
    fun getTemplatesForSenderFlow(senderId: String): Flow<List<SenderTemplate>>
    suspend fun getEnabledTemplatesForSender(senderId: String): Result<List<SenderTemplate>>
    suspend fun addTemplate(template: SenderTemplate): Result<Long>
    suspend fun updateTemplate(template: SenderTemplate): Result<Unit>
    suspend fun removeTemplate(templateId: Long): Result<Unit>
}
