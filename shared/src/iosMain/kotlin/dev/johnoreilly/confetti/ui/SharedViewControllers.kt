package dev.johnoreilly.confetti.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.window.ComposeUIViewController
import dev.johnoreilly.confetti.decompose.ConferencesComponent
import dev.johnoreilly.confetti.fragment.SessionDetails
import dev.johnoreilly.confetti.ui.sessions.SessionDetailViewShared
import dev.johnoreilly.confetti.ui.components.ConferenceComponents
import dev.johnoreilly.confetti.ui.components.WithConferenceComponents
import org.koin.compose.koinInject
import platform.UIKit.UIViewController

fun SessionDetailsViewController(conference: String, session: SessionDetails, conferenceThemeColor: String?, onSpeakerClick: (speakerId: String) -> Unit): UIViewController =
    ComposeUIViewController {
        val components: ConferenceComponents = koinInject()
        components.ConferenceTheme(conference, conferenceThemeColor) {
            WithConferenceComponents(components) {
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
