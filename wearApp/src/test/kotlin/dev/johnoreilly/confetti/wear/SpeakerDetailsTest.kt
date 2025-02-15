@file:Suppress("UnstableApiUsage")

package dev.johnoreilly.confetti.wear

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithText
import androidx.wear.compose.foundation.lazy.TransformingLazyColumnState
import dev.johnoreilly.confetti.decompose.SpeakerDetailsUiState
import dev.johnoreilly.confetti.wear.preview.TestFixtures.JohnOreilly
import dev.johnoreilly.confetti.wear.screenshots.BaseScreenshotTest
import dev.johnoreilly.confetti.wear.screenshots.WearDevice
import dev.johnoreilly.confetti.wear.speakerdetails.SpeakerDetailsView
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.ParameterizedRobolectricTestRunner

@RunWith(ParameterizedRobolectricTestRunner::class)
class SpeakerDetailsTest(override val device: WearDevice) : BaseScreenshotTest() {
    init {
        tolerance = 0.02f
    }

    @Test
    fun speakerDetailsScreen() {
        val columnState = TransformingLazyColumnState()

        composeRule.setContent {
            TestScaffold {
                SpeakerDetailsView(
                    columnState = columnState,
                    uiState = SpeakerDetailsUiState.Success("myconf", JohnOreilly.speakerDetails),
                )
            }
        }
        composeRule.onNodeWithText("John O'Reilly").assertIsDisplayed()
        takeScreenshot()

        columnState.dispatchRawDelta(1000f)
        takeScreenshot("_end")
    }

    @Test
    fun speakerDetailsScreenA11y() {
        enableA11yTest()

        composeRule.setContent {
            TestScaffold {
                SpeakerDetailsView(
                    uiState = SpeakerDetailsUiState.Success("myconf", JohnOreilly.speakerDetails),
                )
            }
        }
        composeRule.onNodeWithText("John O'Reilly").assertIsDisplayed()
        takeScreenshot()

        scrollToBottom()
        takeScreenshot("_end")
    }
}