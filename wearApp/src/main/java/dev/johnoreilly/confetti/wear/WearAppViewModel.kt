package dev.johnoreilly.confetti.wear

import com.rickclephas.kmm.viewmodel.coroutineScope
import dev.johnoreilly.confetti.AppViewModel
import dev.johnoreilly.confetti.wear.settings.PhoneSettingsSync
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import org.koin.core.component.get

class WearAppViewModel: AppViewModel() {
    val phoneSettingsSync: PhoneSettingsSync = get()

    val appState = phoneSettingsSync.settingsFlow
        .stateIn(viewModelScope.coroutineScope, SharingStarted.Eagerly, WearSettings())
}