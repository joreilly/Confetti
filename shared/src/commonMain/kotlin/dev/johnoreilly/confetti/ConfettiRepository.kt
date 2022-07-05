package dev.johnoreilly.confetti

import com.apollographql.apollo3.ApolloClient
import com.apollographql.apollo3.api.Optional
import com.apollographql.apollo3.cache.normalized.FetchPolicy
import com.apollographql.apollo3.cache.normalized.apolloStore
import com.apollographql.apollo3.cache.normalized.fetchPolicy
import com.apollographql.apollo3.cache.normalized.watch
import com.rickclephas.kmp.nativecoroutines.NativeCoroutineScope
import dev.johnoreilly.confetti.fragment.SessionDetails
import dev.johnoreilly.confetti.fragment.SpeakerDetails
import dev.johnoreilly.confetti.utils.DateTimeFormatter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject


// needed for iOS client as "description" is reserved
fun SessionDetails.sessionDescription() = this.description


fun SpeakerDetails.imageUrl(): String {
    return "https://raw.githubusercontent.com/paug/android-makers-2022/main/$photoUrl"
        .replace("..", "")
        .replace(".svg", ".svg.png")
        .let {
            if (it.endsWith(".svg.png")) {
                it.replace("logos/", "logos/pngs/")
            } else {
                it
            }
        }
}


class ConfettiRepository : KoinComponent {
    @NativeCoroutineScope
    private val coroutineScope: CoroutineScope = MainScope()

    private val apolloClient: ApolloClient by inject()
    private val appSettings: AppSettings by inject()
    private val dateTimeFormatter: DateTimeFormatter by inject()

    private var timeZone: TimeZone = TimeZone.currentSystemDefault()

    val enabledLanguages = appSettings.enabledLanguages

    val sessions = MutableStateFlow<List<SessionDetails>>(emptyList())
    private var hasFetchedAllSessions = false
    private var isFetchingSessions = false

    val favoriteSessions = MutableStateFlow<List<SessionDetails>>(emptyList())
    private var hasFetchedAllFavoriteSessions = false
    private var isFetchingFavoriteSessions = false

    val speakers = apolloClient.query(GetSpeakersQuery()).watch().map {
        it.dataAssertNoErrors.speakers.map { it.speakerDetails }
    }

    val rooms = apolloClient.query(GetRoomsQuery()).watch().map {
        it.dataAssertNoErrors.rooms.map { it.roomDetails }
    }


    init {
        coroutineScope.launch {
            val configResponse = apolloClient.query(GetConfigurationQuery()).execute()
            configResponse.data?.config?.timezone?.let {
                timeZone = TimeZone.of(it)
            }

            launch {
                // Fetch the first page and watch it.
                // Calling fetchMoreSessions() will fetch the following pages and notify watchers.
                apolloClient.query(GetSessionsQuery(first = Optional.Present(15)))
                    .fetchPolicy(FetchPolicy.CacheAndNetwork)
                    .watch()
                    .map {
                        it.dataAssertNoErrors.sessions.edges
                            .map { it.node.sessionDetails }
                    }.combine(enabledLanguages) { sessionList, enabledLanguages ->
                        sessionList.filter { enabledLanguages.contains(it.language) }
                    }.collect {
                        sessions.value = it
                    }
            }

            launch {
                // Fetch the first page and watch it.
                // Calling fetchMoreSessions() will fetch the following pages and notify watchers.
                apolloClient.query(GetFavoriteSessionsQuery(first = Optional.Present(15)))
                    .fetchPolicy(FetchPolicy.CacheAndNetwork)
                    .watch()
                    .map {
                        it.dataAssertNoErrors.sessions.edges
                            .map { it.node.sessionDetails }
                    }.combine(enabledLanguages) { sessionList, enabledLanguages ->
                        sessionList.filter { enabledLanguages.contains(it.language) }
                    }.collect {
                        favoriteSessions.value = it
                    }
            }
        }
    }

    fun getSessionTime(session: SessionDetails): String {
        return dateTimeFormatter.format(session.startInstant, timeZone, "HH:mm")
    }

    fun getSession(sessionId: String): Flow<SessionDetails?> {
        return apolloClient.query(GetSessionQuery(sessionId)).watch().map { it.data?.session?.sessionDetails }
    }

    fun updateEnableLanguageSetting(language: String, checked: Boolean) {
        appSettings.updateEnableLanguageSetting(language, checked)
    }

    fun fetchMoreSessions() {
        if (isFetchingSessions || hasFetchedAllSessions) return
        isFetchingSessions = true
        coroutineScope.launch {
            // Get the last cursor from the cache
            val lastCursor = try {
                apolloClient.query(GetSessionsQuery())
                    .fetchPolicy(FetchPolicy.CacheOnly)
                    .execute()
                    .dataAssertNoErrors.sessions.edges.last().cursor
            } catch (e: Exception) {
                null
            }

            // Fetch the page after it, from the network
            try {
                val fetchedItemCount =
                    apolloClient.query(GetSessionsQuery(after = Optional.presentIfNotNull(lastCursor)))
                        .fetchPolicy(FetchPolicy.NetworkOnly)
                        .execute()
                        .dataAssertNoErrors.sessions.edges.size
                hasFetchedAllSessions = fetchedItemCount == 0
            } catch (_: Exception) {
            } finally {
                isFetchingSessions = false
            }
        }
    }

    fun fetchMoreFavoriteSessions() {
        if (isFetchingFavoriteSessions || hasFetchedAllFavoriteSessions) return
        isFetchingFavoriteSessions = true
        coroutineScope.launch {
            // Get the last cursor from the cache
            val lastCursor = try {
                apolloClient.query(GetFavoriteSessionsQuery())
                    .fetchPolicy(FetchPolicy.CacheOnly)
                    .execute()
                    .dataAssertNoErrors.sessions.edges.last().cursor
            } catch (e: Exception) {
                null
            }

            // Fetch the page after it, from the network
            try {
                val fetchedItemCount =
                    apolloClient.query(
                        GetFavoriteSessionsQuery(
                            after = Optional.presentIfNotNull(
                                lastCursor
                            )
                        )
                    )
                        .fetchPolicy(FetchPolicy.NetworkOnly)
                        .execute()
                        .dataAssertNoErrors.sessions.edges.size
                hasFetchedAllFavoriteSessions = fetchedItemCount == 0
            } catch (_: Exception) {
            } finally {
                isFetchingFavoriteSessions = false
            }
        }
    }

    suspend fun setSessionFavorite(sessionId: String, isFavorite: Boolean) {
        apolloClient.mutation(SetSessionFavoriteMutation(sessionId, isFavorite))
            .execute()

        if (isFavorite) {
            // Session added to the favorite list: refresh the cache by fetching the first page
            val fetchedItemCount =
                apolloClient.query(GetFavoriteSessionsQuery())
                    .fetchPolicy(FetchPolicy.NetworkOnly)
                    .execute()
                    .dataAssertNoErrors.sessions.edges.size
            hasFetchedAllFavoriteSessions = fetchedItemCount == 0
        } else {
            // Session removed from the favorite list: manually update the cache
            val dataFromCache = apolloClient.apolloStore.readOperation(GetFavoriteSessionsQuery())
            val updatedData = dataFromCache.copy(
                sessions = dataFromCache.sessions.copy(
                    edges = dataFromCache.sessions.edges.filterNot { it.node.id == sessionId }
                )
            )
            apolloClient.apolloStore.writeOperation(GetFavoriteSessionsQuery(), updatedData)
        }
    }

}
