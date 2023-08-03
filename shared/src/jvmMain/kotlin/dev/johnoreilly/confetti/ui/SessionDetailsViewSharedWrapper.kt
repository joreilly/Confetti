package dev.johnoreilly.confetti.dev.johnoreilly.confetti.ui

import androidx.compose.runtime.Composable
import dev.johnoreilly.confetti.fragment.SessionDetails
import dev.johnoreilly.confetti.ui.SessionDetailViewShared

@Composable
fun SessionDetailViewSharedWrapper(session: SessionDetails?, socialLinkClicked: (String) -> Unit) {
    SessionDetailViewShared(session, socialLinkClicked)
}

