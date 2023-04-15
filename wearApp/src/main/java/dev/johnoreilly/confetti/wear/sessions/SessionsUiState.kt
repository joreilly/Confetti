package dev.johnoreilly.confetti.wear.sessions

import dev.johnoreilly.confetti.fragment.SessionDetails
import dev.johnoreilly.confetti.navigation.ConferenceDayKey
import kotlinx.datetime.LocalDateTime

data class SessionsUiState(
    val conferenceDay: ConferenceDayKey,
    val sessionsByTime: List<SessionAtTime>,
    val now: LocalDateTime
)

data class SessionAtTime(
    val time: LocalDateTime,
    val sessions: List<SessionDetails>
)