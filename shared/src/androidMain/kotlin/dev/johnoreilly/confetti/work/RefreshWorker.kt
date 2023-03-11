@file:OptIn(ExperimentalCoroutinesApi::class, ExperimentalCoilApi::class)

package dev.johnoreilly.confetti.work

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import coil.ImageLoader
import coil.annotation.ExperimentalCoilApi
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.apollographql.apollo3.cache.normalized.FetchPolicy
import com.apollographql.apollo3.cache.normalized.fetchPolicy
import com.apollographql.apollo3.cache.normalized.writeToCacheAsynchronously
import dev.johnoreilly.confetti.ApolloClientCache
import dev.johnoreilly.confetti.ConfettiRepository
import dev.johnoreilly.confetti.GetConferenceDataQuery
import dev.johnoreilly.confetti.GetConferencesQuery
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

class RefreshWorker(
    private val appContext: Context,
    private val workerParams: WorkerParameters,
    private val confettiRepository: ConfettiRepository,
    private val apolloClientCache: ApolloClientCache,
    private val imageLoader: ImageLoader
) : CoroutineWorker(appContext, workerParams) {
    override suspend fun doWork(): Result {
        val conference =
            workerParams.inputData.getString(ConferenceKey) ?: confettiRepository.getConference()
        val fetchConferences = workerParams.inputData.getBoolean(FetchConferencesKey, false)
        val fetchImages = workerParams.inputData.getBoolean(FetchImagesKey, false)

        coroutineScope {
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

        return Result.success()
    }

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
        val ConferenceKey = "conference"
        val FetchConferencesKey = "fetchConferences"
        val FetchImagesKey = "fetchImages"
    }
}