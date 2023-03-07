package dev.johnoreilly.confetti.wear.sessions

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.johnoreilly.confetti.ConfettiRepository
import dev.johnoreilly.confetti.fragment.SessionDetails
import dev.johnoreilly.confetti.navigation.ConferenceDayKey
import dev.johnoreilly.confetti.wear.sessions.navigation.SessionsDestination
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

class SessionsViewModel(
    savedStateHandle: SavedStateHandle,
    repository: ConfettiRepository,
) : ViewModel() {
    private val conferenceDay: ConferenceDayKey =
        SessionsDestination.fromNavArgs(savedStateHandle)

    val session: StateFlow<List<SessionDetails>?> = flow {
        val resultsFlow = repository.sessions(conferenceDay.conference)

        val sessions = resultsFlow
            .map {
                val timeZone = TimeZone.of(it.dataAssertNoErrors.config.timezone!!)
                val sessionList = it.dataAssertNoErrors.sessions.nodes.map { it.sessionDetails }
                sessionList.filter {
                    it.startInstant.toLocalDateTime(timeZone).date == conferenceDay.date
                }
            }
        emitAll(sessions)
    }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
}