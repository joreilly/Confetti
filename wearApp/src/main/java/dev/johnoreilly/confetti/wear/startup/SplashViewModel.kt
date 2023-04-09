@file:OptIn(ExperimentalCoroutinesApi::class)

package dev.johnoreilly.confetti.wear.startup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.johnoreilly.confetti.ConfettiRepository
import dev.johnoreilly.confetti.auth.Authentication
import dev.johnoreilly.confetti.wear.bookmarks.BookmarksUiState
import dev.johnoreilly.confetti.wear.home.HomeUiState
import dev.johnoreilly.confetti.wear.settings.PhoneSettingsSync
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn

class SplashViewModel(
    private val repository: ConfettiRepository,
    phoneSettingsSync: PhoneSettingsSync,
    authentication: Authentication,
) : ViewModel() {
    val conferenceFlow = phoneSettingsSync.conferenceFlow
        .stateIn(
            viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = null
        )

    init {
        repository.getConferenceFlow()
    }

    val uiState: StateFlow<HomeUiState> = conferenceFlow.flatMapLatest { conference ->
        if (conference == null) {
            flowOf(HomeUiState.Loading)
        } else {
            conferenceDataFlow(conference, repository)
        }
    }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), HomeUiState.Loading)

    val bookmarksUiState: StateFlow<BookmarksUiState> = conferenceFlow.flatMapLatest { conference ->
        if (conference == null) {
            flowOf(BookmarksUiState.Loading)
        } else {
            bookmarksUiStateFlow(conference, authentication, repository)
        }
    }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), BookmarksUiState.Loading)
}

