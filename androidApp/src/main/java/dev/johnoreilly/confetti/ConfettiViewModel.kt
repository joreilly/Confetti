package dev.johnoreilly.confetti

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.johnoreilly.confetti.fragment.SessionDetails
import kotlinx.coroutines.flow.*
import kotlinx.datetime.LocalDate


class ConfettiViewModel(private val repository: ConfettiRepository): ViewModel() {
    val conferenceList = repository.conferenceList
    val speakers = repository.speakers
    val rooms = repository.rooms

    var selectedDateIndex = MutableStateFlow<Int>(0)

    val uiState: StateFlow<SessionsUiState> =
        combine(
            repository.conferenceName,
            repository.sessionsMap,
            selectedDateIndex
        ) { conferenceName, sessionsMap, selectedDateIndex ->
            val confDates = sessionsMap.keys.toList().sorted()
            val selectedDate = confDates[selectedDateIndex]
            val sessions = sessionsMap[selectedDate] ?: emptyList()
            val sessionsByStartTime = sessions.groupBy { repository.getSessionTime(it) }

            SessionsUiState.Success(conferenceName, confDates, selectedDateIndex, sessionsByStartTime)

        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SessionsUiState.Loading)


    fun setConference(conference: String) {
        repository.setConference(conference)
    }

    fun switchTab(newIndex: Int) {
        selectedDateIndex.value = newIndex
    }

    fun onLanguageChecked(language: String, checked: Boolean) {
        repository.updateEnableLanguageSetting(language, checked)
    }

    suspend fun getSession(sessionId: String): SessionDetails? {
        return repository.getSession(sessionId)
    }

    suspend fun refresh() {
        repository.refresh()
    }
}

sealed interface SessionsUiState {
    object Loading : SessionsUiState

    data class Success(
        val conferenceName: String,
        val confDates: List<LocalDate>,
        val selectedDateIndex: Int,
        val sessionsByStartTime: Map<String, List<SessionDetails>>
    ) : SessionsUiState
}
