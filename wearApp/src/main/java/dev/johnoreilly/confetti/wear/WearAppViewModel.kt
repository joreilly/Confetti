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

        AppUiState(
            defaultConference = defaultConference,
            settings = phoneSettings,
            user = user
        )
    }
        .stateIn(
            viewModelScope,
            SharingStarted.Eagerly,
            AppUiState(user = authentication.currentUser.value)
        )
}

