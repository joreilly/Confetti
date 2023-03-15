@file:OptIn(ExperimentalCoroutinesApi::class)

package dev.johnoreilly.confetti

import com.apollographql.apollo3.ApolloCall
import com.apollographql.apollo3.api.ApolloResponse
import com.apollographql.apollo3.api.Mutation
import com.apollographql.apollo3.cache.normalized.FetchPolicy
import com.apollographql.apollo3.cache.normalized.apolloStore
import com.apollographql.apollo3.cache.normalized.fetchPolicy
import com.apollographql.apollo3.cache.normalized.optimisticUpdates
import com.apollographql.apollo3.cache.normalized.refetchPolicy
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
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class ConfettiRepository(
    private val defaultFetchPolicy: FetchPolicy
) : KoinComponent {
    // TODO move coroutines out of Repository to ViewModel
    val coroutineScope: CoroutineScope = MainScope()

    private val appSettings: AppSettings by inject()

    private val apolloClientCache: ApolloClientCache by inject()

    val bookmarks: Flow<List<String>> = getConferenceFlow().flatMapLatest {
        apolloClientCache.getClient(it)
            .query(GetBookmarksQuery())
            .fetchPolicy(FetchPolicy.NetworkFirst)
            .refetchPolicy(FetchPolicy.CacheOnly)
            .watch()
            .onEach { println("got bookmarks ${it.data}") }
            .map { it.data?.bookmarks?.sessionIds.orEmpty() }
    }

    private suspend fun <D: Mutation.Data> modifyBookmarks(mutation: Mutation<D>, data: (sessionIds: List<String>, id: String) -> D ) {
            try {
                val client = apolloClientCache.getClient(getConference())
                val optimisticData  = try {
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

    suspend fun addBookmark(sessionId: String) {
        modifyBookmarks(AddBookmarkMutation(sessionId)) { sessionIds, id ->
            AddBookmarkMutation.Data {
                addBookmark = buildBookmarks {
                    this.id = id
                    this.sessionIds = sessionIds + sessionId
                }
            }
        }
    }

    suspend fun removeBookmark(sessionId: String) {
        modifyBookmarks(RemoveBookmarkMutation(sessionId)) {sessionIds, id ->
            RemoveBookmarkMutation.Data {
                removeBookmark = buildBookmarks {
                    this.id = id
                    this.sessionIds = sessionIds - sessionId
                }
            }
        }
    }

    val conferenceList: Flow<List<GetConferencesQuery.Conference>> = flow {
        val client = apolloClientCache.getClient("all")

        emitAll(client.query(GetConferencesQuery())
            .fetchPolicy(defaultFetchPolicy)
            .toFlow()
            .catch {
                // handle network failures by swallowing the error
            }
            .mapNotNull {
                it.data?.conferences
            })
    }

    private val conferenceData: StateFlow<GetConferenceDataQuery.Data?> =
        getConferenceFlow().flatMapLatest {
            if (it.isEmpty()) {
                flowOf(null)
            } else {
                apolloClientCache.getClient(it).query(GetConferenceDataQuery()).toFlow().map {
                    it.data
                }.catch {
                    // TODO log errors
                    emit(null)
                }
            }
        }.stateIn(coroutineScope, SharingStarted.Eagerly, null)

    val timeZone = conferenceData.value?.config?.timezone?.let {
        TimeZone.of(it)
    } ?: TimeZone.currentSystemDefault()

    val conferenceName = conferenceData.filterNotNull().map { it.config.name }

    val sessions = conferenceData.filterNotNull().map {
        it.sessions.nodes.map { it.sessionDetails }.sortedBy { it.startsAt }
    }

    val sessionsMap: Flow<Map<LocalDate, List<SessionDetails>>> = sessions.map {
        it.groupBy {
            it.startsAt.date
        }
    }

    val speakers = conferenceData.filterNotNull().map {
        it.speakers.map { it.speakerDetails }
    }

    val rooms = conferenceData.filterNotNull().map {
        it.rooms.map { it.roomDetails }
    }

    suspend fun getConference(): String {
        return appSettings.getConference()
    }

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
    ): Flow<ApolloResponse<GetSessionQuery.Data>> =
        apolloClientCache.getClient(conference).query(GetSessionQuery(sessionId)).toFlow()

    suspend fun conferenceData(conference: String): Flow<ApolloResponse<GetConferenceDataQuery.Data>> =
        apolloClientCache.getClient(conference).query(GetConferenceDataQuery()).toFlow()

    suspend fun sessions(conference: String): Flow<ApolloResponse<GetSessionsQuery.Data>> =
        apolloClientCache.getClient(conference).query(GetSessionsQuery()).toFlow()

    suspend fun bookmarks(conference: String): Flow<List<String>> =
        apolloClientCache.getClient(conference).query(GetBookmarksQuery())
            .fetchPolicy(FetchPolicy.NetworkFirst)
            .refetchPolicy(FetchPolicy.CacheOnly)
            .toFlow()
            .map {
                it.data?.bookmarks?.sessionIds.orEmpty()
            }

    suspend fun conferenceHomeData(conference: String): ApolloCall<GetConferenceDataQuery.Data> {
        return apolloClientCache.getClient(conference).query(GetConferenceDataQuery())
    }
}
