package dev.johnoreilly.confetti.appsearch

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.appsearch.app.AppSearchSession
import androidx.appsearch.app.GlobalSearchSession
import androidx.appsearch.app.PutDocumentsRequest
import androidx.appsearch.app.SetSchemaRequest
import androidx.appsearch.platformstorage.PlatformStorage
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import dev.johnoreilly.confetti.fragment.SessionDetails
import dev.johnoreilly.confetti.work.RefreshWorker
import kotlinx.coroutines.guava.await

@RequiresApi(Build.VERSION_CODES.S)
class AppSearchManager(
    private val context: Context,
    private val workManager: WorkManager
) {
    private lateinit var globalSession: GlobalSearchSession
    lateinit var session: AppSearchSession

    suspend fun init() {
        globalSession = PlatformStorage.createGlobalSearchSessionAsync(
            PlatformStorage.GlobalSearchContext.Builder(context).build()
        ).await()
        session = PlatformStorage.createSearchSessionAsync(
            PlatformStorage.SearchContext.Builder(context, "confetti")
                .build()
        ).await()
    }

    suspend fun defineSchema() {
        session.setSchemaAsync(
            SetSchemaRequest.Builder()
                .addDocumentClasses(SearchSessionModel::class.java)
                .setDocumentClassDisplayedBySystem(SearchSessionModel::class.java, true)
                .build()
        ).await()
    }

    fun scheduleDailyUpdate() {
        workManager.enqueueUniquePeriodicWork(
            RefreshWorker.WorkDaily,
            ExistingPeriodicWorkPolicy.UPDATE,
            AppSearchWorker.dailyRefresh()
        )
    }

    suspend fun flushAndClose() {
        session.requestFlushAsync().await()
        session.close()
    }

    fun scheduleImmediate() {
        workManager.enqueueUniqueWork(
            "AppSearch",
            ExistingWorkPolicy.KEEP,
            OneTimeWorkRequest.from(AppSearchWorker::class.java)
        )
    }

    suspend fun writeSessions(conference: String, nodes: List<SessionDetails>) {
        val putRequest = PutDocumentsRequest.Builder().apply {
            nodes.forEach { details ->
                val sessionDocument = SearchSessionModel(
                    conference,
                    details.id,
                    details.title,
                    details.room?.name ?: "",
                    details.speakers.map { it.speakerDetails.name })
                addDocuments(sessionDocument)
            }
        }.build()
        session.putAsync(putRequest).await()
    }
}