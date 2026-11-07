package dev.johnoreilly.confetti.ui

import dev.johnoreilly.confetti.ui.components.NativeComponents
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.window.ComposeUIViewController
import dev.johnoreilly.confetti.decompose.ConferencesComponent
import dev.johnoreilly.confetti.fragment.SessionDetails
import dev.johnoreilly.confetti.ui.sessions.SessionDetailViewShared
import platform.UIKit.UIViewController

fun SessionDetailsViewController(conference: String, session: SessionDetails, conferenceThemeColor: String?, onSpeakerClick: (speakerId: String) -> Unit): UIViewController =
    ComposeUIViewController {
        ConferenceMaterialThemeFromSettings(conferenceThemeColor) {
            with(NativeComponents()) {
                SessionDetailViewShared(conference, session, onSpeakerClick)
            }
        }
    }


fun ConferenceListViewController(component: ConferencesComponent): UIViewController =

    ComposeUIViewController {
        MaterialTheme {
            ConferenceListView(component)
        }
    }
