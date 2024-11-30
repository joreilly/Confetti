package dev.johnoreilly.confetti.ui.sessions

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import dev.johnoreilly.confetti.decompose.SessionsComponent
import dev.johnoreilly.confetti.decompose.SessionsUiState
import dev.johnoreilly.confetti.ui.HomeScaffold
import dev.johnoreilly.confetti.utils.isExpanded
import kotlinx.coroutines.flow.receiveAsFlow

@Composable
fun SessionsUI(
    component: SessionsComponent,
    windowSizeClass: WindowSizeClass,
    topBarNavigationIcon: @Composable () -> Unit = {},
    topBarActions: @Composable RowScope.() -> Unit = {},
    snackbarHostState: SnackbarHostState
) {
    val uiState by component.uiState.subscribeAsState()

    val title = (uiState as? SessionsUiState.Success)?.conferenceName ?: ""
    HomeScaffold(
        title = title,
        windowSizeClass = windowSizeClass,
        topBarNavigationIcon = topBarNavigationIcon,
        topBarActions = topBarActions,
    ) {
        Column {
            if (windowSizeClass.isExpanded) {
                SessionListGridView(
                    uiState = uiState,
                    sessionSelected = component::onSessionClicked,
                    onRefresh = {},
                    addBookmark = {},
                    removeBookmark = {},
                    onNavigateToSignIn = {},
                    isLoggedIn = component.isLoggedIn,
                )
            } else {
                SessionListView(
                    uiState = uiState,
                    sessionSelected = component::onSessionClicked,
                    addBookmark = component::addBookmark,
                    removeBookmark = component::removeBookmark,
                    onRefresh = component::refresh,
                    onNavigateToSignIn = component::onSignInClicked,
                    isLoggedIn = component.isLoggedIn,
                )
            }
        }
    }


    val addErrorCount by component.addErrorChannel.receiveAsFlow()
        .collectAsStateWithLifecycle(initialValue = 0)
    LaunchedEffect(addErrorCount) {
        if (addErrorCount > 0) {
            snackbarHostState.showSnackbar(
                message = "Error while adding bookmark",
                duration = SnackbarDuration.Short,
            )
        }
    }

    val removeErrorCount by component.removeErrorChannel.receiveAsFlow()
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
