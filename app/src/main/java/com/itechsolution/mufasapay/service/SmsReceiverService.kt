package com.itechsolution.mufasapay.service

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.itechsolution.mufasapay.MainActivity
import com.itechsolution.mufasapay.R
import com.itechsolution.mufasapay.domain.usecase.ProcessIncomingSmsUseCase
import com.itechsolution.mufasapay.util.Constants
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import timber.log.Timber

/**
 * Foreground service for processing SMS messages
 * Runs in foreground to ensure reliable processing despite background restrictions
 * Shows notification while processing
 */
class SmsReceiverService : Service() {

    private val processIncomingSmsUseCase: ProcessIncomingSmsUseCase by inject()
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        Timber.d("SmsReceiverService created")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Timber.d("SmsReceiverService started")

        // Start foreground with notification
        startForeground(Constants.FOREGROUND_SERVICE_NOTIFICATION_ID, createNotification())

        // Extract SMS data from intent
        val sender = intent?.getStringExtra(Constants.EXTRA_SMS_SENDER)
        val message = intent?.getStringExtra(Constants.EXTRA_SMS_MESSAGE)
        val timestamp = intent?.getLongExtra(Constants.EXTRA_SMS_TIMESTAMP, System.currentTimeMillis())
            ?: System.currentTimeMillis()

        if (sender.isNullOrBlank() || message.isNullOrBlank()) {
            Timber.w("Invalid SMS data received in service")
            stopSelfAndService()
            return START_NOT_STICKY
        }

        // Process SMS in coroutine
        serviceScope.launch {
            try {
                Timber.d("Processing SMS in foreground service: $sender")
                val result = processIncomingSmsUseCase(sender, message, timestamp)

                if (result.isSuccess) {
                    Timber.i("SMS processed successfully in service")
                    showNotification("SMS Forwarded", "Message from $sender forwarded successfully")
                } else {
                    Timber.w("SMS processing failed: ${result.exceptionOrNull()?.message}")
                    showNotification("SMS Processing", "Failed to forward message from $sender")
                }
            } catch (e: Exception) {
                Timber.e(e, "Exception in SMS processing service")
                showNotification("Error", "Error processing SMS")
            } finally {
                // Stop service after processing
                stopSelfAndService()
            }
        }

        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
        Timber.d("SmsReceiverService destroyed")
    }

    /**
     * Creates notification for foreground service
     */
    private fun createNotification(): Notification {
        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            notificationIntent,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            } else {
                PendingIntent.FLAG_UPDATE_CURRENT
            }
        )

        return NotificationCompat.Builder(this, Constants.NOTIFICATION_CHANNEL_ID)
            .setContentTitle("Processing SMS")
            .setContentText("MufasaPay is processing an SMS message...")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .build()
    }

    /**
     * Shows a completion notification
     */
    private fun showNotification(title: String, message: String) {
        val notificationManager = getSystemService(NotificationManager::class.java)

        val notification = NotificationCompat.Builder(this, Constants.NOTIFICATION_CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(message)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(
            System.currentTimeMillis().toInt(),
            notification
        )
    }

    /**
     * Stops the foreground service
     */
    private fun stopSelfAndService() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            stopForeground(STOP_FOREGROUND_REMOVE)
        } else {
            @Suppress("DEPRECATION")
            stopForeground(true)
        }
        stopSelf()
    }
}
