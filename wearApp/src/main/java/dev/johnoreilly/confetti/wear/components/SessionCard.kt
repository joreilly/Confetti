package dev.johnoreilly.confetti.wear.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.TitleCard
import com.google.android.horologist.compose.tools.ThemeValues
import com.google.android.horologist.compose.tools.WearPreview
import dev.johnoreilly.confetti.fragment.SessionDetails
import dev.johnoreilly.confetti.isBreak
import dev.johnoreilly.confetti.wear.preview.ConfettiPreviewThemes
import dev.johnoreilly.confetti.wear.preview.TestFixtures
import dev.johnoreilly.confetti.wear.ui.ConfettiTheme

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
            title = { Text(session.title) },
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
                    style = MaterialTheme.typography.caption2,
                    fontWeight = FontWeight.Light
                )
            }
        }
    }
}

@WearPreview
@Composable
fun SessionCardPreview(
    @PreviewParameter(ConfettiPreviewThemes::class) themeValues: ThemeValues
) {
    ConfettiTheme(colors = themeValues.colors) {
        SessionCard(session = TestFixtures.sessionDetails, sessionSelected = {})
    }
}


