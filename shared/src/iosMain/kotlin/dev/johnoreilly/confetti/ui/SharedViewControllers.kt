package dev.johnoreilly.confetti.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.window.ComposeUIViewController
import dev.johnoreilly.confetti.decompose.ConferencesComponent
import dev.johnoreilly.confetti.fragment.SessionDetails
import dev.johnoreilly.confetti.ui.sessions.SessionDetailViewShared
import platform.UIKit.UIViewController

fun SessionDetailsViewController(conference: String, session: SessionDetails, conferenceThemeColor: String?, onSpeakerClick: (speakerId: String) -> Unit, onSocialLinkClicked: (String) -> Unit): UIViewController =
    ComposeUIViewController {
        ConferenceMaterialTheme(conferenceThemeColor) {
            SessionDetailViewShared(conference, session, onSpeakerClick, onSocialLinkClicked)
        }
    }


fun ConferenceListViewController(component: ConferencesComponent): UIViewController =

    ComposeUIViewController {
        MaterialTheme {
            ConferenceListView(component)
        }
    }
