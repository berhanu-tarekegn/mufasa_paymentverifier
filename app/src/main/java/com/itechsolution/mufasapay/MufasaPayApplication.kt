package com.itechsolution.mufasapay

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.itechsolution.mufasapay.di.appModule
import com.itechsolution.mufasapay.di.databaseModule
import com.itechsolution.mufasapay.di.networkModule
import com.itechsolution.mufasapay.di.repositoryModule
import com.itechsolution.mufasapay.di.viewModelModule
import com.itechsolution.mufasapay.util.Constants
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.androidx.workmanager.koin.workManagerFactory
import org.koin.core.context.startKoin
import org.koin.core.logger.Level
import timber.log.Timber
import java.util.concurrent.TimeUnit

class MufasaPayApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        // Initialize Timber logging
        initializeTimber()

        // Initialize Koin dependency injection
        initializeKoin()

        // Create notification channels
        createNotificationChannels()

        // Schedule periodic SMS cleanup
        schedulePeriodicCleanup()

        Timber.d("MufasaPay Application initialized")
    }

    private fun initializeTimber() {
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        } else {
            // TODO: Add production logging (e.g., Crashlytics)
            Timber.plant(object : Timber.Tree() {
                override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
                    // Log to production logging service
                }
            })
        }
    }

    private fun initializeKoin() {
        startKoin {
            androidLogger(Level.ERROR)
            androidContext(this@MufasaPayApplication)
            workManagerFactory()
            modules(
                appModule,
                databaseModule,
                networkModule,
                repositoryModule,
                viewModelModule
            )
        }
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                Constants.NOTIFICATION_CHANNEL_ID,
                Constants.NOTIFICATION_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Notification for SMS Gateway foreground service"
                setShowBadge(false)
            }

            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun schedulePeriodicCleanup() {
        val constraints = Constraints.Builder()
            .setRequiresBatteryNotLow(true)
            .setRequiresDeviceIdle(false)
            .build()

        val cleanupWorkRequest = PeriodicWorkRequestBuilder<com.itechsolution.mufasapay.data.worker.SmsCleanupWorker>(
            1, TimeUnit.DAYS
        )
            .setConstraints(constraints)
            .addTag(Constants.SMS_CLEANUP_WORKER_TAG)
            .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            Constants.CLEANUP_WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            cleanupWorkRequest
        )

        Timber.d("Scheduled periodic SMS cleanup job")
    }
}
