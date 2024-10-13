package dev.johnoreilly.confetti.sessions

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
import kotlinx.coroutines.flow.receiveAsFlow

@Composable
fun SessionsRoute(
    component: SessionsComponent,
    windowSizeClass: WindowSizeClass,
    topBarNavigationIcon: @Composable () -> Unit = {},
    topBarActions: @Composable RowScope.() -> Unit = {},
    snackbarHostState: SnackbarHostState,
) {
    val uiState by component.uiState.subscribeAsState()

    HomeScaffold(
        title = (uiState as? SessionsUiState.Success)?.conferenceName,
        windowSizeClass = windowSizeClass,
        topBarNavigationIcon = topBarNavigationIcon,
        topBarActions = topBarActions,
    ) {
        // comment out until issue with HorizontalPager in shared code
        // is resolved
//        if (windowSizeClass.isExpanded) {
//            SessionListGridView(
//                uiState = uiState,
//                sessionSelected = component::onSessionClicked,
//                onRefresh = component::refresh,
//                addBookmark = component::addBookmark,
//                removeBookmark = component::removeBookmark,
//                onNavigateToSignIn = component::onSignInClicked,
//                isLoggedIn = component.isLoggedIn,
//            )
//        } else {
            SessionListView(
                uiState = uiState,
                sessionSelected = component::onSessionClicked,
                addBookmark = component::addBookmark,
                removeBookmark = component::removeBookmark,
                onRefresh = component::refresh,
                onNavigateToSignIn = component::onSignInClicked,
                isLoggedIn = component.isLoggedIn,
            )
//        }
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
