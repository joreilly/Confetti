package dev.johnoreilly.confetti.wear.sessions

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.johnoreilly.confetti.ConfettiRepository
import dev.johnoreilly.confetti.GetSessionsQuery
import dev.johnoreilly.confetti.navigation.ConferenceDayKey
import dev.johnoreilly.confetti.wear.sessions.navigation.SessionsDestination
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.datetime.TimeZone
import java.util.TreeMap

class SessionsViewModel(
    savedStateHandle: SavedStateHandle,
    repository: ConfettiRepository,
) : ViewModel() {
    private val conferenceDay: ConferenceDayKey =
        SessionsDestination.fromNavArgs(savedStateHandle)

    val uiState: StateFlow<SessionsUiState> = flow {
        // TODO query for a single day
        val resultsFlow = repository.sessions(conferenceDay.conference)

        val sessions = resultsFlow.map {
            if (it.data != null) {
                buildUiState(it.data!!)
            } else {
                SessionsUiState.Error
            }
        }

        emitAll(sessions)
    }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SessionsUiState.Loading)

    private fun buildUiState(data: GetSessionsQuery.Data): SessionsUiState.Success {
        val sessionList = data.sessions.nodes.map { it.sessionDetails }
        val sessions = sessionList.filter {
            it.startsAt.date == conferenceDay.date
        }
        val sessionsByTime =
            sessions.groupByTo(TreeMap()) { it.startsAt }.map {
                SessionsUiState.SessionAtTime(it.key, it.value)
            }
        return SessionsUiState.Success(conferenceDay, sessionsByTime)
    }
}