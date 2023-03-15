package dev.johnoreilly.confetti

import com.rickclephas.kmm.viewmodel.KMMViewModel
import com.rickclephas.kmm.viewmodel.coroutineScope
import com.rickclephas.kmm.viewmodel.stateIn
import com.rickclephas.kmp.nativecoroutines.NativeCoroutinesState
import dev.johnoreilly.confetti.AppSettings.Companion.CONFERENCE_NOT_SET
import dev.johnoreilly.confetti.fragment.RoomDetails
import dev.johnoreilly.confetti.fragment.SessionDetails
import dev.johnoreilly.confetti.fragment.SpeakerDetails
import dev.johnoreilly.confetti.utils.DateService
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

open class ConfettiViewModel : KMMViewModel(), KoinComponent {
    private val repository: ConfettiRepository by inject()
    private val dateService: DateService by inject()

    private var conference: String = CONFERENCE_NOT_SET

    fun addBookmark(sessionId: String) {
        viewModelScope.coroutineScope.launch {
            repository.addBookmark(sessionId)
        }
    }

    fun removeBookmark(sessionId: String) {
        viewModelScope.coroutineScope.launch {
            repository.removeBookmark(sessionId)
        }
    }

    private val conferenceRefresher: ConferenceRefresh by inject()

    @NativeCoroutinesState
    val conferenceList = repository.conferenceList.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(),
        emptyList()
    )

    private fun uiStates(conference: String): Flow<SessionsUiState> = flow {
        val values = combine(
            repository.bookmarks(conference),
            repository.conferenceData(conference),
        ) { bookmarks, conferenceData ->
            val sessionsMap = conferenceData.data?.sessions?.nodes?.map { it.sessionDetails }
                ?.groupBy { it.startsAt.date }
            val speakers = conferenceData.data?.speakers?.map { it.speakerDetails }
            val rooms = conferenceData.data?.rooms?.map { it.roomDetails }

            if (sessionsMap == null || speakers == null || rooms == null) {
                return@combine SessionsUiState.Error
            }

            val confDates = sessionsMap.keys.toList().sorted()

            val sessionsByStartTimeList = mutableListOf<Map<String, List<SessionDetails>>>()
            confDates.forEach { confDate ->
                val sessions = sessionsMap[confDate] ?: emptyList()
                val sessionsByStartTime = sessions.groupBy { getSessionTime(it) }
                sessionsByStartTimeList.add(sessionsByStartTime)
            }
            SessionsUiState.Success(
                dateService.now(), conference, confDates, sessionsByStartTimeList,
                speakers, rooms, bookmarks.toSet()
            )
        }

        emitAll(values)
    }

    private val triggers = Channel<Unit>()

    @NativeCoroutinesState
    val uiState: StateFlow<SessionsUiState> = triggers.consumeAsFlow()
        .onStart {
            emit(Unit)
        }
        .flatMapLatest {
            uiStates(conference)
                .onStart {
                    emit(SessionsUiState.Loading)
                }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), SessionsUiState.Loading)


    val speakers = repository.speakers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())


    @NativeCoroutinesState
    val savedConference = repository.getConferenceFlow()
        .stateIn(viewModelScope, started = SharingStarted.Lazily, "")

    fun setConference(conference: String) {
        this.conference = conference
    }


    suspend fun refresh() {
        conferenceRefresher.refresh(repository.getConference())
    }

    fun refresh2() {
        triggers.trySend(Unit)
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
