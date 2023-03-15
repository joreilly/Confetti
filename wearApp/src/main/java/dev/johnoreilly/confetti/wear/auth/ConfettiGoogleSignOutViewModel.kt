@file:OptIn(ExperimentalHorologistAuthDataApi::class)

package dev.johnoreilly.confetti.wear.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.common.api.ApiException
import com.google.android.horologist.auth.data.ExperimentalHorologistAuthDataApi
import dev.johnoreilly.confetti.wear.data.auth.GoogleSignInAuthUserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ConfettiGoogleSignOutViewModel(
    private val googleSignInAuthUserRepository: GoogleSignInAuthUserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(GoogleSignOutScreenState.Idle)
    public val uiState: StateFlow<GoogleSignOutScreenState> = _uiState

    fun onIdleStateObserved() {
        if (_uiState.compareAndSet(
                expect = GoogleSignOutScreenState.Idle,
                update = GoogleSignOutScreenState.Loading
            )
        ) {
            viewModelScope.launch {
                try {
                    googleSignInAuthUserRepository.signOut()
                    _uiState.value = GoogleSignOutScreenState.Success
                } catch (apiException: ApiException) {
                    _uiState.value = GoogleSignOutScreenState.Failed
                }
            }
        }
    }
}

enum class GoogleSignOutScreenState {
    Idle,
    Loading,
    Success,
    Failed
}
