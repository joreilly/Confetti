package dev.johnoreilly.confetti.ui

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
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import dev.johnoreilly.confetti.decompose.SessionDetailsComponent
import dev.johnoreilly.confetti.decompose.SessionDetailsUiState


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SessionDetailsUI(component: SessionDetailsComponent) {
    val uriHandler = LocalUriHandler.current

    val uiState by component.uiState.subscribeAsState()

    Scaffold(topBar = {
        CenterAlignedTopAppBar(
            title = { },
            navigationIcon = {
                IconButton(onClick = component::onCloseClicked ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
            }
        )
    }) {
        Column(Modifier.padding(it)) {
            when (val state = uiState) {
                is SessionDetailsUiState.Loading -> LoadingView()
                is SessionDetailsUiState.Error -> ErrorView()

                is SessionDetailsUiState.Success ->
                    SessionDetailViewShared(
                        state.conference, state.sessionDetails,
                        component::onSpeakerClicked
                    ) { url ->
                        uriHandler.openUri(url)
                    }
            }
        }
    }
}
