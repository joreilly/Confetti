package dev.johnoreilly.confetti.appsearch

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequest
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkerParameters
import com.apollographql.cache.normalized.FetchPolicy
import dev.johnoreilly.confetti.ApolloClientCache
import dev.johnoreilly.confetti.ConfettiRepository
import dev.johnoreilly.confetti.GetConferenceDataQuery
import dev.johnoreilly.confetti.fetchPolicy
import dev.johnoreilly.confetti.work.ConferenceSetting
import dev.johnoreilly.confetti.work.RefreshWorker
import dev.johnoreilly.confetti.work.RefreshWorker.Companion.ConferenceKey
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.time.Duration

@RequiresApi(Build.VERSION_CODES.S)
class AppSearchWorker(
    private val appContext: Context,
    private val workerParams: WorkerParameters
): CoroutineWorker(appContext, workerParams), KoinComponent {
    private val apolloClientCache: ApolloClientCache by inject()
    private val confettiRepository: ConfettiRepository by inject()
    private val appSearchManager: AppSearchManager by inject()

    override suspend fun doWork(): Result {
        val conference = confettiRepository.getConference()

        return if (conference.isBlank()) {
            Result.success()
        } else {
            updateAppSearch(appSearchManager, conference, apolloClientCache)
            Result.success()
        }
    }

    companion object {
        fun dailyRefresh(): PeriodicWorkRequest =
            PeriodicWorkRequestBuilder<RefreshWorker>(
                Duration.ofDays(1)
            )
                .setConstraints(
                    Constraints.Builder()
                        .setRequiresCharging(true)
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .setRequiresDeviceIdle(true)
                        .build()
                )
                .build()
    }
}


@RequiresApi(Build.VERSION_CODES.S)
suspend fun updateAppSearch(
    appSearchManager: AppSearchManager,
    conference: String,
    apolloClientCache: ApolloClientCache,
) {
    println("updateAppSearch")
    val client = apolloClientCache.getClient(conference)

    val result = client.query(GetConferenceDataQuery())
        .fetchPolicy(FetchPolicy.CacheOnly)
        .execute()

    println("init")
    appSearchManager.init()
    println("defineSchema")
    appSearchManager.defineSchema()

    val nodes = result.data?.sessions?.nodes?.map { it.sessionDetails } ?: return

    supervisorScope {
        println("writing Session")
        appSearchManager.writeSessions(conference, nodes)
    }

    println("flushAndClose")
    appSearchManager.flushAndClose()
}