package dev.johnoreilly.confetti.wear.settings

import com.arkivanov.decompose.ComponentContext
import com.google.firebase.auth.auth
import com.google.firebase.Firebase
import dev.johnoreilly.confetti.AppSettings
import dev.johnoreilly.confetti.decompose.coroutineScope
import dev.johnoreilly.confetti.wear.data.auth.FirebaseAuthUserRepository
import dev.johnoreilly.confetti.wear.data.auth.FirebaseUserMapper
import dev.johnoreilly.confetti.wear.proto.NetworkPreferences
import dev.johnoreilly.confetti.wear.proto.WearPreferences
import dev.johnoreilly.confetti.work.WorkManagerConferenceRefresh
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

interface SettingsComponent {
    val uiState: StateFlow<SettingsUiState>

    fun enableDeveloperMode()
    fun refreshToken()
    fun refresh()
    fun onSwitchConferenceSelected()
    fun navigateToGoogleSignIn()
    fun navigateToGoogleSignOut()
    fun updatePreferences(wearPreferences: WearPreferences)
}

class DefaultSettingsComponent(
    componentContext: ComponentContext,
    val onNavigateToGoogleSignIn: () -> Unit,
    val onNavigateToGoogleSignOut: () -> Unit,
    val onNavigateToConferences: () -> Unit,
) : SettingsComponent, KoinComponent, ComponentContext by componentContext {
    private val userRepository: FirebaseAuthUserRepository by inject()
    private val appSettings: AppSettings by inject()
    private val phoneSettingsSync: PhoneSettingsSync by inject()
    private val workManagerConferenceRefresh: WorkManagerConferenceRefresh by inject()
    private val wearPreferencesStore: WearPreferencesStore by inject()

    private val coroutineScope = coroutineScope()

    override val uiState: StateFlow<SettingsUiState> =
        combine(
            userRepository.firebaseAuthFlow,
            appSettings.developerModeFlow(),
            wearPreferencesStore.preferences
        ) { firebaseUser, developerMode, preferences ->
            val authUser = FirebaseUserMapper.map(firebaseUser)

            if (developerMode) {
                val token = firebaseUser?.getIdToken(false)?.await()
                SettingsUiState.Success(
                    developerMode = true,
                    authUser = authUser,
                    firebaseUser = firebaseUser,
                    token = token,
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

    private fun conferenceIdFlow(): Flow<String> = phoneSettingsSync.conferenceFlow

    override fun refresh() {
        coroutineScope.launch {
            val conference = conferenceIdFlow().first()

            if (conference.isNotEmpty()) {
                workManagerConferenceRefresh.refresh(conference, fetchImages = true)
            }
        }
    }

    override fun enableDeveloperMode() {
        coroutineScope.launch {
            appSettings.setDeveloperMode(true)
        }
    }

    override fun refreshToken() {
        coroutineScope.launch {
            Firebase.auth.currentUser?.getIdToken(true)?.await()
        }
    }

    override fun onSwitchConferenceSelected() {
        onNavigateToConferences()
    }

    override fun navigateToGoogleSignIn() {
        onNavigateToGoogleSignIn()
    }

    override fun navigateToGoogleSignOut() {
        onNavigateToGoogleSignOut()
    }

    override fun updatePreferences(wearPreferences: WearPreferences) {
        coroutineScope.launch {
            wearPreferencesStore.dataStore.updateData {
                wearPreferences
            }
        }
    }
}