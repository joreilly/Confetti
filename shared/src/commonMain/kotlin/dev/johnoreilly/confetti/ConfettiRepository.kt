package dev.johnoreilly.confetti

import com.apollographql.apollo3.ApolloClient
import com.apollographql.apollo3.api.Optional
import com.apollographql.apollo3.cache.normalized.watch
import com.rickclephas.kmp.nativecoroutines.NativeCoroutineScope
import dev.johnoreilly.confetti.fragment.SessionDetails
import dev.johnoreilly.confetti.fragment.SpeakerDetails
import dev.johnoreilly.confetti.utils.DateTimeFormatter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject



// needed for iOS client as "description" is reserved
fun SessionDetails.sessionDescription() = this.description


class ConfettiRepository: KoinComponent {
    @NativeCoroutineScope
    private val coroutineScope: CoroutineScope = MainScope()

    private val apolloClient: ApolloClient by inject()
    private val appSettings: AppSettings by inject()
    private val dateTimeFormatter: DateTimeFormatter by inject()

    private var timeZone: TimeZone = TimeZone.currentSystemDefault()

    val enabledLanguages = appSettings.enabledLanguages

    val sessions = MutableStateFlow<List<SessionDetails>>(emptyList())

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

            // TODO: We fetch the first page only, assuming there are <100 conferennces. Pagination should be implemented instead.
            apolloClient.query(GetSessionsQuery(first = Optional.Present(100))).watch().map {
                it.dataAssertNoErrors.sessions.nodes
                    .map { it.sessionDetails }
                    .sortedBy { it.startInstant }
            }.combine(enabledLanguages) { sessionList, enabledLanguages ->
                sessionList.filter { enabledLanguages.contains(it.language) }
            }.collect {
                sessions.value = it
            }
        }
    }

    fun getSessionTime(session: SessionDetails): String {
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
