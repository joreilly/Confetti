import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import dev.johnoreilly.confetti.preview.sessionDetails
import dev.johnoreilly.confetti.ui.SessionDetailViewShared

class ScreenshotTest {

    @Preview(showBackground = true)
    @Composable
    fun SessionDetailsPreview() {
        MaterialTheme {
            SessionDetailViewShared(
                conference = "kotlinconf2023",
                session = sessionDetails,
                onSpeakerClick = {},
                onSocialLinkClicked = {}
            )
        }
    }
}