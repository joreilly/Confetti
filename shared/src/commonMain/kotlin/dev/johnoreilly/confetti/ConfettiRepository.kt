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
import kotlinx.coroutines.flow.*
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

    private var hasFetchedAllSessions = false
    private var isFetchingSessions = false

    val speakers = apolloClient.query(GetSpeakersQuery()).watch().map {
        it.dataAssertNoErrors.speakers.map { it.speakerDetails }
    }

    val rooms = apolloClient.query(GetRoomsQuery()).watch().map {
        it.dataAssertNoErrors.rooms.map { it.roomDetails }
    }

    val sessionsCacheInvalidated: SharedFlow<Unit> = apolloClient.query(GetSessionsQuery())
        .watch(data = null)
        .map { }
        .shareIn(coroutineScope, started = SharingStarted.Eagerly)

    fun getSessionTime(session: SessionDetails): String {
        return dateTimeFormatter.format(session.startInstant, timeZone, "HH:mm")
    }

    fun getSession(sessionId: String): Flow<SessionDetails?> {
        return apolloClient.query(GetSessionQuery(sessionId)).watch()
            .map { it.data?.session?.sessionDetails }
    }

    fun updateEnableLanguageSetting(language: String, checked: Boolean) {
        appSettings.updateEnableLanguageSetting(language, checked)
    }

    fun fetchMoreSessions(after: String?): Boolean {
        if (isFetchingSessions) return false
        if (hasFetchedAllSessions) return true
        isFetchingSessions = true
        coroutineScope.launch {
            try {
                val fetchedItemCount =
                    apolloClient.query(GetSessionsQuery(after = Optional.presentIfNotNull(after)))
                        .fetchPolicy(FetchPolicy.NetworkOnly)
                        .execute()
                        .dataAssertNoErrors.sessions.edges.size
                hasFetchedAllSessions = fetchedItemCount == 0
            } catch (_: Exception) {
            } finally {
                isFetchingSessions = false
            }
        }
        return hasFetchedAllSessions
    }

    suspend fun setSessionFavorite(sessionId: String, isFavorite: Boolean) {
        apolloClient.mutation(SetSessionFavoriteMutation(sessionId, isFavorite))
            .execute()
    }

    fun clearCache() {
        apolloClient.apolloStore.clearAll()
    }

    suspend fun getAllSessionsFromCache(): List<GetSessionsQuery.Edge> {
        return try {
            apolloClient.query(GetSessionsQuery())
                .fetchPolicy(FetchPolicy.CacheOnly)
                .execute()
                .dataAssertNoErrors.sessions.edges
        } catch (ApolloCacheException: Exception) {
            emptyList()
        }
    }
}
