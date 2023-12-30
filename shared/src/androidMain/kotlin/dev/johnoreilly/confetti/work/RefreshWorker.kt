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
import dev.johnoreilly.confetti.ApolloClientCache
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.supervisorScope
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.time.Duration

@OptIn(ExperimentalCoroutinesApi::class)
class RefreshWorker(
    private val appContext: Context,
    private val workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams), KoinComponent {
    private val apolloClientCache: ApolloClientCache by inject()
    private val imageLoader: ImageLoader by inject()
    private val conferenceSetting: ConferenceSetting by inject()
    private val avatarType: AvatarType = getKoin().getOrNull<AvatarType>() ?: { photoUrl }

    override suspend fun doWork(): Result = try {
        val conference = workerParams.inputData.getString(ConferenceKey) ?:
            conferenceSetting.selectedConference().first()

        if (conference.isBlank()) {
            Result.failure()
        } else {
            val fetchConferences = workerParams.inputData.getBoolean(FetchConferencesKey, false)
            val fetchImages = workerParams.inputData.getBoolean(FetchImagesKey, false)

            updateCache(
                fetchConferences = fetchConferences,
                fetchImages = fetchImages,
                conference = conference,
                apolloClientCache = apolloClientCache,
                avatarType = avatarType,
                cacheImages = ::cacheImages,
            )
            Result.success()
        }
    } catch (e: Exception) {
        Result.failure()
    }

    override suspend fun getForegroundInfo(): ForegroundInfo = ForegroundInfo(
        WorkNotificationId,
        createNotification(appContext, id)
    )

    private suspend fun cacheImages(images: Set<String>) {
        val dispatcher = Dispatchers.IO.limitedParallelism(3)
        val cache = imageLoader.diskCache!!

        supervisorScope {
            images.forEach { url ->
                val openSnapshot = cache.openSnapshot(url).also {
                    it?.close()
                }
                if (openSnapshot == null) {
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
    companion object {
        fun WorkRefresh(conference: String): String = "refresh-$conference"
        val WorkDaily: String = "daily"
        val WorkNotificationId = 101
        val WorkChannelId = "refresh"
        val ConferenceKey = "conference"
        val FetchConferencesKey = "fetchConferences"
        val FetchImagesKey = "fetchImages"

        fun oneOff(
            conference: String,
            fetchConferences: Boolean = false,
            fetchImages: Boolean = false
        ): OneTimeWorkRequest =
            OneTimeWorkRequestBuilder<RefreshWorker>()
                .setInputData(
                    workDataOf(
                        ConferenceKey to conference,
                        FetchConferencesKey to fetchConferences,
                        FetchImagesKey to fetchImages
                    )
                )
                .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
                .build()

        fun dailyRefresh(): PeriodicWorkRequest =
            PeriodicWorkRequestBuilder<RefreshWorker>(
                Duration.ofDays(1)
            )
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

