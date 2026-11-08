package dev.johnoreilly.confetti.dev.johnoreilly.confetti.ui

import dev.johnoreilly.confetti.ui.components.WithConferenceComponents
import androidx.compose.runtime.Composable
import dev.johnoreilly.confetti.fragment.SessionDetails
import dev.johnoreilly.confetti.ui.sessions.SessionDetailViewShared

@Composable
fun SessionDetailViewSharedWrapper(conference: String, session: SessionDetails?, onSpeakerClick: (speakerId: String) -> Unit) {
    WithConferenceComponents {
        SessionDetailViewShared(conference, session, onSpeakerClick)
    }
}

