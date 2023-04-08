@file:OptIn(ExperimentalCoroutinesApi::class)

package dev.johnoreilly.confetti.wear.bookmarks

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.apollographql.apollo3.api.ApolloResponse
import com.apollographql.apollo3.cache.normalized.FetchPolicy
import dev.johnoreilly.confetti.ConfettiRepository
import dev.johnoreilly.confetti.GetBookmarkedSessionsQuery
import dev.johnoreilly.confetti.auth.Authentication
import dev.johnoreilly.confetti.auth.User
import dev.johnoreilly.confetti.toTimeZone
import dev.johnoreilly.confetti.wear.bookmarks.navigation.BookmarksDestination
import dev.johnoreilly.confetti.wear.complication.ComplicationUpdater
import dev.johnoreilly.confetti.wear.tile.TileUpdater
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
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

    val uiState: StateFlow<BookmarksUiState> = authentication.currentUser.flatMapLatest { user ->
        if (user == null) {
            flowOf(BookmarksUiState.NotLoggedIn)
        } else {
            // Assume this is a good point to refresh
            val fetchPolicy = FetchPolicy.CacheAndNetwork
            fetchBookmarkedSessions(repository, conference, user, fetchPolicy).onCompletion {
                tileUpdater.updateAll()
                complicationUpdater.update()
            }
        }
    }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), BookmarksUiState.Loading)

    companion object {
        suspend fun fetchBookmarkedSessions(
            repository: ConfettiRepository,
            conference: String,
            user: User,
            fetchPolicy: FetchPolicy
        ): Flow<BookmarksUiState> =
            repository.bookmarkedSessionsQuery(conference, user.uid, user, fetchPolicy)
                .toFlow()
                .map<ApolloResponse<GetBookmarkedSessionsQuery.Data>, BookmarksUiState> { bookmarks ->
                    val data = bookmarks.data

                    if (data != null) {
                        val allSessions =
                            data.bookmarkConnection?.nodes
                                ?.map { it.sessionDetails }
                                ?.sortedBy { it.startsAt }
                                .orEmpty()

                        val timeZone = data.config.timezone.toTimeZone()

                        val now = Instant.now().toKotlinInstant().toLocalDateTime(timeZone)

                        val (upcoming, past) = allSessions.partition {
                            it.endsAt > now
                        }

                        BookmarksUiState.Success(conference, upcoming, past)
                    } else {
                        BookmarksUiState.Success(conference, listOf(), listOf())
                    }
                }.catch { emit(BookmarksUiState.Error) }
    }
}