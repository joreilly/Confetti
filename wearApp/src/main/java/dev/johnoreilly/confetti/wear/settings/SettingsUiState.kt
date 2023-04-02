@file:OptIn(ExperimentalHorologistApi::class)

package dev.johnoreilly.confetti.wear.settings

import com.google.android.horologist.annotations.ExperimentalHorologistApi
import com.google.android.horologist.auth.data.common.model.AuthUser
import dev.johnoreilly.confetti.wear.data.auth.AuthAndSource

sealed interface SettingsUiState {
    object Loading : SettingsUiState

    data class Success(
        val conference: String?,
        val authUser: AuthUser?,
        val source: SettingsSource?,
    ) : SettingsUiState
}