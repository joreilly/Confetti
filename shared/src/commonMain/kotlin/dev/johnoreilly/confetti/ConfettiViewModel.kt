package dev.johnoreilly.confetti

import com.rickclephas.kmp.nativecoroutines.NativeCoroutines
import dev.johnoreilly.confetti.fragment.RoomDetails
import dev.johnoreilly.confetti.fragment.SessionDetails
import com.rickclephas.kmm.viewmodel.*
import dev.johnoreilly.confetti.fragment.SpeakerDetails
import kotlinx.coroutines.flow.*
import kotlinx.datetime.LocalDate
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

open class ConfettiViewModel: KMMViewModel(), KoinComponent {
    private val repository: ConfettiRepository by inject()

    val conferenceList = repository.conferenceList

    @NativeCoroutines
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
            SessionsUiState.Success(conferenceName, confDates, sessionsByStartTimeList,
                speakers, rooms)

        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), SessionsUiState.Loading)


    val speakers = repository.speakers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())


    @NativeCoroutines
    val savedConference = MutableStateFlow(viewModelScope, repository.getConference())

    fun setConference(conference: String) {
        repository.setConference(conference)
        savedConference.value = conference
    }

    suspend fun refresh()  {
        repository.refresh(networkOnly = true)
    }
}

sealed interface SessionsUiState {
    object Loading : SessionsUiState

    data class Success(
        val conferenceName: String,
        val confDates: List<LocalDate>,
        val sessionsByStartTimeList: List<Map<String, List<SessionDetails>>>,
        val speakers: List<SpeakerDetails>,
        val rooms: List<RoomDetails>
    ) : SessionsUiState
}
