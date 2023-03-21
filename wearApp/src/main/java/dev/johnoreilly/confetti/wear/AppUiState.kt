package dev.johnoreilly.confetti.wear

import dev.johnoreilly.confetti.wear.proto.WearSettings

data class AppUiState(
    val conference: String = "",
    val settings: WearSettings = WearSettings()
)
