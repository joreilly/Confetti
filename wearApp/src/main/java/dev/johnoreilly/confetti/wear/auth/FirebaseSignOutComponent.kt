package dev.johnoreilly.confetti.wear.auth

import com.arkivanov.decompose.ComponentContext
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.koin.core.component.KoinComponent

interface FirebaseSignOutComponent {
    val uiState: StateFlow<GoogleSignOutScreenState>

    fun onIdleStateObserved()
    fun signedOut()
    fun navigateUp()
}

class DefaultFirebaseSignOutComponent(
    componentContext: ComponentContext,
    private val onSignedOut: () -> Unit,
    private val onNavigateUp: () -> Unit,
) : FirebaseSignOutComponent, KoinComponent, ComponentContext by componentContext {

    private val _uiState = MutableStateFlow(GoogleSignOutScreenState.Idle)
    override val uiState: StateFlow<GoogleSignOutScreenState> = _uiState

    override fun onIdleStateObserved() {
        try {
            Firebase.auth.signOut()
            _uiState.value = GoogleSignOutScreenState.Success
        } catch (apiException: ApiException) {
            _uiState.value = GoogleSignOutScreenState.Failed
        }
    }

    override fun signedOut() {
        onSignedOut()
    }

    override fun navigateUp() {
        onNavigateUp()
    }
}
