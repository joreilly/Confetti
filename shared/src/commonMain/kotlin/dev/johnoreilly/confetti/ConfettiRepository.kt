package dev.johnoreilly.confetti

import com.apollographql.apollo3.ApolloClient
import com.apollographql.apollo3.api.Optional
import com.apollographql.apollo3.cache.normalized.FetchPolicy
import com.apollographql.apollo3.cache.normalized.api.MemoryCacheFactory
import com.apollographql.apollo3.cache.normalized.fetchPolicy
import com.apollographql.apollo3.cache.normalized.normalizedCache
import com.apollographql.apollo3.cache.normalized.sql.SqlNormalizedCacheFactory
import dev.johnoreilly.confetti.di.getDatabaseName
import dev.johnoreilly.confetti.fragment.SessionDetails
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import okio.use
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.component.inject

class ConfettiRepository : KoinComponent {
    val coroutineScope: CoroutineScope = MainScope()

    private var apolloClient: ApolloClient? = null
    private val appSettings: AppSettings by inject()

    private var refreshJob: Job? = null

    val conferenceList: Flow<List<GetConferencesQuery.Conference>> = createApolloClient("all", false).use {
        it.query(GetConferencesQuery()).fetchPolicy(FetchPolicy.CacheAndNetwork).toFlow()
            .mapNotNull {
                println(it)
                it.data?.conferences
            }.catch {
            // do nothing
        }
    }

    private val conferenceData = MutableStateFlow<GetConferenceDataQuery.Data?>(null)

    val timeZone = conferenceData.value?.config?.timezone?.let {
        TimeZone.of(it)
    } ?: TimeZone.currentSystemDefault()

    val conferenceName = conferenceData.filterNotNull().map { it.config.name }

    val sessions = conferenceData.filterNotNull().map {
        it.sessions.nodes.map { it.sessionDetails }.sortedBy { it.startInstant }
    }

    val sessionsMap: Flow<Map<LocalDate, List<SessionDetails>>> = sessions.map {
        it.groupBy {
            it.startInstant.toLocalDateTime(
                TimeZone.of(
                    conferenceData.value?.config?.timezone ?: ""
                )
            ).date
        }
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
        refreshJob?.cancel()
        conferenceData.value = null
        appSettings.setConference(conference)

        apolloClient?.close()
        apolloClient = createApolloClient(conference)

        refreshJob = coroutineScope.launch {
            refresh(networkOnly = false)
        }
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
            it.query(GetConferenceDataQuery())
                .fetchPolicy(fetchPolicy)
                .toFlow()
                .catch {
                    // this can be valid scenario of say offline and we get data from cache initially
                    // but can't connect to network.  TODO should we surface this somewhere?
                }.collect {
                    println("got data, conf name = ${it.data?.config?.name}")
                    conferenceData.value = it.data
                }
        }
    }

    private fun createApolloClient(conference: String, writeToCacheAsynchronously: Boolean = true): ApolloClient {
        val sqlNormalizedCacheFactory = SqlNormalizedCacheFactory(getDatabaseName(conference))
        val memoryFirstThenSqlCacheFactory = MemoryCacheFactory(10 * 1024 * 1024)
            .chain(sqlNormalizedCacheFactory)

        return get<ApolloClient.Builder>()
            .serverUrl("https://graphql-dot-confetti-349319.uw.r.appspot.com/graphql?conference=$conference")
            //.serverUrl("http://10.0.2.2:8080/graphql?conference=graphqlsummit2022")
            .normalizedCache(memoryFirstThenSqlCacheFactory, writeToCacheAsynchronously = writeToCacheAsynchronously)
            .build()
    }
}
