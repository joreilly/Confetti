package dev.johnoreilly.confetti.sessiondetails

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.apollographql.apollo3.cache.normalized.FetchPolicy
import dev.johnoreilly.confetti.ConfettiRepository
import dev.johnoreilly.confetti.fragment.SessionDetails
import dev.johnoreilly.confetti.sessiondetails.navigation.SessionDetailsDestination
import kotlinx.coroutines.flow.*

class SessionDetailsViewModel(
    savedStateHandle: SavedStateHandle,
    repository: ConfettiRepository
) : ViewModel() {

    private val sessionId: String? = savedStateHandle[SessionDetailsDestination.sessionIdArg]
    private val conference: String? = savedStateHandle[SessionDetailsDestination.conferenceArg]

    val session: StateFlow<SessionDetails?> = flow {
        if (sessionId != null && conference != null) {
            val response = repository.sessionDetails(conference = conference, sessionId = sessionId)
            emit(response.data?.session?.sessionDetails)
        }
    }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
}