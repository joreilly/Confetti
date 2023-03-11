package dev.johnoreilly.confetti.wear.conferences

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.OneTimeWorkRequestBuilder
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
) : ViewModel() {
    val conferenceList: StateFlow<ConferencesUiState> = repository.conferenceList.map {
        ConferencesUiState.Success(it)
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(),
        ConferencesUiState.Loading
    )

    fun setConference(conference: String) {
        OneTimeWorkRequestBuilder<RefreshWorker>()
            .setInputData(
                workDataOf(
                    RefreshWorker.ConferenceKey to conference
                )
            )
            .build()

        viewModelScope.launch {
            repository.setConference(conference)
        }
    }
}