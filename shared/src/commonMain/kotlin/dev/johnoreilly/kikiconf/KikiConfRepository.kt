package dev.johnoreilly.kikiconf

import com.apollographql.apollo3.ApolloClient
import com.apollographql.apollo3.cache.normalized.watch
import com.rickclephas.kmp.nativecoroutines.NativeCoroutineScope
import dev.johnoreilly.kikiconf.fragment.SessionDetails
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject


// needed for iOS client as "description" is reserved
fun SessionDetails.sessionDescription() = this.description

class KikiConfRepository: KoinComponent {
    @NativeCoroutineScope
    private val coroutineScope: CoroutineScope = MainScope()

    private val apolloClient: ApolloClient by inject()
    private val appSettings: AppSettings by inject()

    val enabledLanguages = appSettings.enabledLanguages

    val sessions = apolloClient.query(GetSessionsQuery()).watch().map {
        it.dataAssertNoErrors.sessions.map { it.sessionDetails }
    }.combine(enabledLanguages) { sessions, enabledLanguages ->
        sessions.filter { enabledLanguages.contains(it.language) }
    }

    val speakers = apolloClient.query(GetSpeakersQuery()).watch().map {
        it.dataAssertNoErrors.speakers.map { it.speakerDetails }
    }

    val rooms = apolloClient.query(GetRoomsQuery()).watch().map {
        it.dataAssertNoErrors.rooms.map { it.roomDetails }
    }

    suspend fun getSession(sessionId: String): SessionDetails? {
        val response = apolloClient.query(GetSessionQuery(sessionId)).execute()
        return response.data?.session?.sessionDetails
    }

    fun updateEnableLanguageSetting(language: String, checked: Boolean) {
        appSettings.updateEnableLanguageSetting(language, checked)
    }
}