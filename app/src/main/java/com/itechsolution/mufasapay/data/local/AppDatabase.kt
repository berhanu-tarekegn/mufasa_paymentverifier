package com.itechsolution.mufasapay.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.itechsolution.mufasapay.data.local.dao.DeliveryLogDao
import com.itechsolution.mufasapay.data.local.dao.SenderDao
import com.itechsolution.mufasapay.data.local.dao.SmsMessageDao
import com.itechsolution.mufasapay.data.local.dao.WebhookConfigDao
import com.itechsolution.mufasapay.data.local.entity.DeliveryLogEntity
import com.itechsolution.mufasapay.data.local.entity.SenderEntity
import com.itechsolution.mufasapay.data.local.entity.SmsMessageEntity
import com.itechsolution.mufasapay.data.local.entity.WebhookConfigEntity

@Database(
    entities = [
        SmsMessageEntity::class,
        SenderEntity::class,
        WebhookConfigEntity::class,
        DeliveryLogEntity::class
    ],
    version = 1,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun smsMessageDao(): SmsMessageDao
    abstract fun senderDao(): SenderDao
    abstract fun webhookConfigDao(): WebhookConfigDao
    abstract fun deliveryLogDao(): DeliveryLogDao
}
