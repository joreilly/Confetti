package dev.johnoreilly.confetti.work

import com.apollographql.apollo3.cache.normalized.FetchPolicy
import com.apollographql.apollo3.cache.normalized.fetchPolicy
import com.apollographql.apollo3.cache.normalized.writeToCacheAsynchronously
import dev.johnoreilly.confetti.ApolloClientCache
import dev.johnoreilly.confetti.GetConferenceDataQuery
import dev.johnoreilly.confetti.GetConferencesQuery
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope

suspend fun updateCache(
    fetchConferences: Boolean,
    fetchImages: Boolean,
    conference: String,
    apolloClientCache: ApolloClientCache,
    cacheImages: (suspend (Set<String>) -> Unit)?,
) {
    supervisorScope {
        if (fetchConferences) {
            launch {
                fetchConferencesList(apolloClientCache)
            }
        }

        if (conference.isNotEmpty()) {
            launch {
                fetchConference(conference, fetchImages, apolloClientCache, cacheImages)
            }
        }
    }
}

private suspend fun fetchConference(
    conference: String,
    fetchImages: Boolean,
    apolloClientCache: ApolloClientCache,
    cacheImages: (suspend (Set<String>) -> Unit)?
) {
    val client = apolloClientCache.getClient(conference)

    val result = client.query(GetConferenceDataQuery())
        .fetchPolicy(FetchPolicy.NetworkOnly)
        .writeToCacheAsynchronously(false)
        .execute()

    if (fetchImages && result.data != null) {
        val images = extractImages(result.data!!)

        cacheImages?.invoke(images)
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

private suspend fun fetchConferencesList(apolloClientCache: ApolloClientCache) {
    apolloClientCache.getClient("all")
        .query(GetConferencesQuery())
        .fetchPolicy(FetchPolicy.NetworkOnly)
        .writeToCacheAsynchronously(false)
        .execute()
}