@file:OptIn(ExperimentalHorologistApi::class)

package dev.johnoreilly.confetti.wear.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.common.api.ApiException
import com.google.android.horologist.annotations.ExperimentalHorologistApi
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class FirebaseSignOutViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(GoogleSignOutScreenState.Idle)
    val uiState: StateFlow<GoogleSignOutScreenState> = _uiState

    fun onIdleStateObserved() {
        if (_uiState.compareAndSet(
                expect = GoogleSignOutScreenState.Idle,
                update = GoogleSignOutScreenState.Loading
            )
        ) {
            viewModelScope.launch {
                try {
                    Firebase.auth.signOut()
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
