package dev.johnoreilly.confetti.dev.johnoreilly.confetti.ui

import androidx.compose.runtime.Composable
import dev.johnoreilly.confetti.fragment.SessionDetails
import dev.johnoreilly.confetti.ui.sessions.SessionDetailViewShared

@Composable
fun SessionDetailViewSharedWrapper(conference: String, session: SessionDetails?, onSpeakerClick: (speakerId: String) -> Unit) {
    SessionDetailViewShared(conference, session, onSpeakerClick)
}

