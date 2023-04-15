@file:Suppress("UnstableApiUsage")

package dev.johnoreilly.confetti.wear.components

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithText
import dev.johnoreilly.confetti.wear.preview.TestFixtures
import dev.johnoreilly.confetti.wear.screenshots.BaseScreenshotTest
import kotlinx.datetime.toKotlinLocalDateTime
import org.junit.Test
import java.time.LocalDateTime

class SessionCardTest : BaseScreenshotTest(
    tolerance = 0.05f
) {
    @Test
    fun sessionCardFuture() {
        screenshotTestRule.takeComponentScreenshot(
            checks = { composeRule ->
                composeRule
                    .onNodeWithText(TestFixtures.sessionDetails.title)
                    .assertIsDisplayed()

                screenshotTestRule.composeTestRule
                    .onNodeWithText("2:00 PM")
                    .assertIsDisplayed()
            }
        ) {
            SessionCard(
                session = TestFixtures.sessionDetails,
                sessionSelected = {},
                currentTime = LocalDateTime.of(2022, 1, 1, 1, 1).toKotlinLocalDateTime()
            )
        }
    }

    @Test
    fun sessionCardNow() {
        screenshotTestRule.takeComponentScreenshot(
            checks = {
                screenshotTestRule.composeTestRule
                    .onNodeWithText(TestFixtures.sessionDetails.title)
                    .assertIsDisplayed()

                screenshotTestRule.composeTestRule
                    .onNodeWithText("Now")
                    .assertIsDisplayed()
            }
        ) {
            SessionCard(
                session = TestFixtures.sessionDetails,
                sessionSelected = {},
                currentTime = TestFixtures.sessionDetails.startsAt
            )
        }
    }
}