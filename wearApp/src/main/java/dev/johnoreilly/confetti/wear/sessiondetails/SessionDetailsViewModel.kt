package dev.johnoreilly.confetti.wear.sessiondetails

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.johnoreilly.confetti.ConfettiRepository
import dev.johnoreilly.confetti.navigation.SessionDetailsKey
import dev.johnoreilly.confetti.toTimeZone
import dev.johnoreilly.confetti.utils.ClientQuery.toUiState
import dev.johnoreilly.confetti.utils.QueryResult
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class SessionDetailsViewModel(
    savedStateHandle: SavedStateHandle,
    repository: ConfettiRepository
) : ViewModel() {
    val sessionId: SessionDetailsKey = TODO()
//        SessionDetailsDestination.fromNavArgs(savedStateHandle)

    val session: StateFlow<QueryResult<SessionDetailsUiState>> =
        repository.sessionDetailsQuery(sessionId.conference, sessionId.sessionId)
            .toUiState {
                SessionDetailsUiState(
                    it.session.sessionDetails,
                    it.config.timezone.toTimeZone()
                )
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), QueryResult.Loading)
}