@file:OptIn(ExperimentalHorologistApi::class)

package dev.johnoreilly.confetti.wear.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.horologist.annotations.ExperimentalHorologistApi
import dev.johnoreilly.confetti.wear.data.auth.FirebaseAuthUserRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class SettingsViewModel(
    userRepository: FirebaseAuthUserRepository
) : ViewModel() {
    val uiState: StateFlow<SettingsUiState> =
        userRepository.localAuthState.map {
            SettingsUiState.Success(it)
        }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SettingsUiState.Loading)

}