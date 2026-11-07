package dev.johnoreilly.confetti.dev.johnoreilly.confetti.ui

import dev.johnoreilly.confetti.ui.components.NativeComponents
import androidx.compose.runtime.Composable
import dev.johnoreilly.confetti.fragment.SessionDetails
import dev.johnoreilly.confetti.ui.sessions.SessionDetailViewShared

@Composable
fun SessionDetailViewSharedWrapper(conference: String, session: SessionDetails?, onSpeakerClick: (speakerId: String) -> Unit) {
    with(NativeComponents()) {
        SessionDetailViewShared(conference, session, onSpeakerClick)
    }
}

