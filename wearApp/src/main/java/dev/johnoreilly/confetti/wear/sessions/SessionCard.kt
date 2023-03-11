@file:OptIn(ExperimentalHorologistComposeLayoutApi::class)

package dev.johnoreilly.confetti.wear.sessions

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.TitleCard
import com.google.android.horologist.compose.navscaffold.ExperimentalHorologistComposeLayoutApi
import dev.johnoreilly.confetti.fragment.SessionDetails
import dev.johnoreilly.confetti.isBreak

@Composable
fun SessionCard(
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


