package dev.johnoreilly.confetti.wear.home

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.johnoreilly.confetti.ConfettiRepository
import dev.johnoreilly.confetti.fragment.RoomDetails
import dev.johnoreilly.confetti.fragment.SessionDetails
import dev.johnoreilly.confetti.fragment.SpeakerDetails
import dev.johnoreilly.confetti.utils.DateService
import dev.johnoreilly.confetti.wear.home.navigation.ConferenceHomeDestination
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

class HomeViewModel(
    savedStateHandle: SavedStateHandle,
    private val repository: ConfettiRepository,
    private val dateService: DateService
) : ViewModel() {
    fun refresh() {
        viewModelScope.launch {
            repository.refresh(networkOnly = true)
        }
    }

    private val conference: String =
        ConferenceHomeDestination.fromNavArgs(savedStateHandle)

    fun getSessionTime(session: SessionDetails): String {
        return dateService.format(session.startInstant, repository.timeZone, "HH:mm")
    }

    val uiState: StateFlow<HomeUiState> = if (conference.isNotEmpty()) {
        conferenceDataFlow()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), HomeUiState.Loading)
    } else {
        MutableStateFlow(HomeUiState.NoneSelected)
    }

    private fun conferenceDataFlow() = flow<HomeUiState> {
        val data = repository.conferenceHomeData(conference).toFlow()

        val results = data.map {
            val conferenceData = it.dataAssertNoErrors

            val timezone = TimeZone.of(
                conferenceData.config.timezone
            )

            val sessions =
                conferenceData.sessions.nodes.map { it.sessionDetails }.sortedBy { it.startInstant }

            val speakers = conferenceData.speakers.map { it.speakerDetails }

            val rooms = conferenceData.rooms.map { it.roomDetails }

            val sessionsMap = sessions.groupBy {
                it.startInstant.toLocalDateTime(timezone).date
            }
            val confDates = sessionsMap.keys.toList().sorted()

            val sessionsByStartTimeList = mutableListOf<Map<String, List<SessionDetails>>>()
            confDates.forEach { confDate ->
                val sessions = sessionsMap[confDate] ?: emptyList()
                val sessionsByStartTime = sessions.groupBy { getSessionTime(it) }
                sessionsByStartTimeList.add(sessionsByStartTime)
            }
            HomeUiState.Success(
                conference, dateService.now(), conference, confDates, sessionsByStartTimeList,
                speakers, rooms
            )
        }

        emitAll(results)
    }
}

sealed interface HomeUiState {
    object NoneSelected : HomeUiState
    object Loading : HomeUiState

    data class Success(
        val conference: String,
        val now: LocalDateTime,
        val conferenceName: String,
        val confDates: List<LocalDate>,
        val sessionsByStartTimeList: List<Map<String, List<SessionDetails>>>,
        val speakers: List<SpeakerDetails>,
        val rooms: List<RoomDetails>
    ) : HomeUiState {
        fun currentSessions(now: LocalDateTime): List<Pair<String, List<SessionDetails>>>? {
            val indexInDays = confDates.indexOf(now.date)
            return if (indexInDays != -1) {
                // TODO filter the right session times
                sessionsByStartTimeList[indexInDays].entries.take(2).map {
                    it.toPair()
                }
            } else {
                null
            }
        }
    }
}