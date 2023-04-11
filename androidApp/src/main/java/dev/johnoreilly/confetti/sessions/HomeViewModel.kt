package dev.johnoreilly.confetti.sessions

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.johnoreilly.confetti.ConfettiRepository
import dev.johnoreilly.confetti.sessions.navigation.SessionsKey
import dev.johnoreilly.confetti.splash.SplashReadyStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.WhileSubscribed
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlin.time.Duration.Companion.seconds

class HomeViewModel(
    savedStateHandle: SavedStateHandle,
    confettiRepository: ConfettiRepository,
    splashReadyStatus: SplashReadyStatus,
) : ViewModel() {
    private val conferenceParam: String? =
        SessionsKey.conferenceFromNavArgs(savedStateHandle)?.ifEmpty { null }

    private val conferenceFlow: Flow<String> = flow {
        if (conferenceParam != null) {
            // Set the conference selected if we've deep linked into a specific conference
            confettiRepository.setConference(conferenceParam)
            emit(conferenceParam)
        }
        emitAll(confettiRepository.getConferenceFlow())
    }

    val uiState: StateFlow<HomeUiState> = conferenceFlow
        .map { conference ->
            if (conference.isNotBlank()) {
                HomeUiState.Conference(conference)
            } else {
                HomeUiState.NoConference
            }
        }
        .onEach { uiState ->
            if (uiState is HomeUiState.Conference) {
                splashReadyStatus.reportIsReady()
            }
        }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5.seconds),
            HomeUiState.Loading
        )
}

sealed interface HomeUiState {
    object Loading : HomeUiState
    data class Conference(val conference: String) : HomeUiState
    object NoConference : HomeUiState
}