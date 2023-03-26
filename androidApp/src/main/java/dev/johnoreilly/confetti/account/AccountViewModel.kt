package dev.johnoreilly.confetti.account

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.horologist.data.apphelper.AppHelperNodeStatus
import dev.johnoreilly.confetti.ApolloClientCache
import dev.johnoreilly.confetti.wear.WearSettingsSync
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class AccountViewModel(
    val authentication: Authentication,
    val apolloClientCache: ApolloClientCache,
    val wearSettingsSync: WearSettingsSync
) : ViewModel() {

    val uiState: StateFlow<UiState> =
        combine(authentication.currentUserFlow, wearSettingsSync.wearNodes) { user, wearNodes ->
            UiState(user, wearNodes)
        }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UiState())

    init {
        authentication
        viewModelScope.launch {
            val idToken = authentication.idToken(true)
            wearSettingsSync.updateIdToken(idToken)
        }
    }

    fun signOut() {
        apolloClientCache.clear()
        authentication.signOut()
    }

    fun installOnWear() {
        viewModelScope.launch {
            uiState.value.wearNodes.filter { !it.isAppInstalled }.forEach {
                wearSettingsSync.installOnWearNode(it.id)
            }
        }
    }

    fun updateWearTheme() {
        viewModelScope.launch {
            wearSettingsSync.updateWearTheme()
        }
    }
}

data class UiState(
    val user: User? = null,
    val wearNodes: List<AppHelperNodeStatus> = listOf()
) {
    val isInstalledOnWear: Boolean
        get() = wearNodes.find { it.isAppInstalled } != null
    val showInstallOnWear: Boolean
        get() = wearNodes.isNotEmpty() && wearNodes.find { !it.isAppInstalled } != null
}