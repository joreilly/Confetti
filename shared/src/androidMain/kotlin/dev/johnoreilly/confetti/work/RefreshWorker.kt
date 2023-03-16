@file:OptIn(ExperimentalCoroutinesApi::class, ExperimentalCoilApi::class)

package dev.johnoreilly.confetti.work

import android.content.Context
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequest
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OutOfQuotaPolicy
import androidx.work.PeriodicWorkRequest
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import coil.ImageLoader
import coil.annotation.ExperimentalCoilApi
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.apollographql.apollo3.cache.normalized.FetchPolicy
import com.apollographql.apollo3.cache.normalized.fetchPolicy
import com.apollographql.apollo3.cache.normalized.writeToCacheAsynchronously
import dev.johnoreilly.confetti.ApolloClientCache
import dev.johnoreilly.confetti.GetConferenceDataQuery
import dev.johnoreilly.confetti.GetConferencesQuery
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import java.time.Duration

class RefreshWorker(
    private val appContext: Context,
    private val workerParams: WorkerParameters,
    private val apolloClientCache: ApolloClientCache,
    private val imageLoader: ImageLoader
) : CoroutineWorker(appContext, workerParams) {
    override suspend fun doWork(): Result = try {
        val conference = workerParams.inputData.getString(ConferenceKey)

        if (conference == null) {
            Result.failure()
        } else {
            val fetchConferences = workerParams.inputData.getBoolean(FetchConferencesKey, false)
            val fetchImages = workerParams.inputData.getBoolean(FetchImagesKey, false)

            supervisorScope {
                if (fetchConferences) {
                    launch {
                        fetchConferencesList()
                    }
                }

                if (conference.isNotEmpty()) {
                    launch {
                        fetchConference(conference, fetchImages)
                    }
                }
            }

            Result.success()
        }
    } catch (e: Exception) {
        Result.failure()
    }

    override suspend fun getForegroundInfo(): ForegroundInfo = ForegroundInfo(
        WorkNotificationId,
        createNotification(appContext, id)
    )

    private suspend fun fetchConference(conference: String, fetchImages: Boolean) {
        val client = apolloClientCache.getClient(conference)

        val result = client.query(GetConferenceDataQuery())
            .fetchPolicy(FetchPolicy.NetworkOnly)
            .writeToCacheAsynchronously(false)
            .execute()

        if (fetchImages && result.data != null) {
            val images = extractImages(result.data!!)

            fetchImages(images)
        }
    }

    private suspend fun fetchImages(images: Set<String>) {
        val dispatcher = Dispatchers.IO.limitedParallelism(3)
        val cache = imageLoader.diskCache!!

        supervisorScope {
            images.forEach { url ->
                if (cache[url] == null) {
                    val request = ImageRequest.Builder(appContext)
                        .data(url)
                        .memoryCachePolicy(CachePolicy.DISABLED)
                        .dispatcher(dispatcher)
                        .build()
                    imageLoader.execute(request)
                }
            }
        }
    }

    private fun extractImages(data: GetConferenceDataQuery.Data): Set<String> {
        return data.speakers.flatMap {
            listOfNotNull(
                it.speakerDetails.photoUrl,
                it.speakerDetails.companyLogoUrl
            )
        }.toSet()
    }

    private suspend fun fetchConferencesList() {
        apolloClientCache.getClient("all")
            .query(GetConferencesQuery())
            .fetchPolicy(FetchPolicy.NetworkOnly)
            .writeToCacheAsynchronously(false)
            .execute()
    }

    companion object {
        fun WorkRefresh(conference: String): String = "refresh-$conference"
        val WorkDaily: String = "daily"
        val WorkNotificationId = 101
        val WorkChannelId = "refresh"
        val ConferenceKey = "conference"
        val FetchConferencesKey = "fetchConferences"
        val FetchImagesKey = "fetchImages"

        fun oneOff(conference: String): OneTimeWorkRequest =
            OneTimeWorkRequestBuilder<RefreshWorker>()
                .setInputData(
                    workDataOf(
                        ConferenceKey to conference
                    )
                )
                .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
                .build()

        fun dailyRefresh(): PeriodicWorkRequest =
            PeriodicWorkRequestBuilder<RefreshWorker>(Duration.ofDays(1))
                .setInputData(
                    workDataOf(
                        FetchConferencesKey to true,
                        FetchImagesKey to true
                    )
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