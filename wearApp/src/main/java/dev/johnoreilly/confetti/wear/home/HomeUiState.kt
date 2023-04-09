package dev.johnoreilly.confetti.wear.home

import kotlinx.datetime.LocalDate


data class HomeUiState(
    val conference: String,
    val conferenceName: String,
    val confDates: List<LocalDate>
)
