package dev.johnoreilly.confetti

import androidx.compose.ui.test.onNodeWithText
import dev.johnoreilly.confetti.di.KoinTestApp
import dev.johnoreilly.confetti.fragment.SpeakerDetails
import dev.johnoreilly.confetti.speakerdetails.SpeakerDetailsView
import dev.johnoreilly.confetti.test.screenshot.ScreenshotTestRule
import dev.johnoreilly.confetti.test.screenshot.ScreenshotTestRuleImpl
import dev.johnoreilly.confetti.test.screenshot.createScreenshotTestRule
import dev.johnoreilly.confetti.ui.ConfettiTheme
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.After
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin
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

    //@get:Rule
    //val screenshotTestRule = createScreenshotTestRule(record = true)

    @get:Rule
    val screenshotTestRule = ScreenshotTestRuleImpl(record = true, tolerance = 0.1f)

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `GIVEN SpeakerDetailsView SHOULD show consistent UI`() {
        System.out.println("Starting test")
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

    @After
    fun stop() {
        stopKoin()
    }

}