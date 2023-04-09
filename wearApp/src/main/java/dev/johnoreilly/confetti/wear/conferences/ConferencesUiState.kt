package dev.johnoreilly.confetti.wear.conferences

import dev.johnoreilly.confetti.GetConferencesQuery

data class ConferencesUiState(
    val conferences: List<GetConferencesQuery.Conference>
)