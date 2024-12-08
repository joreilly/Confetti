package dev.johnoreilly.confetti.wear.settings

import dev.johnoreilly.confetti.auth.User
import dev.johnoreilly.confetti.wear.proto.WearPreferences
import dev.johnoreilly.confetti.wear.proto.WearSettings

sealed interface SettingsUiState {
    object Loading : SettingsUiState

    data class Success(
        val developerMode: Boolean = false,
        val authUser: User? = null,
        val phoneSettings: WearSettings? = null,
        val wearPreferences: WearPreferences? = null
    ) : SettingsUiState
}