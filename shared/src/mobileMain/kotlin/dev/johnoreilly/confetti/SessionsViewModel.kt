package dev.johnoreilly.confetti

import com.apollographql.apollo3.api.ApolloResponse
import com.apollographql.apollo3.cache.normalized.FetchPolicy
import com.rickclephas.kmm.viewmodel.KMMViewModel
import com.rickclephas.kmm.viewmodel.coroutineScope
import com.rickclephas.kmm.viewmodel.stateIn
import com.rickclephas.kmp.nativecoroutines.NativeCoroutinesState
import dev.johnoreilly.confetti.AppSettings.Companion.CONFERENCE_NOT_SET
import dev.johnoreilly.confetti.fragment.RoomDetails
import dev.johnoreilly.confetti.fragment.SessionDetails
import dev.johnoreilly.confetti.fragment.SpeakerDetails
import dev.johnoreilly.confetti.utils.DateService
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

open class SessionsViewModel : KMMViewModel(), KoinComponent {
    private val repository: ConfettiRepository by inject()
    private val dateService: DateService by inject()

    private var conference: String = CONFERENCE_NOT_SET
    private val refreshDatas = Channel<RefreshData>()
    private var started: Boolean = false

    private fun maybeStart() {
        if (!started) {
            started = true
            viewModelScope.coroutineScope.launch {
                refresh(true)
            }
        }
    }

    fun addBookmark(sessionId: String) {
        viewModelScope.coroutineScope.launch {
            repository.addBookmark(conference, sessionId)
        }
    }

    fun removeBookmark(sessionId: String) {
        viewModelScope.coroutineScope.launch {
            repository.removeBookmark(conference, sessionId)
        }
    }

    private fun uiStates(bookmarksResponse: ApolloResponse<GetBookmarksQuery.Data>, sessionsResponse: ApolloResponse<GetConferenceDataQuery.Data>): SessionsUiState {
        val bookmarksData = bookmarksResponse.data
        val sessionsData = sessionsResponse.data

        if (
            bookmarksResponse.exception != null
            || bookmarksResponse.hasErrors()
            || sessionsResponse.exception != null
            || sessionsResponse.hasErrors()
            || sessionsData == null
            || bookmarksData == null
        ) {
            bookmarksResponse.exception?.printStackTrace()
            sessionsResponse.exception?.printStackTrace()
            bookmarksResponse.errors?.let { println(it) }
            sessionsResponse.errors?.let { println(it) }
            return SessionsUiState.Error
        }

        val sessionsMap = sessionsData.sessions.nodes.map { it.sessionDetails }.groupBy { it.startsAt.date }
        val speakers = sessionsData.speakers.map { it.speakerDetails }
        val rooms = sessionsData.rooms.map { it.roomDetails }

        val confDates = sessionsMap.keys.toList().sorted()

        val sessionsByStartTimeList = mutableListOf<Map<String, List<SessionDetails>>>()
        confDates.forEach { confDate ->
            val sessions = sessionsMap[confDate] ?: emptyList()
            val sessionsByStartTime = sessions.groupBy { getSessionTime(it) }
            sessionsByStartTimeList.add(sessionsByStartTime)
        }
        return SessionsUiState.Success(
            dateService.now(),
            conference,
            confDates,
            sessionsByStartTimeList,
            speakers,
            rooms,
            bookmarksData.bookmarks?.sessionIds.orEmpty().toSet()
        )
    }

    class RefreshData(
        val bookmarksResponse: ApolloResponse<GetBookmarksQuery.Data>,
        val sessionsResponse: ApolloResponse<GetConferenceDataQuery.Data>,
    )

    @NativeCoroutinesState
    val uiState: StateFlow<SessionsUiState> = flow {
        for (i in refreshDatas) {
            emit(i)
        }
    }
        .flatMapLatest { refreshData ->
            val bookmarksData = refreshData.bookmarksResponse.data
            repository.watchBookmarks(conference, bookmarksData).onStart {
                refreshData.bookmarksResponse.let { emit(it) }
            }.map {
                uiStates(it, refreshData.sessionsResponse)
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SessionsUiState.Loading)

    val speakers = repository.speakers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())

    fun setConference(conference: String) {
        this.conference = conference
        maybeStart()
    }

    suspend fun refresh() = refresh(false)

    private suspend fun refresh(initial: Boolean) {
        val fetchPolicy = if (initial) {
            FetchPolicy.CacheFirst
        } else {
            FetchPolicy.NetworkOnly
        }
        coroutineScope {
            val bookmarksResponse = async {
                repository.bookmarks(conference, fetchPolicy)
            }
            val sessionsResponse = async {
                repository.conferenceData(conference, fetchPolicy)
            }

            delay(2000)
            refreshDatas.send(
                RefreshData(
                    bookmarksResponse = bookmarksResponse.await(),
                    sessionsResponse = sessionsResponse.await()
                )
            )
        }
    }

    fun getSessionTime(session: SessionDetails): String {
        return dateService.format(session.startsAt, repository.timeZone, "HH:mm")
    }
}

sealed interface SessionsUiState {
    object Loading : SessionsUiState
    object Error : SessionsUiState

    data class Success(
        val now: LocalDateTime,
        val conferenceName: String,
        val confDates: List<LocalDate>,
        val sessionsByStartTimeList: List<Map<String, List<SessionDetails>>>,
        val speakers: List<SpeakerDetails>,
        val rooms: List<RoomDetails>,
        val bookmarks: Set<String>,
    ) : SessionsUiState
}
