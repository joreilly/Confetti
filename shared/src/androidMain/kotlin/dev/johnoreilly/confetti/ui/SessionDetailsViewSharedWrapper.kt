package dev.johnoreilly.confetti.ui

import androidx.compose.runtime.Composable
import dev.johnoreilly.confetti.fragment.SessionDetails

@Composable
fun SessionDetailViewSharedWrapper(session: SessionDetails?, onSpeakerClick: (speakerId: String) -> Unit, onSocialLinkClicked: (String) -> Unit) {
    SessionDetailViewShared(session, onSpeakerClick, onSocialLinkClicked)
}

