package dev.johnoreilly.confetti.decompose

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.childContext
import dev.johnoreilly.confetti.ConfettiRepository
import dev.johnoreilly.confetti.auth.User
import dev.johnoreilly.confetti.fragment.SessionDetails
import dev.johnoreilly.confetti.utils.DateService
import dev.johnoreilly.confetti.utils.createCurrentLocalDateTimeFlow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDateTime
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

interface BookmarksComponent {

    val isLoggedIn: Boolean
    val loading: Flow<Boolean>
    val bookmarks: Flow<Set<String>>
    val pastSessions: Flow<Map<LocalDateTime, List<SessionDetails>>>
    val upcomingSessions: Flow<Map<LocalDateTime, List<SessionDetails>>>

    fun addBookmark(sessionId: String)
    fun removeBookmark(sessionId: String)
    fun onSessionClicked(id: String)
    fun onSignInClicked()
}

class DefaultBookmarksComponent(
    componentContext: ComponentContext,
    private val conference: String,
    private val user: User?,
    private val onSessionSelected: (id: String) -> Unit,
    private val onSignIn: () -> Unit,
) : BookmarksComponent, KoinComponent, ComponentContext by componentContext {

    private val repository: ConfettiRepository by inject()
    private val dateService: DateService by inject()
    private val coroutineScope = coroutineScope()

    private val sessionsComponent =
        SessionsSimpleComponent(
            componentContext = childContext("Sessions"),
            conference = conference,
            user = user,
        )

    override val isLoggedIn: Boolean = user != null

    override val loading: Flow<Boolean> = sessionsComponent
        .uiState
        .map { state -> state is SessionsUiState.Loading }

    private val loadedSessions = sessionsComponent
        .uiState
        .filterIsInstance<SessionsUiState.Success>()

    override val bookmarks: Flow<Set<String>> = loadedSessions
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

    override val pastSessions: Flow<Map<LocalDateTime, List<SessionDetails>>> = sessions
        .combine(currentDateTimeFlow) { sessions, now ->
            sessions.filter { session ->
                session.endsAt < now
            }.groupBy { it.startsAt }
        }

    override val upcomingSessions: Flow<Map<LocalDateTime, List<SessionDetails>>> = sessions
        .combine(currentDateTimeFlow) { sessions, now ->
            sessions.filter { session ->
                session.endsAt >= now
            }.groupBy { it.startsAt }
        }

    override fun addBookmark(sessionId: String) {
        coroutineScope.launch {
            repository.addBookmark(conference, user?.uid, user, sessionId)
        }
    }

    override fun removeBookmark(sessionId: String) {
        coroutineScope.launch {
            repository.removeBookmark(conference, user?.uid, user, sessionId)
        }
    }

    override fun onSessionClicked(id: String) {
        onSessionSelected(id)
    }

    override fun onSignInClicked() {
        onSignIn()
    }
}
