package dev.johnoreilly.confetti

import com.apollographql.apollo3.api.ApolloResponse
import com.apollographql.apollo3.cache.normalized.FetchPolicy
import com.rickclephas.kmm.viewmodel.KMMViewModel
import com.rickclephas.kmm.viewmodel.coroutineScope
import com.rickclephas.kmm.viewmodel.stateIn
import com.rickclephas.kmp.nativecoroutines.NativeCoroutinesState
import dev.johnoreilly.confetti.fragment.RoomDetails
import dev.johnoreilly.confetti.fragment.SessionDetails
import dev.johnoreilly.confetti.fragment.SpeakerDetails
import dev.johnoreilly.confetti.utils.DateService
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atTime
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

open class SessionsViewModel : KMMViewModel(), KoinComponent {
    private val repository: ConfettiRepository by inject()
    private val dateService: DateService by inject()
    private var addErrorCount = 1
    private var removeErrorCount = 1

    private lateinit var conference: String
    private val responseDatas = Channel<ResponseData>()
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
            val success = repository.addBookmark(conference, sessionId)
            if (!success) {
                addErrorChannel.send(addErrorCount++)
            }
        }
    }

    fun removeBookmark(sessionId: String) {
        viewModelScope.coroutineScope.launch {
            val success = repository.removeBookmark(conference, sessionId)
            if (!success) {
                removeErrorChannel.send(removeErrorCount++)
            }
        }
    }

    private fun uiStates(
        refreshData: ResponseData
    ): SessionsUiState {
        val bookmarksResponse = refreshData.bookmarksResponse
        val sessionsResponse = refreshData.sessionsResponse
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

        val conferenceName = sessionsData.config.name
        val timeZone = sessionsData.config.timezone.toTimeZone()
        val sessionsMap =
            sessionsData.sessions.nodes.map { it.sessionDetails }.groupBy { it.startsAt.date }
        val speakers = sessionsData.speakers.map { it.speakerDetails }
        val rooms = sessionsData.rooms.map { it.roomDetails }

        val confDates = sessionsMap.keys.toList().sorted()

        val sessionsByStartTimeList = mutableListOf<Map<String, List<SessionDetails>>>()
        confDates.forEach { confDate ->
            val sessions = sessionsMap[confDate] ?: emptyList()
            val sessionsByStartTime =
                sessions.groupBy { getSessionTime(it, timeZone) }
            sessionsByStartTimeList.add(sessionsByStartTime)
        }

        val formattedConfDates = confDates.map { date ->
            dateService.format(date.atTime(0, 0), timeZone, "MMM dd, yyyy")
        }
        return SessionsUiState.Success(
            conference,
            dateService.now(),
            conferenceName,
            formattedConfDates,
            sessionsByStartTimeList,
            speakers,
            rooms,
            bookmarksData.bookmarks?.sessionIds.orEmpty().toSet(),
        )
    }

    data class ResponseData(
        val bookmarksResponse: ApolloResponse<GetBookmarksQuery.Data>,
        val sessionsResponse: ApolloResponse<GetConferenceDataQuery.Data>,
    )

    val addErrorChannel = Channel<Int>()
    val removeErrorChannel = Channel<Int>()

    private val _search = MutableStateFlow("")
    val search = _search.asStateFlow()

    @NativeCoroutinesState
    val uiState: StateFlow<SessionsUiState> = responseDatas.receiveAsFlow()
        .flatMapLatest { refreshData ->
            val bookmarksData = refreshData.bookmarksResponse.data
            repository.watchBookmarks(conference, bookmarksData)
                .map { refreshData.copy(bookmarksResponse = it) }
                .onStart {
                    emit(refreshData)
                }.map {
                    uiStates(it)
                }
        }.combine(_search) { uiState, search ->
            filterSessions(uiState, search)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SessionsUiState.Loading)

    // FIXME: can we pass that as a parameter somehow
    fun configure(conference: String) {
        this.conference = conference
        maybeStart()
    }

    suspend fun refresh() = refresh(false)

    fun onSearch(search: String) {
        _search.value = search
    }

    private fun filterSessions(uiState: SessionsUiState, filter: String): SessionsUiState {
        return if (filter.isNotBlank() && uiState is SessionsUiState.Success) {
            val newSessions = uiState.sessionsByStartTimeList.map { outerMap ->
                outerMap.mapValues { (_, value) ->
                    value.filter { session ->
                        filterSessionDetails(session, filter)
                    }
                }
            }
            uiState.copy(sessionsByStartTimeList = newSessions)
        } else {
            uiState
        }
    }

    private fun filterSessionDetails(details: SessionDetails, filter: String): Boolean {
        val ignoreCase = true
        return details.title.contains(filter, ignoreCase) ||
            details.sessionDescription.orEmpty().contains(filter, ignoreCase) ||
            details.room?.name?.contains(filter, ignoreCase) == true ||
            details.speakers.any { speaker ->
                speaker.speakerDetails.name.contains(filter, ignoreCase)
            }
    }

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

            responseDatas.send(
                ResponseData(
                    bookmarksResponse = bookmarksResponse.await(),
                    sessionsResponse = sessionsResponse.await()
                )
            )
        }
    }

    private fun getSessionTime(session: SessionDetails, timeZone: TimeZone): String {
        return dateService.format(session.startsAt, timeZone, "HH:mm")
    }
}

sealed interface SessionsUiState {
    object Loading : SessionsUiState
    object Error : SessionsUiState

    data class Success(
        val conference: String,
        val now: LocalDateTime,
        val conferenceName: String,
        val formattedConfDates: List<String>,
        val sessionsByStartTimeList: List<Map<String, List<SessionDetails>>>,
        val speakers: List<SpeakerDetails>,
        val rooms: List<RoomDetails>,
        val bookmarks: Set<String>,
    ) : SessionsUiState
}
