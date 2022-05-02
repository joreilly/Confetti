package dev.johnoreilly.confetti

import com.apollographql.apollo3.ApolloClient
import com.apollographql.apollo3.cache.normalized.watch
import com.rickclephas.kmp.nativecoroutines.NativeCoroutineScope
import dev.johnoreilly.confetti.fragment.SessionDetails
import dev.johnoreilly.confetti.fragment.SpeakerDetails
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
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


class ConfettiRepository: KoinComponent {
    @NativeCoroutineScope
    private val coroutineScope: CoroutineScope = MainScope()

    private val apolloClient: ApolloClient by inject()
    private val appSettings: AppSettings by inject()

    private var timeZone: TimeZone = TimeZone.currentSystemDefault()

    val enabledLanguages = appSettings.enabledLanguages

    val sessions = apolloClient.query(GetSessionsQuery()).watch().map {
        it.dataAssertNoErrors.sessions
            .map { it.sessionDetails }
            .sortedBy { it.startInstant }
    }.combine(enabledLanguages) { sessions, enabledLanguages ->
        sessions.filter { enabledLanguages.contains(it.language) }
    }

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
        }
    }

    fun getSessionTime(session: SessionDetails): String {
        val localDateTime = session.startInstant.toLocalDateTime(timeZone)
        return "${localDateTime.hour}:${localDateTime.minute}"
    }

    suspend fun getSession(sessionId: String): SessionDetails? {
        val response = apolloClient.query(GetSessionQuery(sessionId)).execute()
        return response.data?.session?.sessionDetails
    }

    fun updateEnableLanguageSetting(language: String, checked: Boolean) {
        appSettings.updateEnableLanguageSetting(language, checked)
    }
}