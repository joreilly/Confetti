package dev.johnoreilly.confetti.ui.livery

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import dev.johnoreilly.confetti.livery.SduiExperiment
import dev.johnoreilly.confetti.livery.confettiLiveryStyling
import dev.johnoreilly.confetti.livery.liveryHttpClient
import dev.johnoreilly.confetti.preview.PreviewImages
import dev.johnoreilly.confetti.preview.sessionDetails
import dev.johnoreilly.confetti.ui.components.NativeComponents
import dev.johnoreilly.confetti.ui.components.SessionCardModel
import kotlinx.datetime.LocalDateTime
import okhttp3.OkHttpClient

/**
 * A conference's session card and home-feed promotion as LiveryComponents draws them, from the
 * module's bundled catalogs only (no network). A conference with no template for the slot draws
 * Confetti's own row, and nothing for the promotion.
 */
@Preview(name = "KotlinConf", widthDp = 400, showBackground = true)
@Composable
internal fun LiveryKotlinConfPreview() = LiveryPreview("kotlinconf2026")

@Preview(name = "droidcon", widthDp = 400, showBackground = true)
@Composable
internal fun LiveryDroidconPreview() = LiveryPreview("droidconlondon2026")

@Composable
private fun LiveryPreview(conference: String) {
    val context = LocalContext.current
    val components = remember(context) {
        LiveryComponents(
            confettiLiveryStyling(context, liveryHttpClient(OkHttpClient()), refresh = false),
            NativeComponents(arm = SduiExperiment.LIVERY),
        )
    }
    MaterialTheme {
        PreviewImages {
            Column {
                components.SessionCard(
                    SessionCardModel(
                        conference = conference, session = sessionDetails, now = LocalDateTime.parse("2023-04-13T14:10"),
                        isBookmarked = false, isLoggedIn = true, trackColor = null, tags = emptyList(),
                        sessionSelected = {}, addBookmark = {}, removeBookmark = {}, onNavigateToSignIn = {},
                    ),
                )
                components.Promo("home.feed", conference, Modifier.fillMaxWidth())
            }
        }
    }
}
