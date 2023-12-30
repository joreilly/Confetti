package dev.johnoreilly.confetti.work

import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.WorkManager
import dev.johnoreilly.confetti.decompose.ConferenceRefresh

class WorkManagerConferenceRefresh(
    private val workManager: WorkManager
) : ConferenceRefresh {
    override fun refresh(conference: String, fetchImages: Boolean) {
        if (conference.isNotEmpty()) {
            workManager.enqueueUniqueWork(
                RefreshWorker.WorkRefresh(conference),
                ExistingWorkPolicy.KEEP,
                RefreshWorker.oneOff(
                    conference = conference,
                    fetchConferences = true,
                    fetchImages = fetchImages,
                )
            )
        }
    }
}

fun setupDailyRefresh(workManager: WorkManager) {
    workManager.enqueueUniquePeriodicWork(
        RefreshWorker.WorkDaily,
        ExistingPeriodicWorkPolicy.UPDATE,
        RefreshWorker.dailyRefresh()
    )
}