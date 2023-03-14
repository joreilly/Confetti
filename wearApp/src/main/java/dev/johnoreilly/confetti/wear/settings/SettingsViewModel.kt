@file:OptIn(ExperimentalHorologistAuthDataApi::class)

package dev.johnoreilly.confetti.wear.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.horologist.auth.data.ExperimentalHorologistAuthDataApi
import dev.johnoreilly.confetti.ConfettiRepository
import dev.johnoreilly.confetti.wear.data.auth.GoogleSignInAuthUserRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
    userRepository: GoogleSignInAuthUserRepository,
    private val repository: ConfettiRepository
) : ViewModel() {
    fun clearConference() {
        viewModelScope.launch {
            repository.setConference("")
        }
    }

    val uiState: StateFlow<SettingsUiState> =
        combine(userRepository.authState, repository.getConferenceFlow()) { authUser, conference ->
            SettingsUiState.Success(authUser, conference)
        }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SettingsUiState.Loading)

}