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
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

open class ConfettiViewModel: KMMViewModel(), KoinComponent {
    private val repository: ConfettiRepository by inject()

    private val dateService: DateService by inject()

    @NativeCoroutinesState
    val conferenceList = repository.conferenceList.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())

    @NativeCoroutinesState
    val uiState: StateFlow<SessionsUiState> =
        repository.conferenceDataFlow().map {
            if (it == null) {
                SessionsUiState.Loading
            } else {
                val conferenceName = it.config.name
                val sessionsMap = it.sessionsMap
                val speakers = it.speakers.map { it.speakerDetails }
                val rooms = it.rooms.map { it.roomDetails }
                val timezone = it.timeZone

                val confDates = sessionsMap.keys.toList().sorted()

                val sessionsByStartTimeList = mutableListOf<Map<String, List<SessionDetails>>>()
                confDates.forEach { confDate ->
                    val sessions = sessionsMap[confDate] ?: emptyList()
                    val sessionsByStartTime = sessions.groupBy { getSessionTime(it, timezone) }
                    sessionsByStartTimeList.add(sessionsByStartTime)
                }
                SessionsUiState.Success(
                    conference = it.config.id,
                    now = dateService.now(),
                    conferenceName = conferenceName,
                    confDates = confDates,
                    sessionsByStartTimeList = sessionsByStartTimeList,
                    speakers = speakers,
                    rooms = rooms
                )
            }

        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), SessionsUiState.Loading)

    @NativeCoroutinesState
    val savedConference = repository.conferenceFlow()

    fun setConference(conference: String?) {
        viewModelScope.coroutineScope.launch {
            repository.setConference(conference)
        }
    }

    fun clearConference() {
        setConference(null)
    }

    suspend fun refresh()  {
        repository.refresh()
    }

    fun getSessionTime(session: SessionDetails, timezone: TimeZone): String {
        return dateService.format(session.startInstant, timezone, "HH:mm")
    }
}

sealed interface SessionsUiState {
    object Loading : SessionsUiState

    data class Success(
        val conference: String,
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
