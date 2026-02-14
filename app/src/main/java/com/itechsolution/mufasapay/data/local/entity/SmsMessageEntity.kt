package com.itechsolution.mufasapay.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "sms_messages",
    indices = [
        Index(value = ["timestamp"], name = "idx_sms_timestamp"),
        Index(value = ["sender"], name = "idx_sms_sender"),
        Index(value = ["isForwarded"], name = "idx_sms_forwarded")
    ]
)
data class SmsMessageEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val sender: String,

    val message: String,

    val timestamp: Long,

    val rawJson: String? = null,

    val isForwarded: Boolean = false,

    val forwardedAt: Long? = null
)
