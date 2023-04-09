@file:OptIn(ExperimentalCoroutinesApi::class)

package dev.johnoreilly.confetti.wear.startup

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.johnoreilly.confetti.ConfettiRepository
import dev.johnoreilly.confetti.auth.Authentication
import dev.johnoreilly.confetti.utils.QueryResult
import dev.johnoreilly.confetti.wear.bookmarks.BookmarksUiState
import dev.johnoreilly.confetti.wear.home.HomeUiState
import dev.johnoreilly.confetti.wear.home.HomeViewModel
import dev.johnoreilly.confetti.wear.home.HomeViewModel.Companion.homeUiStateFlow
import dev.johnoreilly.confetti.wear.settings.PhoneSettingsSync
import dev.johnoreilly.confetti.wear.startup.navigation.StartHomeDestination
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn

class StartViewModel(
    savedStateHandle: SavedStateHandle,
    private val repository: ConfettiRepository,
    phoneSettingsSync: PhoneSettingsSync,
    authentication: Authentication,
) : ViewModel() {
    val conferenceParam: String? =
        StartHomeDestination.fromNavArgs(savedStateHandle)?.ifEmpty { null }

    val conferenceFlow: Flow<String> = if (conferenceParam != null) {
        flowOf(conferenceParam)
    } else {
        phoneSettingsSync.conferenceFlow
    }

    val uiState: StateFlow<QueryResult<HomeUiState>> = conferenceFlow.flatMapLatest { conference ->
        if (conference.isNotBlank()) {
            homeUiStateFlow(repository, conference)
        } else {
            flowOf(QueryResult.None)
        }
    }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), QueryResult.Loading)

    val bookmarksUiState: StateFlow<QueryResult<BookmarksUiState>> =
        conferenceFlow.flatMapLatest { conference ->
            if (conference.isNotBlank()) {
                HomeViewModel.bookmarksUiStateFlow(authentication, repository, conference)
            } else {
                flowOf(QueryResult.None)
            }
        }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), QueryResult.Loading)
}

