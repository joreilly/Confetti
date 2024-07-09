package dev.johnoreilly.confetti.wear.home

import com.apollographql.apollo.cache.normalized.FetchPolicy
import com.arkivanov.decompose.ComponentContext
import dev.johnoreilly.confetti.ConfettiRepository
import dev.johnoreilly.confetti.GetConferenceDataQuery
import dev.johnoreilly.confetti.auth.User
import dev.johnoreilly.confetti.decompose.coroutineScope
import dev.johnoreilly.confetti.utils.ClientQuery.toUiState
import dev.johnoreilly.confetti.utils.QueryResult
import dev.johnoreilly.confetti.wear.bookmarks.BookmarksUiState
import dev.johnoreilly.confetti.wear.bookmarks.DefaultBookmarksComponent.Companion.toUiState
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

interface HomeComponent {
    val uiState: StateFlow<QueryResult<HomeUiState>>

    val bookmarksUiState: StateFlow<QueryResult<BookmarksUiState>>

    fun onSessionClicked(session: String)
    fun onDayClicked(it: LocalDate)
    fun onSettingsClicked()
    fun onBookmarksClick()
    fun addBookmark(sessionId: String)
    fun removeBookmark(sessionId: String)
}

class DefaultHomeComponent(
    componentContext: ComponentContext,
    private val conference: String,
    private val user: User?,
    private val onSessionSelected: (String) -> Unit,
    private val onDaySelected: (LocalDate) -> Unit,
    private val onSettingsSelected: () -> Unit,
    private val onBookmarksToggled: () -> Unit,
) : HomeComponent, KoinComponent, ComponentContext by componentContext {
    private val coroutineScope = coroutineScope()

    val repository: ConfettiRepository by inject()

    override val uiState: StateFlow<QueryResult<HomeUiState>> =
        repository.conferenceHomeData(conference = conference, fetchPolicy = FetchPolicy.CacheFirst).toUiState {
            it.toUiState()
        }
            .stateIn(coroutineScope, SharingStarted.WhileSubscribed(), QueryResult.Loading)

    override val bookmarksUiState: StateFlow<QueryResult<BookmarksUiState>> =
        repository.bookmarkedSessionsQuery(
            conference,
            user?.uid,
            user,
            FetchPolicy.CacheFirst
        ).toUiState {
            it.toUiState()
        }
            .stateIn(coroutineScope, SharingStarted.WhileSubscribed(), QueryResult.Loading)

    override fun onSessionClicked(session: String) {
        onSessionSelected(session)
    }

    override fun onDayClicked(it: LocalDate) {
        onDaySelected(it)
    }

    override fun onSettingsClicked() {
        onSettingsSelected()
    }

    override fun onBookmarksClick() {
        onBookmarksToggled()
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

    companion object {
        fun GetConferenceDataQuery.Data.toUiState() = HomeUiState(
            config.id,
            config.name,
            config.days,
        )
    }
}
