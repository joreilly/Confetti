@file:OptIn(ExperimentalCoroutinesApi::class)

package dev.johnoreilly.confetti.wear.bookmarks

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.apollographql.apollo3.cache.normalized.FetchPolicy
import dev.johnoreilly.confetti.ConfettiRepository
import dev.johnoreilly.confetti.GetBookmarkedSessionsQuery
import dev.johnoreilly.confetti.auth.Authentication
import dev.johnoreilly.confetti.toTimeZone
import dev.johnoreilly.confetti.utils.ClientQuery.toUiState
import dev.johnoreilly.confetti.utils.QueryResult
import dev.johnoreilly.confetti.wear.bookmarks.navigation.BookmarksDestination
import dev.johnoreilly.confetti.wear.complication.ComplicationUpdater
import dev.johnoreilly.confetti.wear.tile.TileUpdater
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.stateIn
import kotlinx.datetime.toKotlinInstant
import kotlinx.datetime.toLocalDateTime
import java.time.Instant

class BookmarksViewModel(
    private val tileUpdater: TileUpdater,
    private val complicationUpdater: ComplicationUpdater,
    savedStateHandle: SavedStateHandle,
    repository: ConfettiRepository,
    authentication: Authentication,
) : ViewModel() {
    private val conference: String =
        BookmarksDestination.fromNavArgs(savedStateHandle)

    val uiState: StateFlow<QueryResult<BookmarksUiState>> =
        authentication.currentUser.flatMapLatest { user ->
            if (user == null) {
                flowOf(QueryResult.NotLoggedIn)
            } else {
                // Assume this is a good point to refresh
                val fetchPolicy = FetchPolicy.CacheAndNetwork
                repository.bookmarkedSessionsQuery(conference, user.uid, user, fetchPolicy)
                    .toUiState {
                        it.toUiState()
                    }.onCompletion {
                        tileUpdater.updateAll()
                        complicationUpdater.update()
                    }
            }
        }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), QueryResult.Loading)

    companion object {
        fun GetBookmarkedSessionsQuery.Data.toUiState(): BookmarksUiState {
            val allSessions =
                this.bookmarkConnection?.nodes
                    ?.map { it.sessionDetails }
                    ?.sortedBy { it.startsAt }
                    .orEmpty()

            val timeZone = this.config.timezone.toTimeZone()

            val now = Instant.now().toKotlinInstant().toLocalDateTime(timeZone)

            val (upcoming, past) = allSessions.partition {
                it.endsAt > now
            }

            return BookmarksUiState(this.config.id, upcoming, past)
        }
    }
}