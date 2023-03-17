@file:OptIn(ExperimentalCoroutinesApi::class)

package dev.johnoreilly.confetti

import com.apollographql.apollo3.ApolloCall
import com.apollographql.apollo3.api.ApolloResponse
import com.apollographql.apollo3.api.Mutation
import com.apollographql.apollo3.cache.normalized.FetchPolicy
import com.apollographql.apollo3.cache.normalized.apolloStore
import com.apollographql.apollo3.cache.normalized.fetchPolicy
import com.apollographql.apollo3.cache.normalized.optimisticUpdates
import com.apollographql.apollo3.cache.normalized.watch
import dev.johnoreilly.confetti.fragment.SessionDetails
import dev.johnoreilly.confetti.type.buildBookmarks
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class ConfettiRepository : KoinComponent {

    private val appSettings: AppSettings by inject()

    private val apolloClientCache: ApolloClientCache by inject()

    private suspend fun <D : Mutation.Data> modifyBookmarks(
        conference: String,
        mutation: Mutation<D>,
        data: (sessionIds: List<String>, id: String) -> D
    ) {
        try {
            val client = apolloClientCache.getClient(conference)
            val optimisticData = try {
                val bookmarks = client.apolloStore.readOperation(GetBookmarksQuery()).bookmarks
                data(bookmarks!!.sessionIds, bookmarks.id)
            } catch (e: Exception) {
                null
            }
            client.mutation(mutation)
                .apply {
                    if (optimisticData != null) {
                        optimisticUpdates(optimisticData)
                    }
                }
                .execute()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun addBookmark(conference: String, sessionId: String) {
        modifyBookmarks(conference, AddBookmarkMutation(sessionId)) { sessionIds, id ->
            AddBookmarkMutation.Data {
                addBookmark = buildBookmarks {
                    this.id = id
                    this.sessionIds = sessionIds + sessionId
                }
            }
        }
    }

    suspend fun removeBookmark(conference: String, sessionId: String) {
        modifyBookmarks(conference, RemoveBookmarkMutation(sessionId)) { sessionIds, id ->
            RemoveBookmarkMutation.Data {
                removeBookmark = buildBookmarks {
                    this.id = id
                    this.sessionIds = sessionIds - sessionId
                }
            }
        }
    }

    suspend fun conferences(fetchPolicy: FetchPolicy): ApolloResponse<GetConferencesQuery.Data> {
        return apolloClientCache.getClient("all")
            .query(GetConferencesQuery())
            .fetchPolicy(fetchPolicy)
            .tryExecute()
    }

    suspend fun getConference(): String {
        return appSettings.getConference()
    }

    /**
     * This is OK to use in AppViewModel and from background refresh jobs but use with caution
     * elsewhere as the conference might also come from a deep link in which case this value
     * would conflict
     */
    fun getConferenceFlow(): Flow<String> {
        return appSettings.getConferenceFlow()
    }

    suspend fun setConference(conference: String) {
        appSettings.setConference(conference)
    }

    suspend fun refresh(conference: String, networkOnly: Boolean = true) {
        val fetchPolicy = if (networkOnly) FetchPolicy.NetworkOnly else FetchPolicy.CacheAndNetwork

        try {
            // TODO: We fetch the first page only, assuming there are <100 conferences. Pagination should be implemented instead.
            apolloClientCache.getClient(conference)
                .query(GetConferenceDataQuery())
                .fetchPolicy(fetchPolicy)
                .execute()
        } catch (e: Exception) {
            // this can be valid scenario of say offline and we get data from cache initially
            // but can't connect to network.
            // TODO should we surface this somewhere?
            e.printStackTrace()
        }
    }

    suspend fun sessionDetails(
        conference: String,
        sessionId: String
    ): ApolloResponse<GetSessionQuery.Data> =
        apolloClientCache.getClient(conference).query(GetSessionQuery(sessionId)).tryExecute()

    suspend fun conferenceData(
        conference: String,
        fetchPolicy: FetchPolicy
    ): ApolloResponse<GetConferenceDataQuery.Data> =
        apolloClientCache.getClient(conference).query(GetConferenceDataQuery())
            .fetchPolicy(fetchPolicy).tryExecute()

    suspend fun bookmarks(
        conference: String,
        fetchPolicy: FetchPolicy
    ): ApolloResponse<GetBookmarksQuery.Data> =
        apolloClientCache.getClient(conference).query(GetBookmarksQuery()).fetchPolicy(fetchPolicy)
            .tryExecute()

    suspend fun sessions(conference: String): Flow<ApolloResponse<GetSessionsQuery.Data>> =
        apolloClientCache.getClient(conference).query(GetSessionsQuery()).toFlow()


    fun watchBookmarks(
        conference: String,
        initialData: GetBookmarksQuery.Data?
    ): Flow<ApolloResponse<GetBookmarksQuery.Data>> = flow {
        val values = apolloClientCache.getClient(conference).query(GetBookmarksQuery())
            .watch(initialData)

        emitAll(values)
    }

    suspend fun conferenceHomeData(conference: String): ApolloCall<GetConferenceDataQuery.Data> {
        return apolloClientCache.getClient(conference).query(GetConferenceDataQuery())
    }
}
