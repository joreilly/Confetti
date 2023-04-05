package dev.johnoreilly.confetti

import dev.johnoreilly.confetti.di.KoinTestApp
import dev.johnoreilly.confetti.fragment.SpeakerDetails
import dev.johnoreilly.confetti.speakerdetails.SpeakerDetailsView
import dev.johnoreilly.confetti.test.screenshot.createScreenshotTestRule
import dev.johnoreilly.confetti.ui.ConfettiTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.test.KoinTest
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

// TODO: Add qualifiers
@RunWith(RobolectricTestRunner::class)
@Config(
    application = KoinTestApp::class,
    sdk = [30],
)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class SpeakerDetailsScTest : KoinTest {

    @get:Rule
    val screenshotTestRule = createScreenshotTestRule()

    @Test
    fun `GIVEN SpeakerDetailsView SHOULD show consistent UI`() {
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
        screenshotTestRule.takeScreenshot {
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