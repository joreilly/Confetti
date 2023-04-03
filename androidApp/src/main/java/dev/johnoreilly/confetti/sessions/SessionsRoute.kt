package dev.johnoreilly.confetti.sessions

import androidx.compose.material3.SnackbarDuration
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.johnoreilly.confetti.SessionsUiState
import dev.johnoreilly.confetti.SessionsViewModel
import dev.johnoreilly.confetti.sessiondetails.navigation.SessionDetailsKey
import dev.johnoreilly.confetti.ui.ConfettiAppState
import dev.johnoreilly.confetti.ui.ConfettiScaffold
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import org.koin.androidx.compose.getViewModel

@Composable
fun SessionsRoute(
    conference: String,
    appState: ConfettiAppState,
    navigateToSession: (SessionDetailsKey) -> Unit,
    navigateToSignIn: () -> Unit,
    onSignOut: () -> Unit,
    onSwitchConferenceSelected: () -> Unit,
) {
    ConfettiScaffold(
        conference = conference,
        appState = appState,
        onSwitchConference = onSwitchConferenceSelected,
        onSignIn = navigateToSignIn,
        onSignOut = onSignOut,
    ) {
        val snackbarHostState = it.snackbarHostState
        val user = it.user

        val viewModel: SessionsViewModel = getViewModel<SessionsViewModel>().apply {
            configure(conference, user?.uid, user)
        }
        val uiState by viewModel.uiState.collectAsStateWithLifecycle()
        var refreshing by remember { mutableStateOf(false) }
        val scope = rememberCoroutineScope()
        fun refresh() {
            scope.launch {
                refreshing = true
                viewModel.refresh()
                refreshing = false
            }
        }

        it.title.value = (uiState as? SessionsUiState.Success)?.conferenceName

        if (appState.isExpandedScreen) {
            SessionListGridView(
                uiState = uiState,
                sessionSelected = navigateToSession,
                onRefresh = ::refresh,
                addBookmark = viewModel::addBookmark,
                removeBookmark = viewModel::removeBookmark,
                onNavigateToSignIn = navigateToSignIn,
                user = user
            )
        } else {
            SessionListView(
                uiState = uiState,
                refreshing = refreshing,
                sessionSelected = navigateToSession,
                addBookmark = viewModel::addBookmark,
                removeBookmark = viewModel::removeBookmark,
                onRefresh = ::refresh,
                onNavigateToSignIn = navigateToSignIn,
                user
            )
        }

        val addErrorCount by viewModel.addErrorChannel.receiveAsFlow()
            .collectAsStateWithLifecycle(initialValue = 0)
        LaunchedEffect(addErrorCount) {
            if (addErrorCount > 0) {
                snackbarHostState.showSnackbar(
                    message = "Error while adding bookmark",
                    duration = SnackbarDuration.Short,
                )
            }
        }

        val removeErrorCount by viewModel.removeErrorChannel.receiveAsFlow()
            .collectAsStateWithLifecycle(initialValue = 0)
        LaunchedEffect(removeErrorCount) {
            if (removeErrorCount > 0) {
                snackbarHostState.showSnackbar(
                    message = "Error while removing bookmark",
                    duration = SnackbarDuration.Short,
                )
            }
        }
    }
}
