package dev.johnoreilly.confetti.wear.conferences

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.WorkManager
import dev.johnoreilly.confetti.ConferenceRefresh
import dev.johnoreilly.confetti.ConfettiRepository
import dev.johnoreilly.confetti.utils.ClientQuery.toUiState
import dev.johnoreilly.confetti.utils.QueryResult
import dev.johnoreilly.confetti.wear.complication.ComplicationUpdater
import dev.johnoreilly.confetti.wear.tile.TileUpdater
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ConferencesViewModel(
    private val tileUpdater: TileUpdater,
    private val complicationUpdater: ComplicationUpdater,
    private val repository: ConfettiRepository,
    private val refresh: ConferenceRefresh
) : ViewModel() {
    val conferenceList: StateFlow<QueryResult<ConferencesUiState>> = repository.conferencesQuery()
        .toUiState {
            ConferencesUiState(it.conferences)
        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(),
            QueryResult.Loading
        )

    fun setConference(conference: String) {
        viewModelScope.launch {
            repository.setConference(conference)

            tileUpdater.updateAll()
            complicationUpdater.update()
        }

        refresh.refresh(conference)
    }
}