package dev.johnoreilly.confetti.wear.settings

import com.google.android.horologist.auth.data.common.model.AuthUser
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GetTokenResult
import dev.johnoreilly.confetti.wear.proto.WearPreferences
import dev.johnoreilly.confetti.wear.proto.WearSettings

sealed interface SettingsUiState {
    object Loading : SettingsUiState

    data class Success(
        val authUser: AuthUser?,
        val developerMode: Boolean = false,
        val firebaseUser: FirebaseUser? = null,
        val token: GetTokenResult? = null,
        val phoneSettings: WearSettings? = null,
        val wearPreferences: WearPreferences? = null
    ) : SettingsUiState
}