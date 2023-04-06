package dev.johnoreilly.confetti

import androidx.compose.ui.test.onNodeWithText
import dev.johnoreilly.confetti.fragment.SpeakerDetails
import dev.johnoreilly.confetti.speakerdetails.SpeakerDetailsView
import dev.johnoreilly.confetti.ui.ConfettiTheme
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Test


class SpeakerDetailsScTest : BaseScreenshotTest(false, a11yEnabled = true) {


    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun screenshotDetailsA11y() {
        val mockSpeaker = SpeakerDetails(
            id = "",
            name = "Some Speaker",
            photoUrl = "",
            tagline = "",
            company = "Some Company",
            companyLogoUrl = "",
            city = "Some City",
            bio = "Some bio",
            sessions = emptyList(),
            socials = emptyList()
        )
        screenshotTestRule.takeScreenshot(
            checks = {
                it.onNodeWithText("Some Speaker").assertExists()
            }
        ) {
            ConfettiTheme {
                SpeakerDetailsView(
                    conference = "droidCon",
                    speaker = mockSpeaker,
                    navigateToSession = {},
                    popBack = {}
                )
            }
        }
    }
}
