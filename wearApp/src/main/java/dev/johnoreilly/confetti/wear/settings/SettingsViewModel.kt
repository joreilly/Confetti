@file:OptIn(ExperimentalHorologistApi::class)

package dev.johnoreilly.confetti.wear.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.horologist.annotations.ExperimentalHorologistApi
import dev.johnoreilly.confetti.ConfettiRepository
import dev.johnoreilly.confetti.wear.data.auth.GoogleSignInAuthUserRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class SettingsViewModel(
    userRepository: GoogleSignInAuthUserRepository,
    private val repository: ConfettiRepository
) : ViewModel() {
    val uiState: StateFlow<SettingsUiState> =
        userRepository.authState.map {
            SettingsUiState.Success(it)
        }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SettingsUiState.Loading)

}