package dev.johnoreilly.kikiconf

import com.apollographql.apollo3.ApolloClient
import com.apollographql.apollo3.cache.normalized.watch
import com.rickclephas.kmp.nativecoroutines.NativeCoroutineScope
import com.russhwolf.settings.ExperimentalSettingsApi
import com.russhwolf.settings.ObservableSettings
import com.russhwolf.settings.coroutines.getStringOrNullFlow
import dev.johnoreilly.kikiconf.fragment.SessionDetails
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject


// needed for iOS client as "description" is reserved
fun SessionDetails.sessionDescription() = this.description

@ExperimentalSettingsApi
@OptIn(ExperimentalCoroutinesApi::class)
class KikiConfRepository: KoinComponent {
    @NativeCoroutineScope
    private val coroutineScope: CoroutineScope = MainScope()

    private val apolloClient: ApolloClient by inject()

    val settings: ObservableSettings by inject()

    val enabledLanguages: Flow<Set<String>> =
        settings.getStringOrNullFlow(ENABLED_LANGUAGES_SETTING)
            .map {
                if (it == null) return@map emptySet<String>()
                it.split(",").toSet()
            }


    init {
        settings.putString("enabled_languages", "French,English")
    }

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

    fun onLanguageChecked(language: String, checked: Boolean) {
        // TODO cleaner way of doing this?
        val currentEnabledLanguagesString = settings.getStringOrNull(ENABLED_LANGUAGES_SETTING)
        val currentEnabledLanguagesSet = if (currentEnabledLanguagesString == null)
            emptySet()
        else
            currentEnabledLanguagesString.split(",").toSet()

        val newEnabledLanguagesString = if (checked) {
            currentEnabledLanguagesSet.plus(language)
        } else {
            currentEnabledLanguagesSet.minus(language)
        }
        settings.putString(ENABLED_LANGUAGES_SETTING, newEnabledLanguagesString.joinToString(separator = ","))
    }

    companion object {
        const val ENABLED_LANGUAGES_SETTING = "enabled_languages"
    }

}