package dev.johnoreilly.confetti.sessiondetails

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.johnoreilly.confetti.ConfettiRepository
import dev.johnoreilly.confetti.fragment.SessionDetails
import dev.johnoreilly.confetti.sessiondetails.navigation.SessionDetailsDestination
import kotlinx.coroutines.flow.*


class SessionDetailsViewModel(
    savedStateHandle: SavedStateHandle,
    private val repository: ConfettiRepository
) : ViewModel() {

    private val sessionId: String? = savedStateHandle[SessionDetailsDestination.sessionIdArg]

    val session: StateFlow<SessionDetails?> = repository.sessions.map {
        it.first { it.id == sessionId }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
}