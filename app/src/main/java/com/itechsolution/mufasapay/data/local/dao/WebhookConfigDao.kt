package com.itechsolution.mufasapay.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.itechsolution.mufasapay.data.local.entity.WebhookConfigEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WebhookConfigDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(config: WebhookConfigEntity)

    @Update
    suspend fun update(config: WebhookConfigEntity)

    @Query("SELECT * FROM webhook_config WHERE id = 1")
    suspend fun get(): WebhookConfigEntity?

    @Query("SELECT * FROM webhook_config WHERE id = 1")
    fun getFlow(): Flow<WebhookConfigEntity?>

    @Query("SELECT EXISTS(SELECT 1 FROM webhook_config WHERE id = 1 AND isEnabled = 1)")
    suspend fun isEnabled(): Boolean

    @Query("UPDATE webhook_config SET isEnabled = :isEnabled WHERE id = 1")
    suspend fun updateEnabled(isEnabled: Boolean)

    @Query("DELETE FROM webhook_config WHERE id = 1")
    suspend fun delete()
}
