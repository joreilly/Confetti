package dev.johnoreilly.confetti.wear.home

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.johnoreilly.confetti.ConfettiRepository
import dev.johnoreilly.confetti.SessionsUiState
import dev.johnoreilly.confetti.fragment.SessionDetails
import dev.johnoreilly.confetti.utils.DateService
import dev.johnoreilly.confetti.wear.home.navigation.ConferenceHomeDestination
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
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

    val uiState: StateFlow<SessionsUiState> = flow {
        val data = repository.conferenceHomeData(conference).toFlow()

        val results = data.map {
            val conferenceData = it.dataAssertNoErrors
            val timezone = TimeZone.of(
                conferenceData.config?.timezone ?: ""
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
            SessionsUiState.Success(
                dateService.now(), conference, confDates, sessionsByStartTimeList,
                speakers, rooms
            )
        }

        emitAll(results)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), SessionsUiState.Loading)
}