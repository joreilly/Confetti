@file:OptIn(ExperimentalCoroutinesApi::class)

package dev.johnoreilly.confetti.wear.home

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.apollographql.apollo3.cache.normalized.FetchPolicy
import dev.johnoreilly.confetti.ConfettiRepository
import dev.johnoreilly.confetti.GetConferenceDataQuery
import dev.johnoreilly.confetti.auth.Authentication
import dev.johnoreilly.confetti.wear.bookmarks.BookmarksUiState
import dev.johnoreilly.confetti.wear.bookmarks.BookmarksViewModel.Companion.fetchBookmarkedSessions
import dev.johnoreilly.confetti.wear.home.navigation.ConferenceHomeDestination
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class HomeViewModel(
    savedStateHandle: SavedStateHandle,
    private val repository: ConfettiRepository,
    authentication: Authentication,
) : ViewModel() {
    private val conference: String =
        ConferenceHomeDestination.fromNavArgs(savedStateHandle)

    val uiState: StateFlow<HomeUiState> = conferenceDataFlow(conference)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), HomeUiState.Loading)

    val bookmarksUiState: StateFlow<BookmarksUiState> = authentication.currentUser.flatMapLatest { user ->
        if (user != null) {
            fetchBookmarkedSessions(repository, conference, user, FetchPolicy.CacheFirst)
        } else {
            flowOf(BookmarksUiState.NotLoggedIn)
        }
    }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), BookmarksUiState.Loading)

    private fun conferenceDataFlow(conference: String) =
        repository.conferenceHomeData(conference).toFlow().map {
            val conferenceData = it.data

            if (conferenceData != null) {
                toUiState(conferenceData, conference)
            } else if (it.hasErrors()) {
                HomeUiState.Error(it.errors.toString())
            } else {
                HomeUiState.Loading
            }
        }

    private fun toUiState(
        conferenceData: GetConferenceDataQuery.Data,
        actualConference: String
    ): HomeUiState.Success {
        return HomeUiState.Success(
            actualConference,
            conferenceData.config.name,
            conferenceData.config.days,
        )
    }
}

