package dev.johnoreilly.confetti.work

import android.content.Context
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequest
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequest
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import dev.johnoreilly.confetti.work.SessionNotificationSender.Companion.INTERVAL
import kotlin.time.toJavaDuration

class SessionNotificationWorker(
    context: Context,
    workerParams: WorkerParameters,
    private val notifier: SessionNotificationSender,
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        if (runAttemptCount > 3) {
            return Result.failure()
        }

        return try {
            notifier.sendNotification()
            Result.success()
        } catch (e: Throwable) {
            Result.retry()
        }
    }

    companion object {

        private val TAG = SessionNotificationWorker::class.java.simpleName

        fun startPeriodicWorkRequest(workManager: WorkManager) {
            workManager.enqueueUniquePeriodicWork(
                TAG,
                ExistingPeriodicWorkPolicy.UPDATE,
                createPeriodicWorkRequest(),
            )
        }

        fun cancelWorkRequest(workManager: WorkManager) {
            workManager.cancelUniqueWork(TAG)
        }

        private fun createPeriodicWorkRequest(): PeriodicWorkRequest =
            PeriodicWorkRequestBuilder<SessionNotificationWorker>(INTERVAL.toJavaDuration())
                .setConstraints(Constraints.Builder().setRequiresBatteryNotLow(true).build())
                .build()

        fun startOneTimeWorkRequest(workManager: WorkManager) {
            workManager.enqueueUniqueWork(
                TAG,
                ExistingWorkPolicy.REPLACE,
                createOneTimeWorkRequest(),
            )
        }

        private fun createOneTimeWorkRequest(): OneTimeWorkRequest =
            OneTimeWorkRequestBuilder<SessionNotificationWorker>()
                .build()
    }
}
