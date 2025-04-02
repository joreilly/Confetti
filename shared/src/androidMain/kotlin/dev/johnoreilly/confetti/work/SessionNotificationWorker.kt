package dev.johnoreilly.confetti.work

import android.content.Context
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import kotlin.time.Duration.Companion.minutes
import kotlin.time.toJavaDuration

// Using a custom constructor has caused a crash inside Koin.
// For now, as a workaround, we use KoinComponent with an inject for getting the notifier instance.
class SessionNotificationWorker(
    context: Context,
    workerParams: WorkerParameters,
) : CoroutineWorker(context, workerParams), KoinComponent {

    private val notifier: SessionNotificationSender by inject()

    override suspend fun doWork(): Result {
        if (runAttemptCount > 3) {
            return Result.failure()
        }

        return try {
            notifier.sendNotification(Selector)
            Result.success()
        } catch (e: Throwable) {
            Result.retry()
        }
    }

    companion object {

        private val TAG = SessionNotificationWorker::class.java.simpleName

        fun startPeriodicWorkRequest(workManager: WorkManager) {
            val workRequest =
                PeriodicWorkRequestBuilder<SessionNotificationWorker>(Selector.within.toJavaDuration())
                    .setConstraints(Constraints.Builder().setRequiresBatteryNotLow(true).build())
                    .build()

            workManager.enqueueUniquePeriodicWork(
                TAG,
                ExistingPeriodicWorkPolicy.UPDATE,
                workRequest,
            )
        }

        fun cancelWorkRequest(workManager: WorkManager) {
            workManager.cancelUniqueWork(TAG)
        }

        // Minimum interval for work manager: MIN_PERIODIC_INTERVAL_MILLIS
        val Selector = NotificationSender.Today(15.minutes)
    }
}
