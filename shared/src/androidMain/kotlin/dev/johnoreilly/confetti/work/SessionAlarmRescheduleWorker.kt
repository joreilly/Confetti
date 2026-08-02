package dev.johnoreilly.confetti.work

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * CoroutineWorker called by boot receiver.
 * Query bookmarks and reschedule alarms in background.
 */
class SessionAlarmRescheduleWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams), KoinComponent {

    private val notificationSender: SessionNotificationSender by inject()

    override suspend fun doWork(): Result = try {
        notificationSender.updateSchedule()
        Result.success()
    } catch (e: Exception) {
        Result.failure()
    }

    companion object {
        fun enqueue(workManager: WorkManager) {
            val request = OneTimeWorkRequestBuilder<SessionAlarmRescheduleWorker>().build()
            workManager.enqueue(request)
        }
    }
}
