package dev.johnoreilly.confetti.wear.home

import dev.johnoreilly.confetti.fragment.SessionDetails
import kotlinx.datetime.LocalDate

sealed interface HomeUiState {
    object NoneSelected : HomeUiState
    object Error : HomeUiState
    object Loading : HomeUiState

    data class Success(
        val conference: String,
        val conferenceName: String,
        val confDates: List<LocalDate>,
        val currentSessions: List<SessionDetails>,
    ) : HomeUiState
}