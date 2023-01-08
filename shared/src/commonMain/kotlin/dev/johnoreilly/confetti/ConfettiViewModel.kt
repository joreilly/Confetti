package dev.johnoreilly.confetti

import dev.johnoreilly.confetti.fragment.SessionDetails
import com.rickclephas.kmm.viewmodel.*
import com.rickclephas.kmp.nativecoroutines.NativeCoroutinesState
import dev.johnoreilly.confetti.utils.DateTimeFormatter
import kotlinx.coroutines.flow.*
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.TimeZone.Companion.currentSystemDefault

expect val dateTimeFormatter: DateTimeFormatter

class ConfettiViewModel: KMMViewModel() {
    private val repository = ConfettiRepository()

    @NativeCoroutinesState
    val uiState: StateFlow<SessionsUiState> = repository.sessionsByDateMap.map {
        val confDates = it.keys.toList().sorted()

        val sessionsByStartTimeList = mutableListOf<Map<String, List<SessionDetails>>>()
        confDates.forEach { confDate ->
            val sessions = it[confDate] ?: emptyList()
            val sessionsByStartTime = sessions.groupBy { getSessionTime(it) }
            sessionsByStartTimeList.add(sessionsByStartTime)
        }

        SessionsUiState.Success(repository.conferenceName, confDates, sessionsByStartTimeList)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), SessionsUiState.Loading)

    private fun getSessionTime(session: SessionDetails): String {
        return dateTimeFormatter.format(session.startInstant, currentSystemDefault(), "HH:mm")
    }
}

sealed interface SessionsUiState {
    object Loading : SessionsUiState

    data class Success(
        val conferenceName: String,
        val confDates: List<LocalDate>,
        val sessionsByStartTimeList: List<Map<String, List<SessionDetails>>>
    ) : SessionsUiState
}
