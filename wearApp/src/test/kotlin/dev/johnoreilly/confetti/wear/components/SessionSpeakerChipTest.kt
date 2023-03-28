@file:Suppress("UnstableApiUsage")

package dev.johnoreilly.confetti.wear.components

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithText
import androidx.core.graphics.drawable.toDrawable
import coil.decode.DataSource
import coil.request.SuccessResult
import com.google.android.horologist.compose.tools.coil.FakeImageLoader
import dev.johnoreilly.confetti.wear.TestFixtures
import dev.johnoreilly.confetti.wear.screenshots.ScreenshotTest
import okio.Path.Companion.toPath
import org.junit.Test

class SessionSpeakerChipTest : ScreenshotTest() {
    init {
        tolerance = 0.05f
    }

    @Test
    fun speakerChip() {
        val johnBitmap = loadTestBitmap("john.jpg".toPath())

        fakeImageLoader = FakeImageLoader {
            SuccessResult(
                drawable = johnBitmap.toDrawable(resources),
                dataSource = DataSource.MEMORY,
                request = it
            )
        }

        takeComponentScreenshot(
            checks = {
                rule.onNodeWithText("John O'Reilly").assertIsDisplayed()
            }
        ) {
            SessionSpeakerChip(
                conference = "kotlinconf2023",
                speaker = TestFixtures.JohnOreilly.speakerDetails,
                navigateToSpeaker = {}
            )
        }
    }

    @Test
    fun speakerChipWithImageError() {
        fakeImageLoader = FakeImageLoader.NotFound

        takeComponentScreenshot(
            checks = {
                rule.onNodeWithText("John O'Reilly").assertIsDisplayed()
            }
        ) {
            SessionSpeakerChip(
                conference = "kotlinconf2023",
                speaker = TestFixtures.JohnOreilly.speakerDetails,
                navigateToSpeaker = {}
            )
        }
    }
}