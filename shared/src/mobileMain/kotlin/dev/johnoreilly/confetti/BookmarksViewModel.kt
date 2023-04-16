package dev.johnoreilly.confetti

import com.rickclephas.kmm.viewmodel.KMMViewModel
import dev.johnoreilly.confetti.utils.DateService
import dev.johnoreilly.confetti.utils.createCurrentLocalDateTimeFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.map

class BookmarksViewModel(
    private val dateService: DateService,
    // TODO: Remove dependency between view models.
    private val sessionsViewModel: SessionsViewModel,
) : KMMViewModel() {

    val loading = sessionsViewModel
        .uiState
        .map { state -> state is SessionsUiState.Loading }

    private val loadedSessions = sessionsViewModel
        .uiState
        .filterIsInstance<SessionsUiState.Success>()

    val bookmarks = loadedSessions
        .map { state -> state.bookmarks }

    private val sessions = loadedSessions
        .map { state ->
            state
                .sessionsByStartTimeList
                .flatMap { sessions -> sessions.values }
                .flatten()
        }
        .combine(bookmarks) { sessions, bookmarks ->
            sessions.filter { session -> session.id in bookmarks }
        }

    private val currentDateTimeFlow = dateService
        .createCurrentLocalDateTimeFlow()

    val pastSessions = sessions
        .combine(currentDateTimeFlow) { sessions, now ->
            sessions.filter { session ->
                session.endsAt < now
            }.groupBy { it.startsAt }
        }

    val upcomingSessions = sessions
        .combine(currentDateTimeFlow) { sessions, now ->
            sessions.filter { session ->
                session.endsAt >= now
            }.groupBy { it.startsAt }
        }

    fun configure(conference: String, uid: String?, tokenProvider: TokenProvider?) {
        sessionsViewModel.configure(conference, uid, tokenProvider)
    }

    fun addBookmark(sessionId: String) {
        sessionsViewModel.addBookmark(sessionId)
    }

    fun removeBookmark(sessionId: String) {
        sessionsViewModel.removeBookmark(sessionId)
    }
}
