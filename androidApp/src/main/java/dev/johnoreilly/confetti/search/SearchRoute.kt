package dev.johnoreilly.confetti.search

import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.johnoreilly.confetti.SearchViewModel
import dev.johnoreilly.confetti.SessionsViewModelParams
import dev.johnoreilly.confetti.auth.Authentication
import dev.johnoreilly.confetti.auth.User
import dev.johnoreilly.confetti.sessiondetails.navigation.SessionDetailsKey
import dev.johnoreilly.confetti.speakerdetails.navigation.SpeakerDetailsKey
import dev.johnoreilly.confetti.ui.ConfettiAppState
import org.koin.androidx.compose.getViewModel
import org.koin.compose.koinInject
import org.koin.core.parameter.ParametersHolder
import org.koin.core.parameter.parametersOf

@Composable
fun SearchRoute(
    conference: String,
    appState: ConfettiAppState,
    navigateToSession: (SessionDetailsKey) -> Unit,
    navigateToSpeaker: (SpeakerDetailsKey) -> Unit,
    onSwitchConference: () -> Unit,
    onSignIn: () -> Unit,
    onSignOut: () -> Unit,
) {

    val user by koinInject<Authentication>().currentUser.collectAsStateWithLifecycle()
    val viewModel = getViewModel<SearchViewModel>(parameters = {
        parametersOf(SessionsViewModelParams(conference, user?.uid, user))
    })
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
