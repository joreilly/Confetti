package dev.johnoreilly.confetti.account

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.horologist.data.apphelper.AppHelperNodeStatus
import dev.johnoreilly.confetti.ApolloClientCache
import dev.johnoreilly.confetti.auth.Authentication
import dev.johnoreilly.confetti.auth.User
import dev.johnoreilly.confetti.wear.WearSettingsSync
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class AccountViewModel(
    val wearSettingsSync: WearSettingsSync
) : ViewModel() {

    val uiState: StateFlow<UiState> = wearSettingsSync.wearNodes.map { UiState(it) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UiState())

    fun installOnWear() {
        viewModelScope.launch {
            uiState.value.wearNodes.filter { !it.isAppInstalled }.forEach {
                wearSettingsSync.installOnWearNode(it.id)
            }
        }
    }
}

data class UiState(
    val wearNodes: List<AppHelperNodeStatus> = listOf()
) {
    val isInstalledOnWear: Boolean
        get() = wearNodes.find { it.isAppInstalled } != null
    val showInstallOnWear: Boolean
        get() = wearNodes.isNotEmpty() && wearNodes.find { !it.isAppInstalled } != null
}