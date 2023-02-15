package dev.johnoreilly.confetti.ui

import androidx.compose.runtime.Composable
import dev.johnoreilly.confetti.fragment.SessionDetails

@Composable
fun SessionDetailViewSharedWrapper(session: SessionDetails?, socialLinkClicked: (String) -> Unit) {
    SessionDetailViewShared(session, socialLinkClicked)
}

