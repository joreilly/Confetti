package dev.johnoreilly.confetti.wear.conferences

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import dev.johnoreilly.confetti.ConfettiRepository
import dev.johnoreilly.confetti.work.RefreshWorker
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ConferencesViewModel(
    private val repository: ConfettiRepository,
    private val workManager: WorkManager
) : ViewModel() {
    val conferenceList: StateFlow<ConferencesUiState> = repository.conferenceList.map {
        ConferencesUiState.Success(it)
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(),
        ConferencesUiState.Loading
    )

    fun setConference(conference: String) {
        viewModelScope.launch {
            // refreshed
            repository.setConference(conference, refresh = false)
        }

        workManager.enqueueUniqueWork(
            RefreshWorker.WorkRefresh(conference),
            ExistingWorkPolicy.KEEP,
            RefreshWorker.oneOff(conference)
        )
    }
}