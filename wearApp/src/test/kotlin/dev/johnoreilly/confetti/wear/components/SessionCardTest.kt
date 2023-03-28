@file:Suppress("UnstableApiUsage")

package dev.johnoreilly.confetti.wear.components

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithText
import dev.johnoreilly.confetti.wear.preview.TestFixtures
import dev.johnoreilly.confetti.wear.screenshots.ScreenshotTest
import org.junit.Test

class SessionCardTest : ScreenshotTest() {
    init {
        tolerance = 0.05f
    }

    @Test
    fun speakerChip() {
        takeComponentScreenshot(
            checks = {
                rule.onNodeWithText(TestFixtures.sessionDetails.title).assertIsDisplayed()
            }
        ) {
            SessionCard(
                session = TestFixtures.sessionDetails,
                sessionSelected = {}
            )
        }
    }
}