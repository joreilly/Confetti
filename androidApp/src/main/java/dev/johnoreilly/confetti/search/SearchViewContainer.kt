package dev.johnoreilly.confetti.search

import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.johnoreilly.confetti.SearchViewModel
import dev.johnoreilly.confetti.sessiondetails.navigation.SessionDetailsKey
import dev.johnoreilly.confetti.speakerdetails.navigation.SpeakerDetailsKey
import dev.johnoreilly.confetti.ui.ConfettiAppState
import org.koin.androidx.compose.getViewModel

@Composable
fun SearchViewContainer(
    conference: String,
    appState: ConfettiAppState,
    navigateToSession: (SessionDetailsKey) -> Unit,
    navigateToSpeaker: (SpeakerDetailsKey) -> Unit,
    onSwitchConference: () -> Unit,
    onSignIn: () -> Unit,
    onSignOut: () -> Unit,
) {
    val viewModel = getViewModel<SearchViewModel>()
    SideEffect {
        viewModel.configure(conference)
    }
    val search by viewModel
        .search
        .collectAsStateWithLifecycle(initialValue = "")
    val loading by viewModel
        .loading
        .collectAsStateWithLifecycle(initialValue = true)
    val sessions by viewModel
        .sessions
        .collectAsStateWithLifecycle(initialValue = emptyList())
    val bookmarks by viewModel
        .bookmarks
        .collectAsStateWithLifecycle(initialValue = emptySet())
    val speakers by viewModel
        .speakers
        .collectAsStateWithLifecycle(initialValue = emptyList())
    SearchView(
        conference = conference,
        appState = appState,
        navigateToSession = navigateToSession,
        navigateToSpeaker = navigateToSpeaker,
        onSwitchConference = onSwitchConference,
        onSignIn = onSignIn,
        onSignOut = onSignOut,
        sessions = sessions,
        speakers = speakers,
        search = search,
        onSearchChange = viewModel::onSearchChange,
        bookmarks = bookmarks,
        addBookmark = viewModel::addBookmark,
        removeBookmark = viewModel::removeBookmark,
        loading = loading,
    )
}
