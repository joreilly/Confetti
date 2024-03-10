package dev.johnoreilly.confetti.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.window.ComposeUIViewController
import dev.johnoreilly.confetti.GetConferencesQuery
import dev.johnoreilly.confetti.fragment.SessionDetails
import platform.UIKit.UIViewController

fun SessionDetailsViewController(session: SessionDetails, conferenceThemeColor: String?, onSpeakerClick: (speakerId: String) -> Unit, onSocialLinkClicked: (String) -> Unit): UIViewController =
    ComposeUIViewController {
        ConferenceMaterialTheme(conferenceThemeColor) {
            SessionDetailViewShared(session, onSpeakerClick, onSocialLinkClicked)
        }
    }


fun ConferenceListViewController(conferenceListByYear: Map<Int, List<GetConferencesQuery.Conference>>, onConferenceClick: (GetConferencesQuery.Conference) -> Unit): UIViewController =
    ComposeUIViewController {
        MaterialTheme {
            ConferenceListView(conferenceListByYear, onConferenceClick)
        }
    }
