package dev.johnoreilly.confetti.wear.sessiondetails

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.johnoreilly.confetti.ConfettiRepository
import dev.johnoreilly.confetti.analytics.AnalyticsLogger
import dev.johnoreilly.confetti.fragment.SessionDetails
import dev.johnoreilly.confetti.sessionsList
import dev.johnoreilly.confetti.utils.DateService
import dev.johnoreilly.confetti.wear.sessiondetails.navigation.SessionDetailsDestination
import dev.johnoreilly.confetti.wear.sessiondetails.navigation.SessionDetailsKey
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone

class SessionDetailsViewModel(
    savedStateHandle: SavedStateHandle,
    repository: ConfettiRepository,
    val formatter: DateService
) : ViewModel() {
    private val sessionId: SessionDetailsKey =
        SessionDetailsDestination.fromNavArgs(savedStateHandle)

    val session: StateFlow<Pair<SessionDetails, TimeZone>?> = repository.conferenceDataFlow(sessionId.conference)
        .map { confData ->
            confData?.sessionsList?.first { it.id == sessionId.sessionId }?.let {
            Pair(it, TimeZone.of(confData.config.timezone))
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
}