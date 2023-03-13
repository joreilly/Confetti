@file:OptIn(ExperimentalCoroutinesApi::class)

package dev.johnoreilly.confetti

import com.apollographql.apollo3.ApolloCall
import com.apollographql.apollo3.api.ApolloResponse
import com.apollographql.apollo3.cache.normalized.FetchPolicy
import com.apollographql.apollo3.cache.normalized.fetchPolicy
import dev.johnoreilly.confetti.fragment.SessionDetails
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class ConfettiRepository(
    private val defaultFetchPolicy: FetchPolicy
) : KoinComponent {
    val coroutineScope: CoroutineScope = MainScope()

    private val appSettings: AppSettings by inject()

    private val apolloClientCache: ApolloClientCache by inject()

    val conferenceList: Flow<List<GetConferencesQuery.Conference>> = flow {
        val client = apolloClientCache.getClient("all")

        emitAll(client.query(GetConferencesQuery())
            .fetchPolicy(defaultFetchPolicy)
            .toFlow()
            .catch {
                // handle network failures by swallowing the error
            }
            .mapNotNull {
                it.data?.conferences
            })
    }

    private val conferenceData: StateFlow<GetConferenceDataQuery.Data?> =
        getConferenceFlow().flatMapLatest {
            if (it.isEmpty()) {
                flowOf(null)
            } else {
                apolloClientCache.getClient(it).query(GetConferenceDataQuery()).toFlow().map {
                    it.data
                }.catch {
                    // TODO log errors
                    emit(null)
                }
            }
        }.stateIn(coroutineScope, SharingStarted.Eagerly, null)

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

    suspend fun getConference(): String {
        return appSettings.getConference()
    }

    fun getConferenceFlow(): Flow<String> {
        return appSettings.getConferenceFlow()
    }

    suspend fun setConference(conference: String) {
        appSettings.setConference(conference)
    }

    suspend fun refresh(conference: String, networkOnly: Boolean = true) {
        val fetchPolicy = if (networkOnly) FetchPolicy.NetworkOnly else FetchPolicy.CacheAndNetwork

        // TODO: We fetch the first page only, assuming there are <100 conferences. Pagination should be implemented instead.
        apolloClientCache.getClient(conference)
            .query(GetConferenceDataQuery())
            .fetchPolicy(fetchPolicy)
            .execute()
    }

    suspend fun sessionDetails(
        conference: String,
        sessionId: String
    ): Flow<ApolloResponse<GetSessionQuery.Data>> =
        apolloClientCache.getClient(conference).query(GetSessionQuery(sessionId)).toFlow()

    suspend fun sessions(conference: String): Flow<ApolloResponse<GetSessionsQuery.Data>> =
        apolloClientCache.getClient(conference).query(GetSessionsQuery()).toFlow()

    suspend fun conferenceHomeData(conference: String): ApolloCall<GetConferenceDataQuery.Data> {
        return apolloClientCache.getClient(conference).query(GetConferenceDataQuery())
    }
}
