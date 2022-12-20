@file:OptIn(ExperimentalLifecycleComposeApi::class)

package dev.johnoreilly.confetti.wear.sessions

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.ExperimentalLifecycleComposeApi
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.TitleCard
import dev.johnoreilly.confetti.fragment.SessionDetails
import dev.johnoreilly.confetti.isBreak
import dev.johnoreilly.confetti.wear.ConfettiViewModel
import org.koin.androidx.compose.getViewModel


@Composable
fun SessionsRoute(
    navigateToSession: (String) -> Unit,
    onSwitchConferenceSelected: () -> Unit,
    viewModel: ConfettiViewModel = getViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    SessionListView(
        uiState = uiState,
        sessionSelected = navigateToSession,
        onSwitchConferenceSelected = onSwitchConferenceSelected,
        onRefresh = { viewModel.refresh() }
    )
}

@Composable
fun SessionView(
    session: SessionDetails,
    sessionSelected: (sessionId: String) -> Unit,
) {
    val speakers = session.speakers.joinToString(", ") { it.speakerDetails.name }
    val room = session.room

    if (session.isBreak()) {
        Text(session.title)
    } else if (room == null) {
        TitleCard(
            modifier = Modifier.fillMaxWidth(),
            onClick = { sessionSelected(session.id) },
            title = { Text(session.title) }
        ) {
            if (speakers.isNotEmpty()) {
                Text(speakers)
            }
        }
    } else {
        TitleCard(
            modifier = Modifier.fillMaxWidth(),
            onClick = { sessionSelected(session.id) },
            title = { Text(room.name) }
        ) {
            Text(session.title)
            if (speakers.isNotEmpty()) {
                Text(
                    speakers,
                    style = MaterialTheme.typography.caption3,
                    color = MaterialTheme.colors.secondary
                )
            }
        }
    }
}


