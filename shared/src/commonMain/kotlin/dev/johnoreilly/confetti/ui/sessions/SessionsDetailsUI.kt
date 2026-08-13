package dev.johnoreilly.confetti.ui.sessions

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import dev.johnoreilly.confetti.decompose.SessionDetailsComponent
import dev.johnoreilly.confetti.decompose.SessionDetailsUiState
import dev.johnoreilly.confetti.ui.SignInDialog
import dev.johnoreilly.confetti.ui.bookmarks.Bookmark
import dev.johnoreilly.confetti.ui.component.ErrorView
import dev.johnoreilly.confetti.ui.component.LoadingView


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SessionDetailsUI(component: SessionDetailsComponent) {
    val uriHandler = LocalUriHandler.current
    var showDialog by remember { mutableStateOf(false) }

    val uiState by component.uiState.subscribeAsState()
    val isBookmarked by component.isBookmarked.collectAsState()

    Scaffold(topBar = {
        CenterAlignedTopAppBar(
            title = { },
            navigationIcon = {
                IconButton(onClick = component::onCloseClicked ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
            },
            actions = {
                Bookmark(
                    isBookmarked = isBookmarked,
                    onBookmarkChange = { shouldAdd ->
                        if (!component.isLoggedIn) {
                            showDialog = true
                            return@Bookmark
                        }
                        if (shouldAdd) {
                            component.addBookmark()
                        } else {
                            component.removeBookmark()
                        }
                    }
                )
            }

        )
    }) {
        Column(Modifier.padding(it)) {
            when (val state = uiState) {
                is SessionDetailsUiState.Loading -> LoadingView()
                is SessionDetailsUiState.Error -> ErrorView(component::refresh)

                is SessionDetailsUiState.Success ->
                    SessionDetailViewShared(
                        state.conference, state.sessionDetails,
                        component::onSpeakerClicked
                    )
            }
        }
    }

    if (showDialog) {
        SignInDialog(
            onDismissRequest = { showDialog = false },
            onSignInClicked = component::onSignInClicked
        )
    }
}
