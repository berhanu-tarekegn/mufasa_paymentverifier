package com.itechsolution.mufasapay.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Telephony
import android.telephony.SmsMessage
import com.itechsolution.mufasapay.util.Constants
import timber.log.Timber

/**
 * BroadcastReceiver for intercepting SMS messages
 * Registered in AndroidManifest.xml with SMS_RECEIVED action
 * High priority (999) to intercept SMS early
 *
 * IMPORTANT: Does NOT call abortBroadcast() - SMS will still reach default app (non-invasive)
 */
class SmsBroadcastReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null || intent == null) {
            Timber.w("Null context or intent in SMS receiver")
            return
        }

        if (intent.action != Telephony.Sms.Intents.SMS_RECEIVED_ACTION) {
            Timber.d("Not an SMS_RECEIVED action: ${intent.action}")
            return
        }

        try {
            val messages = extractSmsMessages(intent)
            if (messages.isEmpty()) {
                Timber.w("No SMS messages extracted from intent")
                return
            }

            // Combine multipart SMS into one
            val fullMessage = messages.joinToString("") { it.messageBody }

            if (fullMessage.isNotEmpty()) {
                val firstSms = messages[0]
                val sender = firstSms.displayOriginatingAddress ?: firstSms.originatingAddress ?: "Unknown"
                val timestamp = firstSms.timestampMillis
                Timber.i("SMS received from: $sender, length: ${fullMessage.length}")
                startSmsProcessingService(context, sender, fullMessage, timestamp)
            }

            // IMPORTANT: Do NOT call abortBroadcast()
            // We want SMS to reach the default SMS app (non-invasive approach)
        } catch (e: Exception) {
            Timber.e(e, "Error processing SMS in BroadcastReceiver")
        }
    }

    /**
     * Extracts SMS messages from intent
     * Handles both modern (API 19+) and legacy approaches
     */
    private fun extractSmsMessages(intent: Intent): List<SmsMessage> {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                // Modern approach (API 19+)
                Telephony.Sms.Intents.getMessagesFromIntent(intent)?.toList() ?: emptyList()
            } else {
                // Legacy approach (pre-API 19)
                val pdus = intent.extras?.get("pdus") as? Array<*>
                pdus?.mapNotNull { pdu ->
                    try {
                        @Suppress("DEPRECATION")
                        SmsMessage.createFromPdu(pdu as ByteArray)
                    } catch (e: Exception) {
                        Timber.e(e, "Error creating SmsMessage from PDU")
                        null
                    }
                } ?: emptyList()
            }
        } catch (e: Exception) {
            Timber.e(e, "Error extracting SMS messages")
            emptyList()
        }
    }

    /**
     * Starts the foreground service to process SMS
     */
    private fun startSmsProcessingService(
        context: Context,
        sender: String,
        message: String,
        timestamp: Long
    ) {
        val serviceIntent = Intent(context, SmsReceiverService::class.java).apply {
            putExtra(Constants.EXTRA_SMS_SENDER, sender)
            putExtra(Constants.EXTRA_SMS_MESSAGE, message)
            putExtra(Constants.EXTRA_SMS_TIMESTAMP, timestamp)
        }

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(serviceIntent)
            } else {
                context.startService(serviceIntent)
            }
            Timber.d("Started SmsReceiverService")
        } catch (e: Exception) {
            Timber.e(e, "Error starting SmsReceiverService")
        }
    }
}
