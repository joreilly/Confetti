@file:OptIn(ExperimentalHorologistAuthDataApi::class)

package dev.johnoreilly.confetti.wear.settings

import com.google.android.horologist.auth.data.ExperimentalHorologistAuthDataApi
import com.google.android.horologist.auth.data.common.model.AuthUser

sealed interface SettingsUiState {
    object Loading : SettingsUiState

    data class Success(
        val authUser: AuthUser?,
        val conference: String
    ) : SettingsUiState
}