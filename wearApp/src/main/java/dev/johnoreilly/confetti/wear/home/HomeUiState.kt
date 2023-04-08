package dev.johnoreilly.confetti.wear.home

import dev.johnoreilly.confetti.fragment.SessionDetails
import kotlinx.datetime.LocalDate

sealed interface HomeUiState {
    data class Error(val message: String) : HomeUiState
    object Loading : HomeUiState
    object NoConference : HomeUiState

    data class Success(
        val conference: String,
        val conferenceName: String,
        val confDates: List<LocalDate>
    ) : HomeUiState
}