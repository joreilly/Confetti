package dev.johnoreilly.confetti.ui.speakers

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import dev.johnoreilly.confetti.ui.HomeScaffold
import confetti.shared.generated.resources.Res
import confetti.shared.generated.resources.speakers
import dev.johnoreilly.confetti.decompose.SpeakerDetailsComponent
import dev.johnoreilly.confetti.decompose.SpeakerDetailsUiState
import dev.johnoreilly.confetti.decompose.SpeakersComponent
import dev.johnoreilly.confetti.decompose.SpeakersUiState
import dev.johnoreilly.confetti.ui.component.ErrorView
import dev.johnoreilly.confetti.ui.component.LoadingView
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpeakersUI(
    component: SpeakersComponent,
    windowSizeClass: WindowSizeClass,
    topBarNavigationIcon: @Composable () -> Unit = {},
    topBarActions: @Composable RowScope.() -> Unit = {},
) {
    val uiState by component.uiState.subscribeAsState()

    HomeScaffold(
        title = stringResource(Res.string.speakers),
        windowSizeClass = windowSizeClass,
        topBarNavigationIcon = topBarNavigationIcon,
        topBarActions = topBarActions,
    ) {
        when (val state = uiState) {
            is SpeakersUiState.Success -> {
                SpeakerGridView(state.conference, state.speakers, component::onSpeakerClicked)
            }
            is SpeakersUiState.Loading -> LoadingView()
            is SpeakersUiState.Error -> ErrorView {}
        }
    }
}

@Composable
fun SpeakerDetailsUI(component: SpeakerDetailsComponent) {
    val uriHandler = LocalUriHandler.current
    val uiState by component.uiState.subscribeAsState()

    when (val state = uiState) {
        is SpeakerDetailsUiState.Loading -> LoadingView()
        is SpeakerDetailsUiState.Error -> ErrorView()
        is SpeakerDetailsUiState.Success -> SpeakerDetailsView(
            conference = state.conference,
            speaker = state.details,
            isBookmarked = { state.bookmarks.contains(it) },
            addBookmark = component::addBookmark,
            removeBookmark = component::removeBookmark,
            onNavigateToSignIn = component::onSignInClicked,
            isLoggedIn = component.isLoggedIn,
            navigateToSession = component::onSessionClicked,
            popBack = component::onCloseClicked,
            onSocialLinkClicked = uriHandler::openUri
        )
    }
}
