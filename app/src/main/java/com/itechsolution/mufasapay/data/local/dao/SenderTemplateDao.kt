package com.itechsolution.mufasapay.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.itechsolution.mufasapay.data.local.entity.SenderTemplateEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SenderTemplateDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(template: SenderTemplateEntity): Long

    @Update
    suspend fun update(template: SenderTemplateEntity)

    @Delete
    suspend fun delete(template: SenderTemplateEntity)

    @Query("SELECT * FROM sender_templates WHERE id = :id")
    suspend fun getById(id: Long): SenderTemplateEntity?

    @Query("SELECT * FROM sender_templates WHERE senderId = :senderId ORDER BY createdAt ASC")
    fun getTemplatesForSenderFlow(senderId: String): Flow<List<SenderTemplateEntity>>

    @Query("SELECT * FROM sender_templates WHERE senderId = :senderId ORDER BY createdAt ASC")
    suspend fun getTemplatesForSender(senderId: String): List<SenderTemplateEntity>

    @Query("SELECT * FROM sender_templates WHERE senderId = :senderId AND isEnabled = 1 ORDER BY createdAt ASC")
    suspend fun getEnabledTemplatesForSender(senderId: String): List<SenderTemplateEntity>

    @Query("DELETE FROM sender_templates WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM sender_templates WHERE senderId = :senderId")
    suspend fun deleteAllForSender(senderId: String)

    @Query("SELECT COUNT(*) FROM sender_templates WHERE senderId = :senderId")
    suspend fun countForSender(senderId: String): Int

    @Query("SELECT COUNT(*) FROM sender_templates WHERE senderId = :senderId")
    fun countForSenderFlow(senderId: String): Flow<Int>
}
