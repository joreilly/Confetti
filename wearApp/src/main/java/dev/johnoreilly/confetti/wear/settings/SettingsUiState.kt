package dev.johnoreilly.confetti.wear.settings

import com.google.android.horologist.auth.data.common.model.AuthUser
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GetTokenResult

sealed interface SettingsUiState {
    object Loading : SettingsUiState

    data class Success(
        val authUser: AuthUser?,
        val developerMode: Boolean = false,
        val firebaseUser: FirebaseUser? = null,
        val token: GetTokenResult? = null
    ) : SettingsUiState
}