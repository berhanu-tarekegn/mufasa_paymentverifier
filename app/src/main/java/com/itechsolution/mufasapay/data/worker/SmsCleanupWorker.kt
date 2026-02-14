package com.itechsolution.mufasapay.data.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.itechsolution.mufasapay.data.local.dao.SmsMessageDao
import com.itechsolution.mufasapay.util.Constants
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import timber.log.Timber

/**
 * WorkManager worker that automatically deletes SMS older than 30 days
 * Runs daily to maintain database size and user privacy
 * Cascade deletes associated delivery logs (foreign key constraint)
 */
class SmsCleanupWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params), KoinComponent {

    private val smsMessageDao: SmsMessageDao by inject()

    override suspend fun doWork(): Result {
        return try {
            Timber.d("Starting SMS cleanup job")

            val cutoffTimestamp = System.currentTimeMillis() - Constants.SMS_RETENTION_MILLIS

            // Delete SMS older than 30 days
            val deletedCount = smsMessageDao.deleteOlderThan(cutoffTimestamp)

            Timber.i("SMS cleanup completed. Deleted $deletedCount messages older than ${Constants.SMS_RETENTION_DAYS} days")

            Result.success()
        } catch (e: Exception) {
            Timber.e(e, "Error during SMS cleanup")
            Result.failure()
        }
    }
}
