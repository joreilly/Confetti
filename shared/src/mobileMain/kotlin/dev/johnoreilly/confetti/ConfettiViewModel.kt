package dev.johnoreilly.confetti

import com.rickclephas.kmm.viewmodel.KMMViewModel
import com.rickclephas.kmm.viewmodel.coroutineScope
import com.rickclephas.kmm.viewmodel.stateIn
import com.rickclephas.kmp.nativecoroutines.NativeCoroutinesState
import dev.johnoreilly.confetti.fragment.RoomDetails
import dev.johnoreilly.confetti.fragment.SessionDetails
import dev.johnoreilly.confetti.fragment.SpeakerDetails
import dev.johnoreilly.confetti.utils.DateService
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

open class ConfettiViewModel : KMMViewModel(), KoinComponent {
    private val repository: ConfettiRepository by inject()

    private val dateService: DateService by inject()

    fun addBookmark(sessionId: String) {
        repository.addBookmark(sessionId)
    }

    fun removeBookmark(sessionId: String) {
        repository.removeBookmark(sessionId)
    }

    private val conferenceRefresher: ConferenceRefresh by inject()

    @NativeCoroutinesState
    val conferenceList = repository.conferenceList.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(),
        emptyList()
    )

    @NativeCoroutinesState
    val uiState: StateFlow<SessionsUiState> =
        combine(
            repository.conferenceName,
            repository.sessionsMap,
            repository.speakers,
            repository.rooms,
            repository.bookmarks
        ) { conferenceName, sessionsMap, speakers, rooms, bookmarks ->
            val confDates = sessionsMap.keys.toList().sorted()

            val sessionsByStartTimeList = mutableListOf<Map<String, List<SessionDetails>>>()
            confDates.forEach { confDate ->
                val sessions = sessionsMap[confDate] ?: emptyList()
                val sessionsByStartTime = sessions.groupBy { getSessionTime(it) }
                sessionsByStartTimeList.add(sessionsByStartTime)
            }
            SessionsUiState.Success(
                dateService.now(), conferenceName, confDates, sessionsByStartTimeList,
                speakers, rooms, bookmarks.toSet()
            )

        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), SessionsUiState.Loading)


    val speakers = repository.speakers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())


    @NativeCoroutinesState
    val savedConference = repository.getConferenceFlow()
        .stateIn(viewModelScope, started = SharingStarted.Lazily, "")

    fun setConference(conference: String) {
        viewModelScope.coroutineScope.launch {
            repository.setConference(conference)
        }
    }

    fun clearConference() {
        setConference("")
    }

    suspend fun refresh() {
        conferenceRefresher.refresh(repository.getConference())
    }

    fun getSessionTime(session: SessionDetails): String {
        return dateService.format(session.startInstant, repository.timeZone, "HH:mm")
    }
}

sealed interface SessionsUiState {
    object Loading : SessionsUiState

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
