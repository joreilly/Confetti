package dev.johnoreilly.confetti.work

import android.content.Context
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
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
        notifier.sendNotification()
        return Result.success()
    }

    companion object {

        private val TAG = SessionNotificationWorker::class.java.simpleName

        fun startWorkRequest(workManager: WorkManager) {
            workManager.enqueueUniquePeriodicWork(
                TAG,
                ExistingPeriodicWorkPolicy.UPDATE,
                createPeriodicWorkRequest(),
            )
        }

        private fun createPeriodicWorkRequest(): PeriodicWorkRequest =
            PeriodicWorkRequestBuilder<SessionNotificationWorker>(INTERVAL.toJavaDuration())
                .setConstraints(Constraints.Builder().setRequiresBatteryNotLow(true).build())
                .build()
    }
}
