package dev.johnoreilly.confetti

import dev.johnoreilly.confetti.fragment.SessionDetails
import com.rickclephas.kmm.viewmodel.*
import com.rickclephas.kmp.nativecoroutines.NativeCoroutinesState
import dev.johnoreilly.confetti.utils.DateTimeFormatter
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
    val uiState: StateFlow<SessionsUiState> = repository.sessionsByDateMap.map { sessionsByDateMap ->

        val confDates = sessionsByDateMap.keys.toList().sorted()
        val sessionsByStartTimeList = groupSessionsByStartTime(confDates, sessionsByDateMap)
        SessionsUiState.Success(repository.conferenceName, confDates, sessionsByStartTimeList)

    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), SessionsUiState.Loading)

    private fun groupSessionsByStartTime(confDates: List<LocalDate>, sessionsByDateMap: Map<LocalDate, List<SessionDetails>>): MutableList<Map<String, List<SessionDetails>>> {
        val sessionsByStartTimeList = mutableListOf<Map<String, List<SessionDetails>>>()
        confDates.forEach { confDate ->
            val sessions = sessionsByDateMap[confDate] ?: emptyList()
            val sessionsByStartTime = sessions.groupBy {
                dateTimeFormatter.format(it.startInstant, currentSystemDefault(), "HH:mm")
            }
            sessionsByStartTimeList.add(sessionsByStartTime)
        }
        return sessionsByStartTimeList
    }

}