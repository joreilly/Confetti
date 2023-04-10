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
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atTime
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

data class ResponseData(
    val bookmarksResponse: ApolloResponse<GetBookmarksQuery.Data>,
    val sessionsResponse: ApolloResponse<GetConferenceDataQuery.Data>,
)

open class SessionsViewModel : KMMViewModel(), KoinComponent {
    private val repository: ConfettiRepository by inject()
    private val dateService: DateService by inject()
    private var addErrorCount = 1
    private var removeErrorCount = 1

    private var conference: String? = null
    private var uid: String? = null
    private var tokenProvider: TokenProvider? = null

    private val responseDatas = Channel<ResponseData?>()


    fun addBookmark(sessionId: String) {
        viewModelScope.coroutineScope.launch {
            val success = repository.addBookmark(conference!!, uid, tokenProvider, sessionId)
            if (!success) {
                addErrorChannel.send(addErrorCount++)
            }
        }
    }

    fun removeBookmark(sessionId: String) {
        viewModelScope.coroutineScope.launch {
            val success = repository.removeBookmark(conference!!, uid, tokenProvider, sessionId)
            if (!success) {
                removeErrorChannel.send(removeErrorCount++)
            }
        }
    }

    val addErrorChannel = Channel<Int>()
    val removeErrorChannel = Channel<Int>()

    // exposed like this so it can be bound to in SwiftUI code
    @NativeCoroutinesState
    val searchQuery = MutableStateFlow("")

    @NativeCoroutinesState
    val uiState: StateFlow<SessionsUiState> = responseDatas.receiveAsFlow()
        .uiState()
        .combine(searchQuery) { uiState, search ->
            filterSessions(uiState, search)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SessionsUiState.Loading)

    // FIXME: can we pass that as a parameter somehow
    fun configure(conference: String, uid: String?, tokenProvider: TokenProvider?) {
        val hasChanged = this.conference != conference || this.uid != uid
        this.conference = conference
        this.uid = uid
        this.tokenProvider = tokenProvider

        refresh(showLoading = hasChanged, forceRefresh = false)
    }

    private var job: Job? = null

    /**
     * @param showLoading whether to show a loading state (if user or conference changed)
     * @param forceRefresh whether to force a network refresh (pull-to-refresh)
     */
    fun refresh(showLoading: Boolean, forceRefresh: Boolean) {
        job?.cancel()
        job = viewModelScope.coroutineScope.launch {
            responseData(showLoading, forceRefresh).collect {
                responseDatas.send(it)
            }
        }
    }

    suspend fun refresh() = refresh(showLoading = false, forceRefresh = true)

    fun onSearch(searchString: String) {
        searchQuery.value = searchString
    }

    private fun filterSessions(uiState: SessionsUiState, filter: String): SessionsUiState {
        return if (filter.isNotBlank() && uiState is SessionsUiState.Success) {
            val newSessions = uiState.sessionsByStartTimeList.map { outerMap ->
                outerMap.mapValues { (_, value) ->
                    value.filter { session ->
                        filterSessionDetails(session, filter)
                    }
                }.filterValues { it.isNotEmpty() }
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

    private fun responseData(showLoading: Boolean, forceRefresh: Boolean): Flow<ResponseData?> =
        flow {
            if (showLoading) {
                emit(null)
            }
            val fetchPolicy = if (forceRefresh) {
                FetchPolicy.NetworkOnly
            } else {
                FetchPolicy.CacheFirst
            }

            // get initial data
            coroutineScope {
                val bookmarksResponse = async {
                    repository.bookmarks(conference!!, uid, tokenProvider, fetchPolicy).first()
                }
                val sessionsResponse = async {
                    repository.conferenceData(conference!!, fetchPolicy)
                }

                ResponseData(bookmarksResponse.await(), sessionsResponse.await())
            }.also {
                emit(it)
            }
        }

    private fun Flow<ResponseData?>.uiState() = flatMapLatest { responseData ->
        if (responseData == null) {
            flowOf(SessionsUiState.Loading)
        } else {
            val bookmarksData = responseData.bookmarksResponse.data
            repository.watchBookmarks(conference!!, uid, tokenProvider, bookmarksData)
                .map { responseData.copy(bookmarksResponse = it) }
                .onStart {
                    emit(responseData)
                }.map {
                    uiStates(it)
                }
        }
    }

    private fun getSessionTime(session: SessionDetails, timeZone: TimeZone): String {
        return dateService.format(session.startsAt, timeZone, "HH:mm")
    }

    private fun uiStates(
        refreshData: ResponseData
    ): SessionsUiState {
        val bookmarksResponse = refreshData.bookmarksResponse
        val sessionsResponse = refreshData.sessionsResponse
        val bookmarksData = bookmarksResponse.data
        val sessionsData = sessionsResponse.data

        if (sessionsData == null || bookmarksData == null) {
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
        val speakers = sessionsData.speakers.nodes.map { it.speakerDetails }
        val rooms = sessionsData.rooms.map { it.roomDetails }

        val confDates = sessionsMap.keys.toList().sorted()

        val sessionsByStartTimeList = mutableListOf<Map<String, List<SessionDetails>>>()
        confDates.forEach { confDate ->
            val sessions = sessionsMap[confDate] ?: emptyList()

            val sessionsByStartTime = sessions
                .groupBy { getSessionTime(it, timeZone) }
                .mapValues {
                    rooms.mapNotNull { room ->
                        it.value.find { session -> session.room?.name == room.name }
                    }
                }

            sessionsByStartTimeList.add(sessionsByStartTime)
        }

        val formattedConfDates = confDates.map { date ->
            dateService.format(date.atTime(0, 0), timeZone, "MMM dd, yyyy")
        }
        return SessionsUiState.Success(
            conference = conference!!,
            now = dateService.now(),
            conferenceName = conferenceName,
            confDates = confDates,
            formattedConfDates = formattedConfDates,
            sessionsByStartTimeList = sessionsByStartTimeList,
            speakers = speakers,
            rooms = rooms,
            bookmarks = bookmarksData.bookmarks?.sessionIds.orEmpty().toSet(),
        )
    }
}


sealed interface SessionsUiState {
    object Loading : SessionsUiState
    object Error : SessionsUiState

    data class Success(
        val conference: String,
        val now: LocalDateTime,
        val conferenceName: String,
        val confDates: List<LocalDate>,
        val formattedConfDates: List<String>,
        val sessionsByStartTimeList: List<Map<String, List<SessionDetails>>>,
        val speakers: List<SpeakerDetails>,
        val rooms: List<RoomDetails>,
        val bookmarks: Set<String>,
    ) : SessionsUiState
}
