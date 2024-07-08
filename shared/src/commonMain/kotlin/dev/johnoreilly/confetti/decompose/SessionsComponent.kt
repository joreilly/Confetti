@file:OptIn(ExperimentalCoroutinesApi::class)

package dev.johnoreilly.confetti.decompose

import com.apollographql.apollo.api.ApolloResponse
import com.apollographql.apollo.cache.normalized.FetchPolicy
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.childContext
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.lifecycle.doOnStart
import conferenceDateFormat
import dev.johnoreilly.confetti.ConfettiRepository
import dev.johnoreilly.confetti.GetBookmarksQuery
import dev.johnoreilly.confetti.GetConferenceDataQuery
import dev.johnoreilly.confetti.auth.User
import dev.johnoreilly.confetti.fragment.RoomDetails
import dev.johnoreilly.confetti.fragment.SessionDetails
import dev.johnoreilly.confetti.fragment.SpeakerDetails
import dev.johnoreilly.confetti.utils.DateService
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.atTime
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import sessionTimeFormat

data class ResponseData(
    val bookmarksResponse: ApolloResponse<GetBookmarksQuery.Data>,
    val sessionsResponse: ApolloResponse<GetConferenceDataQuery.Data>,
)

interface SessionsComponent {

    val isLoggedIn: Boolean
    val addErrorChannel: Channel<Int>
    val removeErrorChannel: Channel<Int>
    val uiState: Value<SessionsUiState>

    fun addBookmark(sessionId: String)
    fun removeBookmark(sessionId: String)

    fun refresh()
    fun onSearch(searchString: String)
    fun onSessionClicked(id: String)
    fun onSessionSelectionChanged(id: String?)
    fun onSignInClicked()
}

class DefaultSessionsComponent(
    componentContext: ComponentContext,
    private val conference: String,
    private val user: User?,
    private val onSessionSelected: (id: String) -> Unit,
    private val onSignIn: () -> Unit,
) : SessionsComponent, KoinComponent, ComponentContext by componentContext {
    private val simpleComponent =
        SessionsSimpleComponent(
            componentContext = childContext(key = "Sessions"),
            conference = conference,
            user = user,
        )

    private val coroutineScope = coroutineScope()
    private val repository: ConfettiRepository by inject()
    private var addErrorCount = 1
    private var removeErrorCount = 1

    override val isLoggedIn: Boolean = user != null
    override val addErrorChannel: Channel<Int> = Channel<Int>()
    override val removeErrorChannel: Channel<Int> = Channel<Int>()
    override val uiState: Value<SessionsUiState> = simpleComponent.uiState.asValue(lifecycle = lifecycle)

    override fun addBookmark(sessionId: String) {
        coroutineScope.launch {
            val success = repository.addBookmark(conference, user?.uid, user, sessionId)
            if (!success) {
                addErrorChannel.send(addErrorCount++)
            }
        }
    }

    override fun removeBookmark(sessionId: String) {
        coroutineScope.launch {
            val success = repository.removeBookmark(conference, user?.uid, user, sessionId)
            if (!success) {
                removeErrorChannel.send(removeErrorCount++)
            }
        }
    }

    override fun refresh() {
        simpleComponent.refresh(forceRefresh = true)
    }

    override fun onSearch(searchString: String) {
        simpleComponent.onSearch(searchString = searchString)
    }

    override fun onSessionClicked(id: String) {
        onSessionSelected(id)
    }

    override fun onSessionSelectionChanged(id: String?) {
        simpleComponent.onSessionSelectionChanged(id)
    }

    override fun onSignInClicked() {
        onSignIn()
    }
}

