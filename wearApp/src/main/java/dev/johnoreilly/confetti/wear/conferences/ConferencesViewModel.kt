package dev.johnoreilly.confetti.wear.conferences

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.johnoreilly.confetti.ConfettiRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ConferencesViewModel(
    private val repository: ConfettiRepository,
) : ViewModel() {
    val conferenceList = repository.conferenceList.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(),
        emptyList()
    )

    fun setConference(conference: String) {
        viewModelScope.launch {
            repository.setConference(conference)
        }
    }
}