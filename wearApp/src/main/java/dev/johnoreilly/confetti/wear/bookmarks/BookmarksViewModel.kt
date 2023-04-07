@file:OptIn(ExperimentalCoroutinesApi::class)

package dev.johnoreilly.confetti.wear.bookmarks

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.apollographql.apollo3.api.ApolloResponse
import com.apollographql.apollo3.cache.normalized.FetchPolicy
import dev.johnoreilly.confetti.ConfettiRepository
import dev.johnoreilly.confetti.GetBookmarksQuery
import dev.johnoreilly.confetti.auth.Authentication
import dev.johnoreilly.confetti.auth.User
import dev.johnoreilly.confetti.toTimeZone
import dev.johnoreilly.confetti.wear.bookmarks.navigation.BookmarksDestination
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.withContext
import kotlinx.datetime.toKotlinInstant
import kotlinx.datetime.toLocalDateTime
import java.time.Instant

class BookmarksViewModel(
    savedStateHandle: SavedStateHandle,
    repository: ConfettiRepository,
    authentication: Authentication
) : ViewModel() {
    private val conference: String =
        BookmarksDestination.fromNavArgs(savedStateHandle)

    val uiState: StateFlow<BookmarksUiState> = authentication.currentUser.flatMapLatest { user ->
        if (user == null) {
            flowOf(BookmarksUiState.NotLoggedIn)
        } else {
            // Assume this is a good point to refresh
            val fetchPolicy = FetchPolicy.CacheAndNetwork
            fetchBookmarkedSessions(repository, conference, user, fetchPolicy)
        }
    }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), BookmarksUiState.Loading)

    companion object {
        suspend fun fetchBookmarkedSessions(
            repository: ConfettiRepository,
            conference: String,
            user: User,
            fetchPolicy: FetchPolicy
        ): Flow<BookmarksUiState> = repository.bookmarks(conference, user.uid, user, fetchPolicy)
            .map<ApolloResponse<GetBookmarksQuery.Data>, BookmarksUiState> { bookmarks ->
                val ids = bookmarks.data?.bookmarks?.sessionIds.orEmpty()

                if (ids.isNotEmpty()) {
                    val allSessions = withContext(Dispatchers.Unconfined) {
                        ids.map {
                            async {
                                repository.sessionDetails(
                                    conference,
                                    it,
                                    fetchPolicy = FetchPolicy.CacheFirst
                                ).map { response ->
                                    response.data?.let {
                                        Pair(it.session.sessionDetails, it.config.timezone)
                                    }
                                }.first()
                            }
                        }.awaitAll().filterNotNull()
                    }

                    if (allSessions.isNotEmpty()) {
                        val timeZone = allSessions.first().second.toTimeZone()
                        val now = Instant.now().toKotlinInstant().toLocalDateTime(timeZone)

                        val (upcoming, past) = allSessions.map { it.first }.partition {
                            it.endsAt > now
                        }

                        return@map BookmarksUiState.Success(conference, upcoming, past)
                    }
                }

                BookmarksUiState.Success(conference, listOf(), listOf())
            }.catch { emit(BookmarksUiState.Error) }
    }
}