package com.itechsolution.mufasapay.data.local.entity

import androidx.room.Embedded
import androidx.room.Relation

data class SenderWithTemplates(
    @Embedded
    val sender: SenderEntity,
    @Relation(
        parentColumn = "senderId",
        entityColumn = "senderId"
    )
    val templates: List<SenderTemplateEntity>
)
