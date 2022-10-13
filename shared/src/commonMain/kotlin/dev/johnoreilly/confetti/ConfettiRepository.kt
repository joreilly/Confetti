package dev.johnoreilly.confetti

import com.apollographql.apollo3.ApolloClient
import com.apollographql.apollo3.api.Optional
import com.apollographql.apollo3.cache.normalized.FetchPolicy
import com.apollographql.apollo3.cache.normalized.fetchPolicy
import com.apollographql.apollo3.cache.normalized.isFromCache
import com.apollographql.apollo3.cache.normalized.watch
import com.rickclephas.kmp.nativecoroutines.NativeCoroutineScope
import dev.johnoreilly.confetti.fragment.RoomDetails
import dev.johnoreilly.confetti.fragment.SessionDetails
import dev.johnoreilly.confetti.fragment.SpeakerDetails
import dev.johnoreilly.confetti.utils.DateTimeFormatter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject


// needed for iOS client as "description" is reserved
fun SessionDetails.sessionDescription() = this.description

fun SessionDetails.isBreak() = this.type == "break"

class ConfettiRepository : KoinComponent {
    @NativeCoroutineScope
    private val coroutineScope: CoroutineScope = MainScope()

    private val apolloClient: ApolloClient by inject()
    private val appSettings: AppSettings by inject()
    private val dateTimeFormatter: DateTimeFormatter by inject()

    val enabledLanguages = appSettings.enabledLanguages

    private sealed interface EverythingResult
    private class EverythingSuccess(val data: GetEverythingQuery.Data) : EverythingResult
    private class EverythingError(val throwable: Throwable) : EverythingResult
    private object EverythingLoading : EverythingResult

    private val everything = MutableStateFlow<EverythingResult>(EverythingLoading)

    val sessions: Flow<List<SessionDetails>>
        get() {
            return everything.filterIsInstance<EverythingSuccess>().map {
                it.data.sessions.nodes.map { it.sessionDetails }.sortedBy { it.startInstant }
            }
        }

    val sessionsMap: Flow<Map<LocalDate, List<SessionDetails>>> = sessions.map {
        it.groupBy { it.startInstant.toLocalDateTime(TimeZone.of(currentData.config.timezone)).date }
    }

    val speakers: Flow<List<SpeakerDetails>>
        get() {
            return everything.filterIsInstance<EverythingSuccess>().map { it.data.speakers.map { it.speakerDetails } }
        }

    val rooms: Flow<List<RoomDetails>>
        get() {
            return everything.filterIsInstance<EverythingSuccess>().map { it.data.rooms.map { it.roomDetails } }
        }

    init {
        coroutineScope.launch {
            // TODO: We fetch the first page only, assuming there are <100 conferences. Pagination should be implemented instead.
            apolloClient.query(GetEverythingQuery(first = Optional.present(100)))
                .fetchPolicy(FetchPolicy.CacheAndNetwork)
                .toFlow()
                .map { EverythingSuccess(it.dataAssertNoErrors) as EverythingResult }
                .catch {
                    emit(EverythingError(it))
                }.collect {
                    everything.value = it
                }
        }
    }

    private val currentData: GetEverythingQuery.Data
        get() {
            val everything = everything.value as? EverythingSuccess
            check(everything != null) {
                "Cannot call getSessionTime before we fetch the timeZone"
            }
            return everything.data
        }

    fun getSessionTime(session: SessionDetails): String {
        val timeZone = TimeZone.of(currentData.config.timezone)
        return dateTimeFormatter.format(session.startInstant, timeZone, "HH:mm")
    }

    suspend fun getSession(sessionId: String): SessionDetails? {
        val response = apolloClient.query(GetSessionQuery(sessionId)).execute()
        return response.data?.session?.sessionDetails
    }

    fun updateEnableLanguageSetting(language: String, checked: Boolean) {
        appSettings.updateEnableLanguageSetting(language, checked)
    }
}
