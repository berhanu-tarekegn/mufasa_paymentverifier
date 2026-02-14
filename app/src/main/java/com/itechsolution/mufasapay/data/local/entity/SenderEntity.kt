package com.itechsolution.mufasapay.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "senders",
    indices = [
        Index(value = ["isEnabled"], name = "idx_senders_enabled")
    ]
)
data class SenderEntity(
    @PrimaryKey
    val senderId: String,

    val displayName: String,

    val pattern: String? = null,

    val isEnabled: Boolean = true,

    val addedAt: Long,

    val lastMessageAt: Long? = null,

    val messageCount: Int = 0
)
