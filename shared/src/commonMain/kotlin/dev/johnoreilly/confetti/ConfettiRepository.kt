package dev.johnoreilly.confetti

import com.apollographql.apollo.ApolloCall
import com.apollographql.apollo.api.ApolloResponse
import com.apollographql.apollo.api.Mutation
import com.apollographql.apollo.api.Operation
import com.apollographql.apollo.cache.normalized.FetchPolicy
import com.apollographql.apollo.cache.normalized.apolloStore
import com.apollographql.apollo.cache.normalized.fetchPolicy
import com.apollographql.apollo.cache.normalized.optimisticUpdates
import com.apollographql.apollo.cache.normalized.watch
import com.apollographql.apollo.exception.DefaultApolloException
import com.benasher44.uuid.uuid4
import dev.johnoreilly.confetti.type.buildBookmarks
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

private fun <D : Operation.Data> ApolloCall<D>.tokenProvider(tokenProvider: TokenProvider?) = apply {
    if (tokenProvider != null) {
        addExecutionContext(TokenProviderContext(tokenProvider))
    }
}

class ConfettiRepository : KoinComponent {

    private val appSettings: AppSettings by inject()

    private val apolloClientCache: ApolloClientCache by inject()

    private val conferenceListeners: MutableList<suspend (String, String?) -> Unit> = mutableListOf()

    fun addConferenceListener(onConferenceSet: suspend (String, String?) -> Unit = { _, _ -> }) {
        conferenceListeners.add(onConferenceSet)
    }

    suspend fun updateConfenceListeners(conference: String, conferenceThemeColor: String?) {
        conferenceListeners.forEach {
            it(conference, conferenceThemeColor)
        }
    }

    private suspend fun <D : Mutation.Data> modifyBookmarks(
        conference: String,
        uid: String?,
        tokenProvider: TokenProvider?,
        mutation: Mutation<D>,
        data: (sessionIds: List<String>, id: String) -> D
    ): Boolean {
        val client = apolloClientCache.getClient(conference, uid)
        val optimisticData = try {
            val bookmarks = client.apolloStore.readOperation(GetBookmarksQuery()).bookmarks
            data(bookmarks!!.sessionIds, bookmarks.id)
        } catch (e: Exception) {
            null
        }
        val response = client.mutation(mutation)
            .tokenProvider(tokenProvider)
            .apply {
                if (optimisticData != null) {
                    optimisticUpdates(optimisticData)
                }
            }
            .execute()

        return response.data != null
    }
    suspend fun addBookmark(
        conference: String,
        uid: String?,
        tokenProvider: TokenProvider?,
        sessionId: String
    ): Boolean {
        return modifyBookmarks(
            conference,
            uid,
            tokenProvider,
            AddBookmarkMutation(sessionId)
        ) { sessionIds, id ->
            AddBookmarkMutation.Data {
                addBookmark = buildBookmarks {
                    this.id = id
                    this.sessionIds = sessionIds + sessionId
                }
            }
        }
    }

    suspend fun removeBookmark(
        conference: String,
        uid: String?,
        tokenProvider: TokenProvider?,
        sessionId: String
    ): Boolean {
        return modifyBookmarks(
            conference,
            uid,
            tokenProvider,
            RemoveBookmarkMutation(sessionId)
        ) { sessionIds, id ->
            RemoveBookmarkMutation.Data {
                removeBookmark = buildBookmarks {
                    this.id = id
                    this.sessionIds = sessionIds - sessionId
                }
            }
        }
    }

    /**
     * ignores all errors unless the Flow terminates without an emission in which case it will emit an error
     */
    private fun <D : Operation.Data> Flow<ApolloResponse<D>>.itemsOrError(operation: Operation<D>): Flow<ApolloResponse<D>> {
        var hasValidResponse = false
        return this.onEach {
            if (it.data != null) {
                hasValidResponse = true
            }
        }.filter {
            it.data != null
        }.onCompletion {
            if (!hasValidResponse) {
                emit(
                    ApolloResponse.Builder(
                        operation,
                        uuid4(),
                    ).exception(DefaultApolloException("The flow terminated without a valid item")).build()
                )
            }
        }
    }

    fun bookmarks(
        conference: String,
        uid: String?,
        tokenProvider: TokenProvider?,
        fetchPolicy: FetchPolicy
    ): Flow<ApolloResponse<GetBookmarksQuery.Data>> {
        return apolloClientCache.getClient(conference, uid).query(GetBookmarksQuery())
            .tokenProvider(tokenProvider)
            .fetchPolicy(fetchPolicy)
            .toFlow()
            .itemsOrError(GetBookmarksQuery())
    }

