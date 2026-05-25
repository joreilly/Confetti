package dev.johnoreilly.confetti.ui

import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import dev.johnoreilly.confetti.preview.johnOreillySpeaker
import dev.johnoreilly.confetti.preview.sessionDetails
import dev.johnoreilly.confetti.ui.sessions.SessionDetailViewShared
import dev.johnoreilly.confetti.ui.speakers.SpeakerDetailsView

/**
 * Android-specific previews that exercise the real [ConfettiTheme] (with the
 * platform colour scheme and tonal background) plus light + dark uiMode
 * variants. The shared CMP previews live next to each screen and don't have
 * access to `android.content.res.Configuration` constants, so we duplicate a
 * small set of representative screens here for the Android Studio preview pane
 * and for Roborazzi-style screenshot comparison.
 */

@Preview(
    name = "Phone — light",
    widthDp = 411,
    heightDp = 914,
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_NO,
)
@Preview(
    name = "Phone — dark",
    widthDp = 411,
    heightDp = 914,
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Preview(
    name = "Tablet landscape",
    widthDp = 960,
    heightDp = 600,
    showBackground = true,
)
@Composable
internal fun AndroidSessionDetailPreview() {
    ConfettiTheme {
        SessionDetailViewShared(
            conference = "kotlinconf2023",
            session = sessionDetails,
            onSpeakerClick = {},
            onSocialLinkClicked = {},
        )
    }
}

@Preview(
    name = "Phone — light",
    widthDp = 411,
    heightDp = 914,
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_NO,
)
@Preview(
    name = "Phone — dark",
    widthDp = 411,
    heightDp = 914,
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Preview(
    name = "Tablet landscape",
    widthDp = 960,
    heightDp = 600,
    showBackground = true,
)
@Composable
internal fun AndroidSpeakerDetailsPreview() {
    ConfettiTheme {
        SpeakerDetailsView(
            conference = "kotlinconf2023",
            speaker = johnOreillySpeaker,
            navigateToSession = {},
            popBack = {},
            onSocialLinkClicked = {},
        )
    }
}
