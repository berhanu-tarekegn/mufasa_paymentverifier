package com.itechsolution.mufasapay.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "sender_templates",
    foreignKeys = [
        ForeignKey(
            entity = SenderEntity::class,
            parentColumns = ["senderId"],
            childColumns = ["senderId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["senderId"], name = "idx_sender_templates_sender_id")
    ]
)
data class SenderTemplateEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val senderId: String,

    val label: String,

    val pattern: String,

    val isEnabled: Boolean = true,

    val createdAt: Long
)
