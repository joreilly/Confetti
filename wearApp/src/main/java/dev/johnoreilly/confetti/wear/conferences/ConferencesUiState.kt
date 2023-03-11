package dev.johnoreilly.confetti.wear.conferences

import dev.johnoreilly.confetti.GetConferencesQuery

sealed interface ConferencesUiState {
    object Error : ConferencesUiState
    object Loading : ConferencesUiState

    data class Success(
        val conferences: List<GetConferencesQuery.Conference>
    ) : ConferencesUiState
}