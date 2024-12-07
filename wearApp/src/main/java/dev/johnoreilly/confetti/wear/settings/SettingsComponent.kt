package dev.johnoreilly.confetti.wear.settings

import com.arkivanov.decompose.ComponentContext
import dev.johnoreilly.confetti.AppSettings
import dev.johnoreilly.confetti.auth.Authentication
import dev.johnoreilly.confetti.decompose.coroutineScope
import dev.johnoreilly.confetti.wear.proto.WearPreferences
import dev.johnoreilly.confetti.work.WorkManagerConferenceRefresh
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

interface SettingsComponent {
    val uiState: StateFlow<SettingsUiState>

    fun enableDeveloperMode()
    fun refresh()
    fun signIn()
    fun signOut()
    fun onSwitchConferenceSelected()
    fun updatePreferences(wearPreferences: WearPreferences)
}

class DefaultSettingsComponent(
    componentContext: ComponentContext,
    val onNavigateToConferences: () -> Unit,
    private val onSignOut: () -> Unit,
    private val onSignIn: () -> Unit,
) : SettingsComponent, KoinComponent, ComponentContext by componentContext {
    private val appSettings: AppSettings by inject()
    private val phoneSettingsSync: PhoneSettingsSync by inject()
    private val workManagerConferenceRefresh: WorkManagerConferenceRefresh by inject()
    private val wearPreferencesStore: WearPreferencesStore by inject()
    private val authentication: Authentication by inject()

    private val coroutineScope = coroutineScope()

    override val uiState: StateFlow<SettingsUiState> =
        combine(
            authentication.currentUser,
            appSettings.developerModeFlow(),
            wearPreferencesStore.preferences
        ) { authUser, developerMode, preferences ->

            if (developerMode) {
                SettingsUiState.Success(
                    developerMode = true,
                    authUser = authUser,
                    wearPreferences = preferences
                )
            } else {
                SettingsUiState.Success(
                    developerMode = false,
                    authUser = authUser,
                    wearPreferences = preferences
                )
            }
        }
            .stateIn(coroutineScope, SharingStarted.WhileSubscribed(5000), SettingsUiState.Loading)

    private fun conferenceIdFlow(): Flow<String> = phoneSettingsSync.conferenceFlow.map { it.conference }

    override fun refresh() {
        coroutineScope.launch {
            val conference = conferenceIdFlow().first()

            if (conference.isNotEmpty()) {
                workManagerConferenceRefresh.refresh(conference, fetchImages = true)
            }
        }
    }

    override fun signIn() {
        onSignIn()
    }

    override fun signOut() {
        onSignOut()
    }

    override fun enableDeveloperMode() {
        coroutineScope.launch {
            appSettings.setDeveloperMode(true)
        }
    }

    override fun onSwitchConferenceSelected() {
        onNavigateToConferences()
    }

    override fun updatePreferences(wearPreferences: WearPreferences) {
        coroutineScope.launch {
            wearPreferencesStore.dataStore.updateData {
                wearPreferences
            }
        }
    }
}