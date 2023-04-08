package dev.johnoreilly.confetti.wear

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.johnoreilly.confetti.ConfettiRepository
import dev.johnoreilly.confetti.auth.Authentication
import dev.johnoreilly.confetti.wear.settings.PhoneSettingsSync
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

class WearAppViewModel(
    val repository: ConfettiRepository,
    phoneSettingsSync: PhoneSettingsSync,
    authentication: Authentication
) : ViewModel() {

    val appState = combine(
        phoneSettingsSync.settingsFlow,
        repository.getConferenceFlow(),
        authentication.currentUser
    ) { phoneSettings, wearConference, user ->
        val defaultConference = phoneSettings.conference.ifBlank { wearConference }

        AppUiState(defaultConference, phoneSettings, user)
    }
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)
}

