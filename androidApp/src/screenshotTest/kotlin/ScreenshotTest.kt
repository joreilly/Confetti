import androidx.compose.material3.MaterialTheme
import dev.johnoreilly.confetti.ui.components.NativeComponents
import dev.johnoreilly.confetti.preview.PreviewImages
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import dev.johnoreilly.confetti.preview.sessionDetails
import dev.johnoreilly.confetti.ui.sessions.SessionDetailViewShared

class ScreenshotTest {

    @Preview(showBackground = true)
    @Composable
    fun SessionDetailsPreview() {
        PreviewImages {
            MaterialTheme {
                with(NativeComponents()) {
                    SessionDetailViewShared(
                        conference = "kotlinconf2023",
                        session = sessionDetails,
                        onSpeakerClick = {},
                    )
                }
            }
        }
    }
}