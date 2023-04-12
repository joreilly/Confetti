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
            name = "Ben Zweber",
            photoUrl = "",
            tagline = "",
            company = "Big Nerd Ranch",
            companyLogoUrl = "",
            city = "Some City",
            bio = "Ben is an Android Solution architect who has an obsession with Kotin and Android",
            sessions = emptyList(),
            socials = emptyList(),
            __typename = "Speaker",
        )
        screenshotTestRule.takeScreenshot(
            checks = {
                it.onNodeWithText("Ben Zweber").assertExists()
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
