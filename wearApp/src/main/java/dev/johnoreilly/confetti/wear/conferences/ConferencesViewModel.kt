package dev.johnoreilly.confetti.wear.conferences

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.WorkManager
import com.apollographql.apollo3.cache.normalized.FetchPolicy
import dev.johnoreilly.confetti.ConferenceRefresh
import dev.johnoreilly.confetti.ConferencesViewModel
import dev.johnoreilly.confetti.ConfettiRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ConferencesViewModel(
    private val repository: ConfettiRepository,
    private val workManager: WorkManager,
    private val refresh: ConferenceRefresh
) : ViewModel() {
    val conferenceList: StateFlow<ConferencesUiState> = flow {
        var hasConferences = false
        repository.conferences(FetchPolicy.CacheFirst).data?.conferences?.let {
            hasConferences = true
            emit(ConferencesUiState.Success(it))
        }

        repository.conferences(FetchPolicy.NetworkOnly).data?.conferences?.let {
            hasConferences = true
            emit(ConferencesUiState.Success(it))
        }

        if (!hasConferences) {
            emit(ConferencesUiState.Error)
        }
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(),
        ConferencesUiState.Loading
    )


    fun setConference(conference: String) {
        viewModelScope.launch {
            repository.setConference(conference)
        }

        refresh.refresh(conference)
    }
}