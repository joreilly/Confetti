package dev.johnoreilly.confetti.wear

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.johnoreilly.confetti.ConfettiRepository
import dev.johnoreilly.confetti.fragment.RoomDetails
import dev.johnoreilly.confetti.fragment.SessionDetails
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.datetime.LocalDate
import kotlinx.datetime.toKotlinLocalDate

class ConfettiViewModel(private val repository: ConfettiRepository): ViewModel() {
    val conferenceList = repository.conferenceList.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())
    val rooms = repository.rooms

    val uiState: StateFlow<SessionsUiState> =
        combine(
            repository.conferenceName,
            repository.sessionsMap,
            repository.rooms
        ) { conferenceName, sessionsMap, rooms ->
            val confDates = sessionsMap.keys.toList().sorted()

            val sessionsByStartTimeList = mutableListOf<Map<String, List<SessionDetails>>>()
            confDates.forEach { confDate ->
                val sessions = sessionsMap[confDate] ?: emptyList()
                val sessionsByStartTime = sessions.groupBy { repository.getSessionTime(it) }
                sessionsByStartTimeList.add(sessionsByStartTime)
            }
            SessionsUiState.Success(conferenceName, confDates, sessionsByStartTimeList, rooms)

        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SessionsUiState.Loading)


    fun setConference(conference: String) {
        repository.setConference(conference)
    }

    fun clearConference() {
        setConference("")
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
        val sessionsByStartTimeList: List<Map<String, List<SessionDetails>>>,
        val rooms: List<RoomDetails>
    ) : SessionsUiState {
        fun currentSessions(): List<Pair<String, List<SessionDetails>>>? {
            val today = java.time.LocalDate.now().toKotlinLocalDate()

            val indexInDays = confDates.indexOf(today)
            return if (indexInDays != -1) {
                // TODO filter the right session times
                sessionsByStartTimeList[indexInDays].entries.take(2).map {
                    it.toPair()
                }
            } else  {
                null
            }
        }

        val today: LocalDate?
            get() = java.time.LocalDate.now().toKotlinLocalDate().let {
                if (confDates.contains(it)) {
                    it
                } else  {
                    null
                }
            }
    }
}
