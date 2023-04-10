@file:OptIn(ExperimentalHorologistApi::class)

package dev.johnoreilly.confetti.wear.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.horologist.annotations.ExperimentalHorologistApi
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import dev.johnoreilly.confetti.AppSettings
import dev.johnoreilly.confetti.wear.data.auth.FirebaseAuthUserRepository
import dev.johnoreilly.confetti.wear.data.auth.FirebaseUserMapper
import dev.johnoreilly.confetti.work.WorkManagerConferenceRefresh
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class SettingsViewModel(
    userRepository: FirebaseAuthUserRepository,
    val appSettings: AppSettings,
    private val phoneSettingsSync: PhoneSettingsSync,
    val workManagerConferenceRefresh: WorkManagerConferenceRefresh
) : ViewModel() {
    val uiState: StateFlow<SettingsUiState> =
        combine(
            userRepository.firebaseAuthFlow,
            appSettings.developerModeFlow(),
        ) { firebaseUser, developerMode ->
            val authUser = FirebaseUserMapper.map(firebaseUser)

            if (developerMode) {
                val token = firebaseUser?.getIdToken(false)?.await()
                SettingsUiState.Success(
                    developerMode = developerMode,
                    authUser = authUser,
                    firebaseUser = firebaseUser,
                    token = token
                )
            } else {
                SettingsUiState.Success(developerMode = developerMode, authUser = authUser)
            }
        }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SettingsUiState.Loading)

    private fun conferenceIdFlow(): Flow<String> = phoneSettingsSync.conferenceFlow

    fun refresh() {
        viewModelScope.launch {
            val conference = conferenceIdFlow().first()

            if (conference.isNotEmpty()) {
                workManagerConferenceRefresh.refresh(conference)
            }
        }
    }

    fun enableDeveloperMode() {
        viewModelScope.launch {
            appSettings.setDeveloperMode(true)
        }
    }

    fun refreshToken() {
        viewModelScope.launch {
            Firebase.auth.currentUser?.getIdToken(true)?.await()
        }
    }
}