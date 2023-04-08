package dev.johnoreilly.confetti.wear

import dev.johnoreilly.confetti.auth.User
import dev.johnoreilly.confetti.wear.proto.WearSettings

data class AppUiState(
    val defaultConference: String = "",
    val settings: WearSettings = WearSettings(),
    val user: User? = null
)
