package dev.johnoreilly.confetti.wear.tile

import dev.johnoreilly.confetti.fragment.SessionDetails

data class CurrentSessionsData(
    val sessionTime: kotlinx.datetime.LocalDateTime?,
    val sessions: List<SessionDetails>
)