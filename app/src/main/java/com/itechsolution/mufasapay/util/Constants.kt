package com.itechsolution.mufasapay.util

object Constants {
    // Database
    const val DATABASE_NAME = "mufasapay_database"
    const val DATABASE_VERSION = 1

    // Preferences
    const val PREFS_NAME = "mufasapay_prefs"

    // WorkManager
    const val SMS_FORWARD_WORKER_TAG = "sms_forward_worker"
    const val SMS_CLEANUP_WORKER_TAG = "sms_cleanup_worker"
    const val CLEANUP_WORK_NAME = "sms_cleanup_work"

    // SMS retention (30 days in milliseconds)
    const val SMS_RETENTION_DAYS = 30
    const val SMS_RETENTION_MILLIS = SMS_RETENTION_DAYS * 24 * 60 * 60 * 1000L

    // Webhook
    const val DEFAULT_WEBHOOK_TIMEOUT = 30000 // 30 seconds
    const val DEFAULT_MAX_RETRIES = 3
    const val DEFAULT_RETRY_DELAY_MS = 5000L // 5 seconds

    // Notification
    const val NOTIFICATION_CHANNEL_ID = "sms_gateway_channel"
    const val NOTIFICATION_CHANNEL_NAME = "SMS Gateway Service"
    const val FOREGROUND_SERVICE_NOTIFICATION_ID = 1001

    // Intent extras
    const val EXTRA_SMS_SENDER = "extra_sms_sender"
    const val EXTRA_SMS_MESSAGE = "extra_sms_message"
    const val EXTRA_SMS_TIMESTAMP = "extra_sms_timestamp"
    const val EXTRA_DELIVERY_LOG_ID = "extra_delivery_log_id"

    // Webhook event types
    const val WEBHOOK_EVENT_SMS_RECEIVED = "sms.received"

    // Auth types
    const val AUTH_TYPE_NONE = "NONE"
    const val AUTH_TYPE_BEARER = "BEARER"
    const val AUTH_TYPE_BASIC = "BASIC"
    const val AUTH_TYPE_API_KEY = "API_KEY"

}
