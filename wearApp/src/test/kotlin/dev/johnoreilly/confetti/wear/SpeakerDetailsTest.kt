@file:Suppress("UnstableApiUsage")

package dev.johnoreilly.confetti.wear

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasScrollToIndexAction
import androidx.compose.ui.test.onNodeWithText
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
        composeRule.setContent {
            TestScaffold {
                SpeakerDetailsView(
                    uiState = SpeakerDetailsUiState.Success(JohnOreilly.speakerDetails),
                )
            }
        }
        composeRule.onNodeWithText("John O'Reilly").assertIsDisplayed()
        takeScreenshot()
        composeRule.onNode(hasScrollToIndexAction())
            .scrollToBottom()
        takeScreenshot("_end")
    }

    @Test
    fun speakerDetailsScreenA11y() {
        enableA11yTest()

        composeRule.setContent {
            TestScaffold {
                SpeakerDetailsView(
                    uiState = SpeakerDetailsUiState.Success(JohnOreilly.speakerDetails),
                )
            }
        }
        composeRule.onNodeWithText("John O'Reilly").assertIsDisplayed()
        takeScreenshot()
        composeRule.onNode(hasScrollToIndexAction())
            .scrollToBottom()
        takeScreenshot("_end")
    }
}