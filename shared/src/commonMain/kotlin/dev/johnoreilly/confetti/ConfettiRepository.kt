@file:OptIn(ExperimentalCoroutinesApi::class)

package dev.johnoreilly.confetti

import com.apollographql.apollo3.cache.normalized.FetchPolicy
import com.apollographql.apollo3.cache.normalized.fetchPolicy
import dev.johnoreilly.confetti.fragment.SessionDetails
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class ConfettiRepository(private val defaultFetchPolicy: FetchPolicy) : KoinComponent {
    val coroutineScope: CoroutineScope = MainScope()

    private val appSettings: AppSettings by inject()

    private val apolloClientCache: ApolloClientCache by inject()

    val conferenceList: Flow<List<GetConferencesQuery.Conference>> = flow {
        val client = apolloClientCache.getClient("all")

        emitAll(client.query(GetConferencesQuery()).fetchPolicy(defaultFetchPolicy)
            .toFlow().mapNotNull {
                it.data?.conferences
            })
    }

    // TODO: We fetch the first page only, assuming there are <100 conferences. Pagination should be implemented instead.
    private fun conferenceQuery(conference: String): Flow<GetConferenceDataQuery.Data?> = flow {
        emitAll(apolloClientCache.getClient(conference).query(GetConferenceDataQuery())
            .fetchPolicy(defaultFetchPolicy)
            .toFlow()
            .map { it.data })
    }

    fun conferenceDataFlow(): Flow<GetConferenceDataQuery.Data?> = conferenceFlow().flatMapLatest {
        if (it != null) {
            conferenceQuery(it)
        } else {
            flowOf(null)
        }
    }

    fun conferenceDataFlow(conference: String): Flow<GetConferenceDataQuery.Data?> =
        conferenceQuery(conference)

    suspend fun getConference(): String? {
        return appSettings.getConference()
    }

    fun conferenceFlow(): Flow<String?> {
        return appSettings.getConferenceFlow()
    }

    suspend fun setConference(conference: String?) {
        appSettings.setConference(conference)
    }

    suspend fun getSession(conference: String, sessionId: String): SessionDetails? {
        return apolloClientCache.getClient(conference).query(GetSessionQuery(sessionId))
            .execute().data?.session?.sessionDetails
    }

    suspend fun updateEnableLanguageSetting(language: String, checked: Boolean) {
        appSettings.updateEnableLanguageSetting(language, checked)
    }

    fun refresh() {
        coroutineScope.launch {
            val conference = getConference()
            if (conference != null) {
                apolloClientCache.getClient(conference).query(
                    GetConferenceDataQuery()
                ).fetchPolicy(FetchPolicy.NetworkOnly)
            }
        }
    }
}

val GetConferenceDataQuery.Data.timeZone
    get() = config.timezone.let {
        TimeZone.of(it)
    } ?: TimeZone.currentSystemDefault()

val GetConferenceDataQuery.Data.sessionsList: List<SessionDetails>
    get() = sessions.nodes.map { it.sessionDetails }.sortedBy { it.startInstant }

val GetConferenceDataQuery.Data.sessionsMap
    get() = sessionsList.groupBy {
        it.startInstant.toLocalDateTime(TimeZone.of(config.timezone)).date
    }
