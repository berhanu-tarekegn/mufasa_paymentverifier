package com.itechsolution.mufasapay.data.repository

import com.itechsolution.mufasapay.data.local.dao.SenderDao
import com.itechsolution.mufasapay.data.local.dao.SenderTemplateDao
import com.itechsolution.mufasapay.data.local.entity.SenderEntity
import com.itechsolution.mufasapay.data.local.entity.SenderTemplateEntity
import com.itechsolution.mufasapay.data.local.entity.SenderWithTemplates
import com.itechsolution.mufasapay.domain.model.Sender
import com.itechsolution.mufasapay.domain.model.SenderTemplate
import com.itechsolution.mufasapay.domain.repository.SenderRepository
import com.itechsolution.mufasapay.util.Result
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import timber.log.Timber

class SenderRepositoryImpl(
    private val senderDao: SenderDao,
    private val senderTemplateDao: SenderTemplateDao
) : SenderRepository {

    override suspend fun addSender(sender: Sender): Result<Unit> {
        return try {
            senderDao.insert(sender.toEntity())
            Result.success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Error adding sender")
            Result.error(e, "Failed to add sender")
        }
    }

    override suspend fun removeSender(senderId: String): Result<Unit> {
        return try {
            val sender = senderDao.getById(senderId)
            if (sender != null) {
                // Templates are cascade-deleted via foreign key
                senderDao.delete(sender)
                Result.success(Unit)
            } else {
                Result.error("Sender not found")
            }
        } catch (e: Exception) {
            Timber.e(e, "Error removing sender")
            Result.error(e, "Failed to remove sender")
        }
    }

    override suspend fun updateSender(sender: Sender): Result<Unit> {
        return try {
            senderDao.update(sender.toEntity())
            Result.success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Error updating sender")
            Result.error(e, "Failed to update sender")
        }
    }

    override suspend fun getSenderById(senderId: String): Result<Sender?> {
        return try {
            Result.success(senderDao.getByIdWithTemplates(senderId)?.toDomain())
        } catch (e: Exception) {
            Timber.e(e, "Error getting sender")
            Result.error(e, "Failed to get sender")
        }
    }

    override fun getAllSendersFlow(): Flow<List<Sender>> {
        return senderDao.getAllWithTemplatesFlow().map { senders ->
            senders.map { it.toDomain() }
        }
    }

    override suspend fun getAllSenders(): Result<List<Sender>> {
        return try {
            Result.success(senderDao.getAllWithTemplates().map { it.toDomain() })
        } catch (e: Exception) {
            Timber.e(e, "Error getting all senders")
            Result.error(e, "Failed to get senders")
        }
    }

    override suspend fun getEnabledSenders(): Result<List<Sender>> {
        return try {
            Result.success(senderDao.getEnabledWithTemplates().map { it.toDomain() })
        } catch (e: Exception) {
            Timber.e(e, "Error getting enabled senders")
            Result.error(e, "Failed to get enabled senders")
        }
    }

    override fun getEnabledSendersFlow(): Flow<List<Sender>> {
        return senderDao.getEnabledWithTemplatesFlow().map { senders ->
            senders.map { it.toDomain() }
        }
    }

    override suspend fun isSenderWhitelisted(senderId: String): Result<Boolean> {
        return try {
            val isWhitelisted = senderDao.isWhitelisted(senderId)
            Result.success(isWhitelisted)
        } catch (e: Exception) {
            Timber.e(e, "Error checking sender whitelist status")
            Result.error(e, "Failed to check whitelist")
        }
    }

    override suspend fun toggleSenderStatus(senderId: String, isEnabled: Boolean): Result<Unit> {
        return try {
            senderDao.updateEnabled(senderId, isEnabled)
            Result.success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Error toggling sender status")
            Result.error(e, "Failed to update sender status")
        }
    }

    override suspend fun updateSenderStatistics(senderId: String, timestamp: Long): Result<Unit> {
        return try {
            senderDao.updateStatistics(senderId, timestamp)
            Result.success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Error updating sender statistics")
            Result.error(e, "Failed to update statistics")
        }
    }

    override suspend fun replaceSenderStatistics(senderId: String, messageCount: Int, lastMessageAt: Long?): Result<Unit> {
        return try {
            senderDao.replaceStatistics(senderId, lastMessageAt, messageCount)
            Result.success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Error replacing sender statistics")
            Result.error(e, "Failed to replace sender statistics")
        }
    }

    override suspend fun getTotalCount(): Result<Int> {
        return try {
            val count = senderDao.count()
            Result.success(count)
        } catch (e: Exception) {
            Timber.e(e, "Error getting sender count")
            Result.error(e, "Failed to get count")
        }
    }

    override fun getTotalCountFlow(): Flow<Int> = senderDao.countFlow()

    override suspend fun getEnabledCount(): Result<Int> {
        return try {
            val count = senderDao.countEnabled()
            Result.success(count)
        } catch (e: Exception) {
            Timber.e(e, "Error getting enabled sender count")
            Result.error(e, "Failed to get count")
        }
    }

    override fun getEnabledCountFlow(): Flow<Int> = senderDao.countEnabledFlow()

    // --- Template management ---

    override fun getTemplatesForSenderFlow(senderId: String): Flow<List<SenderTemplate>> {
        return senderTemplateDao.getTemplatesForSenderFlow(senderId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getEnabledTemplatesForSender(senderId: String): Result<List<SenderTemplate>> {
        return try {
            val templates = senderTemplateDao.getEnabledTemplatesForSender(senderId)
            Result.success(templates.map { it.toDomain() })
        } catch (e: Exception) {
            Timber.e(e, "Error getting enabled templates for sender: $senderId")
            Result.error(e, "Failed to get templates")
        }
    }

    override suspend fun addTemplate(template: SenderTemplate): Result<Long> {
        return try {
            val id = senderTemplateDao.insert(template.toEntity())
            Timber.d("Added template '${template.label}' for sender: ${template.senderId}")
            Result.success(id)
        } catch (e: Exception) {
            Timber.e(e, "Error adding template")
            Result.error(e, "Failed to add template")
        }
    }

    override suspend fun updateTemplate(template: SenderTemplate): Result<Unit> {
        return try {
            senderTemplateDao.update(template.toEntity())
            Result.success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Error updating template")
            Result.error(e, "Failed to update template")
        }
    }

    override suspend fun removeTemplate(templateId: Long): Result<Unit> {
        return try {
            senderTemplateDao.deleteById(templateId)
            Result.success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Error removing template")
            Result.error(e, "Failed to remove template")
        }
    }

    // --- Mappers ---

    private fun Sender.toEntity() = SenderEntity(
        senderId = senderId,
        displayName = displayName,
        isEnabled = isEnabled,
        addedAt = addedAt,
        lastMessageAt = lastMessageAt,
        messageCount = messageCount
    )

    private fun SenderEntity.toDomain(templates: List<SenderTemplate> = emptyList()) = Sender(
        senderId = senderId,
        displayName = displayName,
        isEnabled = isEnabled,
        addedAt = addedAt,
        lastMessageAt = lastMessageAt,
        messageCount = messageCount,
        templates = templates
    )

    private fun SenderWithTemplates.toDomain() = sender.toDomain(
        templates = templates.map { it.toDomain() }
    )

    private fun SenderTemplate.toEntity() = SenderTemplateEntity(
        id = id,
        senderId = senderId,
        label = label,
        pattern = pattern,
        isEnabled = isEnabled,
        createdAt = createdAt
    )

    private fun SenderTemplateEntity.toDomain() = SenderTemplate(
        id = id,
        senderId = senderId,
        label = label,
        pattern = pattern,
        isEnabled = isEnabled,
        createdAt = createdAt
    )
}
