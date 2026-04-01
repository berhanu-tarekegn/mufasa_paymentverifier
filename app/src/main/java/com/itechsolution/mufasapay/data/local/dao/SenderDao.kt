package com.itechsolution.mufasapay.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.itechsolution.mufasapay.data.local.entity.SenderEntity
import com.itechsolution.mufasapay.data.local.entity.SenderWithTemplates
import kotlinx.coroutines.flow.Flow

@Dao
interface SenderDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(sender: SenderEntity)

    @Update
    suspend fun update(sender: SenderEntity)

    @Delete
    suspend fun delete(sender: SenderEntity)

    @Query("SELECT * FROM senders WHERE senderId = :senderId")
    suspend fun getById(senderId: String): SenderEntity?

    @Transaction
    @Query("SELECT * FROM senders WHERE senderId = :senderId")
    suspend fun getByIdWithTemplates(senderId: String): SenderWithTemplates?

    @Query("SELECT * FROM senders ORDER BY displayName ASC")
    fun getAllFlow(): Flow<List<SenderEntity>>

    @Query("SELECT * FROM senders ORDER BY displayName ASC")
    suspend fun getAll(): List<SenderEntity>

    @Transaction
    @Query("SELECT * FROM senders ORDER BY displayName ASC")
    fun getAllWithTemplatesFlow(): Flow<List<SenderWithTemplates>>

    @Transaction
    @Query("SELECT * FROM senders ORDER BY displayName ASC")
    suspend fun getAllWithTemplates(): List<SenderWithTemplates>

    @Query("SELECT * FROM senders WHERE isEnabled = 1")
    suspend fun getEnabled(): List<SenderEntity>

    @Query("SELECT * FROM senders WHERE isEnabled = 1")
    fun getEnabledFlow(): Flow<List<SenderEntity>>

    @Transaction
    @Query("SELECT * FROM senders WHERE isEnabled = 1 ORDER BY displayName ASC")
    suspend fun getEnabledWithTemplates(): List<SenderWithTemplates>

    @Transaction
    @Query("SELECT * FROM senders WHERE isEnabled = 1 ORDER BY displayName ASC")
    fun getEnabledWithTemplatesFlow(): Flow<List<SenderWithTemplates>>

    @Query("SELECT EXISTS(SELECT 1 FROM senders WHERE senderId = :senderId AND isEnabled = 1)")
    suspend fun isWhitelisted(senderId: String): Boolean

    @Query("UPDATE senders SET isEnabled = :isEnabled WHERE senderId = :senderId")
    suspend fun updateEnabled(senderId: String, isEnabled: Boolean)

    @Query("UPDATE senders SET lastMessageAt = :timestamp, messageCount = messageCount + 1 WHERE senderId = :senderId")
    suspend fun updateStatistics(senderId: String, timestamp: Long)

    @Query("SELECT COUNT(*) FROM senders")
    suspend fun count(): Int

    @Query("SELECT COUNT(*) FROM senders")
    fun countFlow(): Flow<Int>

    @Query("SELECT COUNT(*) FROM senders WHERE isEnabled = 1")
    suspend fun countEnabled(): Int

    @Query("SELECT COUNT(*) FROM senders WHERE isEnabled = 1")
    fun countEnabledFlow(): Flow<Int>
}
