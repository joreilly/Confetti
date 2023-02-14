@file:OptIn(ExperimentalHorologistComposeLayoutApi::class)

package dev.johnoreilly.confetti.wear.sessions

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.TitleCard
import com.google.android.horologist.compose.layout.ScalingLazyColumnState
import com.google.android.horologist.compose.navscaffold.ExperimentalHorologistComposeLayoutApi
import dev.johnoreilly.confetti.ConfettiViewModel
import dev.johnoreilly.confetti.fragment.SessionDetails
import dev.johnoreilly.confetti.isBreak
import dev.johnoreilly.confetti.wear.sessiondetails.navigation.SessionDetailsDestination
import dev.johnoreilly.confetti.wear.sessiondetails.navigation.SessionDetailsKey
import dev.johnoreilly.confetti.wear.sessions.navigation.ConferenceDateKey
import kotlinx.datetime.LocalDate
import org.koin.androidx.compose.getViewModel

@Composable
fun SessionsRoute(
    date: ConferenceDateKey,
    navigateToSession: (SessionDetailsKey) -> Unit,
    columnState: ScalingLazyColumnState,
    viewModel: ConfettiViewModel = getViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    SessionListView(
        date = date,
        uiState = uiState,
        sessionSelected = navigateToSession,
        columnState = columnState
    )
}

@Composable
fun SessionView(
    conference: String,
    session: SessionDetails,
    sessionSelected: (SessionDetailsKey) -> Unit,
) {
    val speakers = session.speakers.joinToString(", ") { it.speakerDetails.name }
    val room = session.room

    if (session.isBreak()) {
        Text(session.title)
    } else if (room == null) {
        TitleCard(
            modifier = Modifier.fillMaxWidth(),
            onClick = { sessionSelected(SessionDetailsKey(conference, session.id)) },
            title = { Text(session.title) }
        ) {
            if (speakers.isNotEmpty()) {
                Text(speakers)
            }
        }
    } else {
        TitleCard(
            modifier = Modifier.fillMaxWidth(),
            onClick = { sessionSelected(SessionDetailsKey(conference, session.id)) },
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


