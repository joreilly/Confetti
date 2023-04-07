@file:OptIn(ExperimentalHorologistApi::class)

package dev.johnoreilly.confetti.wear.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.ExistingWorkPolicy
import androidx.work.WorkManager
import com.google.android.horologist.annotations.ExperimentalHorologistApi
import dev.johnoreilly.confetti.wear.data.auth.FirebaseAuthUserRepository
import dev.johnoreilly.confetti.work.RefreshWorker
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
    userRepository: FirebaseAuthUserRepository,
    val workManager: WorkManager,
    private val phoneSettingsSync: PhoneSettingsSync
) : ViewModel() {
    val uiState: StateFlow<SettingsUiState> =
        userRepository.localAuthState.map {
            SettingsUiState.Success(it)
        }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SettingsUiState.Loading)

    private fun conferenceIdFlow(): Flow<String> = phoneSettingsSync.conferenceFlow

    fun refresh() {
        viewModelScope.launch {
            val conference = conferenceIdFlow().first()

            if (conference.isNotEmpty()) {
                workManager.enqueueUniqueWork(
                    RefreshWorker.WorkRefresh(conference),
                    ExistingWorkPolicy.KEEP,
                    RefreshWorker.oneOff(conference)
                )
            }
        }
    }
}