class SessionsSimpleComponent(
    componentContext: ComponentContext,
    private val conference: String,
    private val user: User?,
    private val date: LocalDate? = null,
) : KoinComponent, ComponentContext by componentContext {
    private val coroutineScope = coroutineScope()
    private val repository: ConfettiRepository by inject()
    private val dateService: DateService by inject()
    private val responseDatas = MutableStateFlow<ResponseData?>(null)
    private val searchQuery = MutableStateFlow("")
    private val isRefreshing = MutableStateFlow(false)
    private val selectedSessionId = MutableStateFlow<String?>(null)

    val uiState: StateFlow<SessionsUiState> =
        combineUiState()
            .combine(searchQuery) { uiState, search ->
                filterSessions(uiState, search)
            }.stateIn(coroutineScope, SharingStarted.WhileSubscribed(5000), SessionsUiState.Loading)

    private var job: Job? = null

    init {
        lifecycle.doOnStart { refresh(showLoading = uiState.value !is SessionsUiState.Success) }
    }

    fun refresh(showLoading: Boolean = false, forceRefresh: Boolean = false) {
        job?.cancel()
        job = coroutineScope.launch {
            isRefreshing.value = showLoading
            responseData(showLoading, forceRefresh).collect {
                isRefreshing.value = false
                responseDatas.value = it
            }
        }
    }

    fun onSearch(searchString: String) {
        searchQuery.value = searchString
    }

    fun onSessionSelectionChanged(id: String?) {
        selectedSessionId.value = id
    }

    private fun filterSessions(uiState: SessionsUiState, filter: String): SessionsUiState {
        return if (uiState is SessionsUiState.Success) {
            val dateSessions = if (date != null) {
                uiState.sessionsByStartTimeList.filter {
                    it.values.first().first().startsAt.date == date
                }
            } else {
                uiState.sessionsByStartTimeList
            }

            val filteredSessions = if (filter.isNotBlank()) {
                dateSessions.map { outerMap ->
                    outerMap.mapValues { (_, value) ->
                        value.filter { session ->
                            filterSessionDetails(session, filter)
                        }
                    }.filterValues { it.isNotEmpty() }
                }
            } else {
                dateSessions
            }

            uiState.copy(sessionsByStartTimeList = filteredSessions)
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
                    repository.bookmarks(conference, user?.uid, user, fetchPolicy).first()
                }
                val sessionsResponse = async {
                    repository.conferenceData(conference, fetchPolicy)
                }

                ResponseData(bookmarksResponse.await(), sessionsResponse.await())
            }.also {
                emit(it)
            }
        }

    private fun combineUiState(): Flow<SessionsUiState> =
        responseDatas.flatMapLatest { responseData ->
            if (responseData == null) {
                flowOf(SessionsUiState.Loading)
            } else {
                val bookmarksData = responseData.bookmarksResponse.data
                combine(
                    repository.watchBookmarks(conference, user?.uid, user, bookmarksData)
                        .map { responseData.copy(bookmarksResponse = it) }
                        .onStart {
                            emit(responseData)
                        },
                    isRefreshing,
                    searchQuery,
                    selectedSessionId,
                    ::uiStates
                )
            }
        }

    private fun getSessionTime(session: SessionDetails): String {
        return session.startsAt.sessionTimeFormat()
    }

    private fun uiStates(
        refreshData: ResponseData,
        isRefreshing: Boolean,
        searchString: String,
        selectedSessionId: String?,
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
        val venueLat = sessionsData.venues.firstOrNull()?.latitude
        val venueLon = sessionsData.venues.firstOrNull()?.longitude
        val sessionsMap =
            sessionsData.sessions.nodes.map { it.sessionDetails }.groupBy { it.startsAt.date }
        val speakers = sessionsData.speakers.nodes.map { it.speakerDetails }
        val rooms = sessionsData.rooms.map { it.roomDetails }

        val confDates = sessionsMap.keys.toList().sorted()

        val sessionsByStartTimeList = mutableListOf<Map<String, List<SessionDetails>>>()
        confDates.forEach { confDate ->
            val sessions = sessionsMap[confDate] ?: emptyList()

            val sessionsByStartTime = sessions
                .groupBy { getSessionTime(it) }

            sessionsByStartTimeList.add(sessionsByStartTime)
        }

        val formattedConfDates = confDates.map { date ->
            date.atTime(0, 0).conferenceDateFormat()
        }
        return SessionsUiState.Success(
            now = dateService.now(),
            conference = conference,
            conferenceName = conferenceName,
            venueLat = venueLat,
            venueLon = venueLon,
            confDates = confDates,
            formattedConfDates = formattedConfDates,
            sessionsByStartTimeList = sessionsByStartTimeList,
            speakers = speakers,
            rooms = rooms,
            bookmarks = bookmarksData.bookmarks?.sessionIds.orEmpty().toSet(),
            isRefreshing = isRefreshing,
            searchString = searchString,
            selectedSessionId = selectedSessionId,
        )
    }
}

sealed interface SessionsUiState {
    object Loading : SessionsUiState
    object Error : SessionsUiState

    data class Success(
        val now: LocalDateTime,
        val conference: String,
        val conferenceName: String,
        val venueLat: Double?,
        val venueLon: Double?,
        val confDates: List<LocalDate>,
        val formattedConfDates: List<String>,
        val sessionsByStartTimeList: List<Map<String, List<SessionDetails>>>,
        val speakers: List<SpeakerDetails>,
        val rooms: List<RoomDetails>,
        val bookmarks: Set<String>,
        val isRefreshing: Boolean,
        val searchString: String,
        val selectedSessionId: String?,
    ) : SessionsUiState
}
