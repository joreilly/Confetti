@file:OptIn(ExperimentalCoroutinesApi::class)

package dev.johnoreilly.confetti.wear.home

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.ExistingWorkPolicy
import androidx.work.WorkManager
import dev.johnoreilly.confetti.ConfettiRepository
import dev.johnoreilly.confetti.GetConferenceDataQuery
import dev.johnoreilly.confetti.fragment.SessionDetails
import dev.johnoreilly.confetti.wear.home.navigation.ConferenceHomeDestination
import dev.johnoreilly.confetti.work.RefreshWorker
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class HomeViewModel(
    savedStateHandle: SavedStateHandle,
    private val repository: ConfettiRepository,
    private val workManager: WorkManager
) : ViewModel() {

    private val conference: String =
        ConferenceHomeDestination.fromNavArgs(savedStateHandle)

    val uiState: StateFlow<HomeUiState> = conferenceDataFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), HomeUiState.Loading)

    fun refresh() {
        workManager.enqueueUniqueWork(
            RefreshWorker.WorkRefresh(conference),
            ExistingWorkPolicy.KEEP,
            RefreshWorker.oneOff(conference)
        )
    }

    private fun conferenceDataFlow(): Flow<HomeUiState> =
        conferenceIdFlow().flatMapLatest { actualConference ->
            if (actualConference.isEmpty()) {
                flowOf(HomeUiState.NoneSelected)
            } else {
                repository.conferenceHomeData(actualConference).toFlow().map {
                    val conferenceData = it.data

                    if (conferenceData == null) {
                        HomeUiState.Error
                    } else {
                        toUiState(conferenceData, actualConference)
                    }
                }
            }
        }

    private fun toUiState(
        conferenceData: GetConferenceDataQuery.Data,
        actualConference: String
    ): HomeUiState.Success {
        // TODO query todays sessions only
        val currentSessions = listOf<SessionDetails>()

        return HomeUiState.Success(
            actualConference,
            conferenceData.config.name,
            conferenceData.config.days,
            currentSessions = currentSessions,
        )
    }

    private fun conferenceIdFlow(): Flow<String> = if (conference.isEmpty()) {
        repository.getConferenceFlow()
    } else {
        flowOf(conference)
    }
}

