package dev.johnoreilly.confetti

import com.rickclephas.kmm.viewmodel.KMMViewModel
import com.rickclephas.kmm.viewmodel.MutableStateFlow
import com.rickclephas.kmm.viewmodel.stateIn
import com.rickclephas.kmp.nativecoroutines.NativeCoroutinesState
import dev.johnoreilly.confetti.fragment.RoomDetails
import dev.johnoreilly.confetti.fragment.SessionDetails
import dev.johnoreilly.confetti.fragment.SpeakerDetails
import dev.johnoreilly.confetti.utils.DateTimeFormatter
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

open class ConfettiViewModel: KMMViewModel(), KoinComponent {
    private val repository: ConfettiRepository by inject()

    private val dateLogic: DateTimeFormatter by inject()

    @NativeCoroutinesState
    val conferenceList = repository.conferenceList.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())

    @NativeCoroutinesState
    val uiState: StateFlow<SessionsUiState> =
        combine(
            repository.conferenceName,
            repository.sessionsMap,
            repository.speakers,
            repository.rooms
        ) { conferenceName, sessionsMap, speakers, rooms,  ->
            val confDates = sessionsMap.keys.toList().sorted()

            val sessionsByStartTimeList = mutableListOf<Map<String, List<SessionDetails>>>()
            confDates.forEach { confDate ->
                val sessions = sessionsMap[confDate] ?: emptyList()
                val sessionsByStartTime = sessions.groupBy { repository.getSessionTime(it) }
                sessionsByStartTimeList.add(sessionsByStartTime)
            }
            SessionsUiState.Success(dateLogic.now(), conferenceName, confDates, sessionsByStartTimeList,
                speakers, rooms)

        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), SessionsUiState.Loading)


    val speakers = repository.speakers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())


    @NativeCoroutinesState
    val savedConference = MutableStateFlow(viewModelScope, repository.getConference())

    fun setConference(conference: String) {
        repository.setConference(conference)
        savedConference.value = conference
    }

    fun clearConference() {
        setConference("")
    }

    suspend fun refresh()  {
        repository.refresh(networkOnly = true)
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
        val rooms: List<RoomDetails>
    ) : SessionsUiState {

        fun currentSessions(now: LocalDateTime): List<Pair<String, List<SessionDetails>>>? {
            val indexInDays = confDates.indexOf(now.date)
            return if (indexInDays != -1) {
                // TODO filter the right session times
                sessionsByStartTimeList[indexInDays].entries.take(2).map {
                    it.toPair()
                }
            } else  {
                null
            }
        }
    }
}
