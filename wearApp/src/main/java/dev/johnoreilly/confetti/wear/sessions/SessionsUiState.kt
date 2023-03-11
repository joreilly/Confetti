package dev.johnoreilly.confetti.wear.sessions

import dev.johnoreilly.confetti.fragment.SessionDetails
import dev.johnoreilly.confetti.navigation.ConferenceDayKey
import kotlinx.datetime.LocalDateTime

sealed interface SessionsUiState {
    object Error : SessionsUiState
    object Loading : SessionsUiState

    data class Success(
        val conferenceDay: ConferenceDayKey,
        val sessionsByTime: List<SessionAtTime>
    ) : SessionsUiState

    data class SessionAtTime(
        val time: LocalDateTime,
        val sessions: List<SessionDetails>
    )
}