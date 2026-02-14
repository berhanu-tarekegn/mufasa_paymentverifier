package com.itechsolution.mufasapay.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "delivery_logs",
    foreignKeys = [
        ForeignKey(
            entity = SmsMessageEntity::class,
            parentColumns = ["id"],
            childColumns = ["smsId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["smsId"], name = "idx_delivery_smsId"),
        Index(value = ["status"], name = "idx_delivery_status"),
        Index(value = ["timestamp"], name = "idx_delivery_timestamp")
    ]
)
data class DeliveryLogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val smsId: Long,

    val status: String, // PENDING, SUCCESS, FAILED, RETRYING

    val attemptNumber: Int = 1,

    val requestPayload: String,

    val responseCode: Int? = null,

    val responseBody: String? = null,

    val errorMessage: String? = null,

    val timestamp: Long,

    val duration: Long? = null // milliseconds
)
