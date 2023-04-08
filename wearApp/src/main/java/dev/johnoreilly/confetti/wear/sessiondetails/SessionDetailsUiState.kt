package dev.johnoreilly.confetti.wear.sessiondetails

import dev.johnoreilly.confetti.fragment.SessionDetails
import dev.johnoreilly.confetti.navigation.SessionDetailsKey
import kotlinx.datetime.TimeZone

data class SessionDetailsUiState(
    val session: SessionDetails,
    val timeZone: TimeZone
)