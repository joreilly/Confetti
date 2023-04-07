package dev.johnoreilly.confetti.wear.sessiondetails

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.johnoreilly.confetti.ConfettiRepository
import dev.johnoreilly.confetti.navigation.SessionDetailsKey
import dev.johnoreilly.confetti.toTimeZone
import dev.johnoreilly.confetti.utils.DateService
import dev.johnoreilly.confetti.wear.sessiondetails.navigation.SessionDetailsDestination
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.datetime.TimeZone

class SessionDetailsViewModel(
    savedStateHandle: SavedStateHandle,
    repository: ConfettiRepository,
    val formatter: DateService
) : ViewModel() {
    private val sessionId: SessionDetailsKey =
        SessionDetailsDestination.fromNavArgs(savedStateHandle)

    val session: StateFlow<SessionDetailsUiState> = repository.sessionDetails(sessionId.conference, sessionId.sessionId).map {
        val sessionDetails = it
        if (sessionDetails.data != null) {
            SessionDetailsUiState.Success(
                sessionId.conference,
                sessionId,
                sessionDetails.data!!.session.sessionDetails,
                sessionDetails.data!!.config.timezone.toTimeZone()
            )
        } else {
            SessionDetailsUiState.Error
        }
    }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            SessionDetailsUiState.Loading
        )
}