package dev.johnoreilly.confetti.work

import com.apollographql.apollo.cache.normalized.FetchPolicy
import com.apollographql.apollo.cache.normalized.fetchPolicy
import com.apollographql.apollo.cache.normalized.writeToCacheAsynchronously
import dev.johnoreilly.confetti.ApolloClientCache
import dev.johnoreilly.confetti.GetConferenceDataQuery
import dev.johnoreilly.confetti.GetConferencesQuery
import dev.johnoreilly.confetti.fragment.SpeakerDetails
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope

typealias AvatarType = SpeakerDetails.() -> String?

suspend fun updateCache(
    fetchConferences: Boolean,
    fetchImages: Boolean,
    conference: String,
    apolloClientCache: ApolloClientCache,
    avatarType: AvatarType = { photoUrl },
    cacheImages: (suspend (Set<String>) -> Unit)? = null,
) {
    supervisorScope {
        if (fetchConferences) {
            launch {
                fetchConferencesList(apolloClientCache)
            }
        }

        if (conference.isNotEmpty()) {
            launch {
                fetchConference(conference, fetchImages, avatarType, apolloClientCache, cacheImages)
            }
        }
    }
}

private suspend fun fetchConference(
    conference: String,
    fetchImages: Boolean,
    avatarType: AvatarType,
    apolloClientCache: ApolloClientCache,
    cacheImages: (suspend (Set<String>) -> Unit)?
) {
    val client = apolloClientCache.getClient(conference)

    val result = client.query(GetConferenceDataQuery())
        .fetchPolicy(FetchPolicy.NetworkOnly)
        .writeToCacheAsynchronously(false)
        .execute()

    if (fetchImages && result.data != null) {
        val images = extractImages(avatarType, result.data!!)

        if (fetchImages) {
            println("Fetching ${images.size} images")
        }

        cacheImages?.invoke(images)
    }
}


private fun extractImages(avatarType: AvatarType, data: GetConferenceDataQuery.Data): Set<String> {
    return data.speakers.nodes.flatMap {
        listOfNotNull(
            avatarType.invoke(it.speakerDetails),
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
