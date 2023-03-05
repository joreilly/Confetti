package dev.johnoreilly.confetti.sessions

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.window.layout.DisplayFeature
import dev.johnoreilly.confetti.ConfettiViewModel
import dev.johnoreilly.confetti.fragment.SessionDetails
import dev.johnoreilly.confetti.isBreak
import dev.johnoreilly.confetti.sessionSpeakerLocation
import org.koin.androidx.compose.getViewModel


@Composable
fun SessionsRoute(
    isExpandedScreen: Boolean,
    displayFeatures: List<DisplayFeature>,
    navigateToSession: (String) -> Unit,
    onSwitchConferenceSelected: () -> Unit,
    viewModel: ConfettiViewModel = getViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    if (isExpandedScreen) {
        SessionListGridView(
            uiState,
            navigateToSession,
            onSwitchConferenceSelected
        )
    } else {
        SessionListView(
            uiState,
            navigateToSession,
            onSwitchConferenceSelected,
            viewModel::refresh
        )
    }
}



@Composable
fun SessionView(
    session: SessionDetails,
    sessionSelected: (sessionId: String) -> Unit
) {

    var modifier = Modifier.fillMaxSize()
    if (!session.isBreak()) {
        modifier = modifier.clickable(onClick = {
            sessionSelected(session.id)
        })
    }
    Column(modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = session.title, style = TextStyle(fontSize = 16.sp))
        }

        session.room?.let {
            Row(
                modifier = Modifier.padding(top = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    session.sessionSpeakerLocation(),
                    style = TextStyle(fontSize = 14.sp), fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
