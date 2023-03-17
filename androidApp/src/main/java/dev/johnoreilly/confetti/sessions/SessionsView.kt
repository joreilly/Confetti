package dev.johnoreilly.confetti.sessions

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.window.layout.DisplayFeature
import dev.johnoreilly.confetti.SessionsUiState
import dev.johnoreilly.confetti.SessionsViewModel
import dev.johnoreilly.confetti.ui.ConfettiAppState
import dev.johnoreilly.confetti.ui.ConfettiScaffold
import kotlinx.coroutines.launch
import org.koin.androidx.compose.getViewModel


@Composable
fun SessionsView(
    appState: ConfettiAppState,
    @Suppress("UNUSED_PARAMETER") displayFeatures: List<DisplayFeature>,
    navigateToSession: (String) -> Unit,
    navigateToSignIn: () -> Unit,
    onSignOut: () -> Unit,
    onSwitchConferenceSelected: () -> Unit,
    conference: String,
) {
    val viewModel: SessionsViewModel = getViewModel()
    viewModel.setConference(conference)
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var refreshing by remember { mutableStateOf(false) }
    val refreshScope = rememberCoroutineScope()
    fun refresh() {
        refreshScope.launch {
            refreshing = true
            viewModel.refresh()
            refreshing = false
        }
    }

    ConfettiScaffold(
        title = (uiState as? SessionsUiState.Success)?.conferenceName,
        appState = appState,
        onSwitchConference = onSwitchConferenceSelected,
        onSignIn = navigateToSignIn,
        onSignOut = onSignOut,
    ){
        if (appState.isExpandedScreen) {
            SessionListGridView(
                uiState = uiState,
                sessionSelected = navigateToSession,
                onRefresh = ::refresh,
            )
        } else {
            SessionListView(
                uiState,
                refreshing,
                navigateToSession,
                { viewModel.addBookmark(it) },
                { viewModel.removeBookmark(it) },
                ::refresh
            )
        }
    }
}
