package dev.johnoreilly.confetti

import com.apollographql.apollo3.ApolloClient
import com.apollographql.apollo3.api.Optional
import com.apollographql.apollo3.cache.normalized.*
import com.apollographql.apollo3.cache.normalized.api.MemoryCacheFactory
import com.apollographql.apollo3.cache.normalized.sql.SqlNormalizedCacheFactory
import com.rickclephas.kmp.nativecoroutines.NativeCoroutineScope
import dev.johnoreilly.confetti.di.getDatabaseName
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

fun SpeakerDetails.getFullNameAndCompany(): String {
    return name + if (company.isNullOrBlank()) "" else ", " + this.company
}


data class Conference(val id: String, val name: String)

class ConfettiRepository : KoinComponent {
    @NativeCoroutineScope
    private val coroutineScope: CoroutineScope = MainScope()

    private var apolloClient: ApolloClient? = null
    private val appSettings: AppSettings by inject()
    private val dateTimeFormatter: DateTimeFormatter by inject()

    val enabledLanguages = appSettings.enabledLanguages

    // TODO query this from backend
    val conferenceList = listOf(
        Conference("droidconsf", "Droidcon San Francisco 2022"),
        Conference("frenchkit2022", "FrenchKit 2022"),
        Conference("graphqlsummit2022", "GraphQL Summit 2022"),
        Conference("devfestnantes", "DevFest Nantes 2022"),
        Conference("droidconlondon2022", "Droidcon London 2022"),
    )

    private val conferenceData = MutableStateFlow<GetConferenceDataQuery.Data?>(null)

    val conferenceName = conferenceData.filterNotNull().map { it.config.name }

    val sessions = conferenceData.filterNotNull().map {
        it.sessions.nodes.map { it.sessionDetails }.sortedBy { it.startInstant }
    }

    val sessionsMap: Flow<Map<LocalDate, List<SessionDetails>>> = sessions.map {
        it.groupBy { it.startInstant.toLocalDateTime(TimeZone.of(conferenceData.value?.config?.timezone ?: "")).date }
    }

    val speakers = conferenceData.filterNotNull().map {
        it.speakers.map { it.speakerDetails }
    }

    val rooms = conferenceData.filterNotNull().map {
        it.rooms.map { it.roomDetails }
    }


    init {
        val conference = appSettings.getConference()
        if (conference.isNotEmpty()) {
            setConference(conference)
        }
    }

    fun getConference(): String {
        return appSettings.getConference()
    }

    fun setConference(conference: String) {
        conferenceData.value = null
        appSettings.setConference(conference)

        apolloClient?.close()
        apolloClient = createApolloClient(conference)

        coroutineScope.launch {
            refresh(networkOnly = false)
        }
    }

    fun getSessionTime(session: SessionDetails): String {
        return conferenceData.value?.let {
            val timeZone = TimeZone.of(it.config.timezone)
            return dateTimeFormatter.format(session.startInstant, timeZone, "HH:mm")
        } ?: ""
    }

    suspend fun getSession(sessionId: String): SessionDetails? {
        val response = apolloClient?.query(GetSessionQuery(sessionId))?.execute()
        return response?.data?.session?.sessionDetails
    }

    fun updateEnableLanguageSetting(language: String, checked: Boolean) {
        appSettings.updateEnableLanguageSetting(language, checked)
    }

    suspend fun refresh(networkOnly: Boolean = true) {
        val fetchPolicy = if (networkOnly) FetchPolicy.NetworkOnly else FetchPolicy.CacheAndNetwork

        // TODO: We fetch the first page only, assuming there are <100 conferences. Pagination should be implemented instead.
        apolloClient?.let {
            it.query(GetConferenceDataQuery(first = Optional.present(100)))
                .fetchPolicy(fetchPolicy)
                .toFlow()
                .catch {
                    // this can be valid scenario of say offline and we get data from cache initially
                    // but can't connect to network.  TODO should we surface this somewhere?
                }.collect {
                    conferenceData.value = it.data
                }
        }
    }

    fun createApolloClient(conference: String): ApolloClient {
        val sqlNormalizedCacheFactory = SqlNormalizedCacheFactory(getDatabaseName(conference))
        val memoryFirstThenSqlCacheFactory = MemoryCacheFactory(10 * 1024 * 1024)
            .chain(sqlNormalizedCacheFactory)

        return ApolloClient.Builder()
            .serverUrl("https://graphql-dot-confetti-349319.uw.r.appspot.com/graphql?conference=$conference")
            //.serverUrl("http://10.0.2.2:8080/graphql?conference=graphqlsummit2022")
            .normalizedCache(memoryFirstThenSqlCacheFactory, writeToCacheAsynchronously = true)
            .build()
    }
}
