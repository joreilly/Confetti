package dev.johnoreilly.confetti

import com.rickclephas.kmm.viewmodel.KMMViewModel
import com.rickclephas.kmm.viewmodel.stateIn
import dev.johnoreilly.confetti.fragment.SessionDetails
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flow

class SessionDetailsViewModel(
    repository: ConfettiRepository
) : KMMViewModel() {
    fun configure(conference: String, sessionId: String) {
        this.conference = conference
        this.sessionId = sessionId
    }

    private lateinit var sessionId: String
    private lateinit var conference: String

    val session: StateFlow<SessionDetails?> = flow {
        val response = repository.sessionDetails(conference = conference, sessionId = sessionId)
        emit(response.data?.session?.sessionDetails)
    }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
}
