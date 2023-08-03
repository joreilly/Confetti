package dev.johnoreilly.confetti.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.window.ComposeUIViewController
import dev.johnoreilly.confetti.fragment.SessionDetails
import platform.UIKit.UIViewController

fun SessionDetailsViewController(session: SessionDetails, onSpeakerClick: (speakerId: String) -> Unit, onSocialLinkClicked: (String) -> Unit): UIViewController =
    ComposeUIViewController {
        // iOS specific colors...just adding like this as test for now
        val colorScheme = lightColorScheme(
            primary = Color(0xFF007AFF),
            surface = Color(0xFFFFFFFF)
        )

        MaterialTheme(colorScheme = colorScheme) {
            SessionDetailViewShared(session, onSpeakerClick, onSocialLinkClicked)
        }
    }
