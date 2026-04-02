package com.itechsolution.mufasapay.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "sms_messages",
    indices = [
        Index(value = ["timestamp"], name = "idx_sms_timestamp"),
        Index(value = ["sender"], name = "idx_sms_sender"),
        Index(value = ["isForwarded"], name = "idx_sms_forwarded"),
        Index(value = ["sender", "message", "timestamp"], name = "idx_sms_unique_content", unique = true)
    ]
)
data class SmsMessageEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val sender: String,

    val message: String,

    val timestamp: Long,

    val amount: Double? = null,

    val transactionId: String? = null,

    val rawJson: String? = null,

    val isForwarded: Boolean = false,

    val forwardedAt: Long? = null
)