    fun bookmarkedSessionsQuery(
        conference: String,
        uid: String?,
        tokenProvider: TokenProvider?,
        fetchPolicy: FetchPolicy
    ): ApolloCall<GetBookmarkedSessionsQuery.Data> {

        println("JFOR, bookmarkedSessionsQuery, uid = $uid")

        return apolloClientCache.getClient(conference, uid).query(GetBookmarkedSessionsQuery())
            .tokenProvider(tokenProvider)
            .fetchPolicy(fetchPolicy)
    }

    fun watchBookmarks(
        conference: String,
        uid: String?,
        tokenProvider: TokenProvider?,
        initialData: GetBookmarksQuery.Data?,
    ): Flow<ApolloResponse<GetBookmarksQuery.Data>> = flow {
        val values = apolloClientCache.getClient(conference, uid).query(GetBookmarksQuery())
            .tokenProvider(tokenProvider)
            .watch(initialData)

        emitAll(values)
    }


    suspend fun conferences(fetchPolicy: FetchPolicy): ApolloResponse<GetConferencesQuery.Data> {
        return conferencesQuery(fetchPolicy)
            .execute()
    }

    suspend fun conferenceVenue(conference: String, fetchPolicy: FetchPolicy): ApolloResponse<GetVenueQuery.Data> {
        return conferenceVenueQuery(conference, fetchPolicy)
            .execute()
    }

    fun conferencesQuery(fetchPolicy: FetchPolicy): ApolloCall<GetConferencesQuery.Data> =
        apolloClientCache.getClient("all")
            .query(GetConferencesQuery())
            .fetchPolicy(fetchPolicy)

    private fun conferenceVenueQuery(conference: String, fetchPolicy: FetchPolicy): ApolloCall<GetVenueQuery.Data> =
        apolloClientCache.getClient(conference)
            .query(GetVenueQuery())
            .fetchPolicy(fetchPolicy)


    suspend fun getConference(): String {
        return appSettings.getConference()
    }

    suspend fun getConferenceThemeColor(): String {
        return appSettings.getConferenceThemeColor()
    }

    /**
     * This is OK to use in AppViewModel and from background refresh jobs but use with caution
     * elsewhere as the conference might also come from a deep link in which case this value
     * would conflict
     */
    fun getConferenceFlow(): Flow<String> {
        return appSettings.getConferenceFlow()
    }

    suspend fun setConference(conference: String, conferenceThemeColor: String?) {
        println(conference)
        appSettings.setConference(conference)
        conferenceThemeColor?.let {
            appSettings.setConferenceThemeColor(conferenceThemeColor)
        }
        updateConfenceListeners(conference, conferenceThemeColor)
    }

    suspend fun refresh(conference: String, fetchPolicy: FetchPolicy) {
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

    fun sessionDetailsQuery(
        conference: String,
        sessionId: String,
        fetchPolicy: FetchPolicy
    ): ApolloCall<GetSessionQuery.Data> =
        apolloClientCache.getClient(conference)
            .query(GetSessionQuery(sessionId))
            .fetchPolicy(fetchPolicy)

    fun sessionDetails(
        conference: String,
        sessionId: String,
        fetchPolicy: FetchPolicy
    ): Flow<ApolloResponse<GetSessionQuery.Data>> =
        sessionDetailsQuery(conference, sessionId, fetchPolicy)
            .toFlow()

    suspend fun conferenceData(
        conference: String,
        fetchPolicy: FetchPolicy
    ): ApolloResponse<GetConferenceDataQuery.Data> =
        apolloClientCache.getClient(conference).query(GetConferenceDataQuery())
            .fetchPolicy(fetchPolicy).execute()

    fun sessionsQuery(
        conference: String,
        fetchPolicy: FetchPolicy
    ): ApolloCall<GetSessionsQuery.Data> =
        apolloClientCache.getClient(conference).query(GetSessionsQuery())
            .fetchPolicy(fetchPolicy)

    suspend fun sessions(
        conference: String,
        uid: String?,
        tokenProvider: TokenProvider?,
        fetchPolicy: FetchPolicy
    ): ApolloResponse<GetSessionsQuery.Data> =
        apolloClientCache.getClient(conference, uid).query(GetSessionsQuery())
            .tokenProvider(tokenProvider)
            .fetchPolicy(fetchPolicy)
            .execute()

    fun conferenceHomeData(
        conference: String,
        fetchPolicy: FetchPolicy
    ): ApolloCall<GetConferenceDataQuery.Data> {
        return apolloClientCache.getClient(conference)
            .query(GetConferenceDataQuery())
            .fetchPolicy(fetchPolicy)
    }

    fun speakerQuery(conference: String, id: String): ApolloCall<GetSpeakerQuery.Data> {
        return apolloClientCache.getClient(conference).query(GetSpeakerQuery(id))
    }
}
