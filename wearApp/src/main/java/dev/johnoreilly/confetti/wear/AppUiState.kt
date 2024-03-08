package dev.johnoreilly.confetti.wear

import androidx.compose.ui.graphics.Color
import dev.johnoreilly.confetti.auth.User
import dev.johnoreilly.confetti.wear.proto.WearPreferences

data class AppUiState(
    val defaultConference: String? = null,
    val seedColor: Color?,
    val user: User? = null,
    val wearPreferences: WearPreferences? = null
)
