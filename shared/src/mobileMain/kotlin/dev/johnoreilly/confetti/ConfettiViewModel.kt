package dev.johnoreilly.confetti

import dev.johnoreilly.confetti.fragment.SessionDetails
import com.rickclephas.kmm.viewmodel.*
import com.rickclephas.kmp.nativecoroutines.NativeCoroutinesState
import kotlinx.coroutines.flow.*
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone.Companion.currentSystemDefault

sealed interface SessionsUiState {
    object Loading : SessionsUiState

    data class Success(
        val conferenceName: String,
        val confDates: List<LocalDate>,
        val sessionsByStartTimeList: List<Map<String, List<SessionDetails>>>
    ) : SessionsUiState
}


class ConfettiViewModel: KMMViewModel() {
    private val repository = ConfettiRepository()

    @NativeCoroutinesState
    val uiState: StateFlow<SessionsUiState> = repository.sessions.map { sessions ->

        // Group sessions by date
        val sessionsByDateMap = sessions.groupBy { it.start.date }
        val confDates = sessionsByDateMap.keys.toList().sorted()

        // Create list for each date with sessions grouped by start time
        val sessionsByStartTime = sessionsByDateMap.map { (_, sessions) ->
            sessions.groupBy {
                dateTimeFormatter.format(it.start, currentSystemDefault(), "HH:mm")
            }
        }

        SessionsUiState.Success(repository.conferenceName, confDates, sessionsByStartTime)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), SessionsUiState.Loading)

}


