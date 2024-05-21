@file:Suppress("UnstableApiUsage")

package dev.johnoreilly.confetti.wear

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasScrollToIndexAction
import androidx.compose.ui.test.onNodeWithText
import dev.johnoreilly.confetti.decompose.SessionDetailsUiState
import dev.johnoreilly.confetti.wear.preview.TestFixtures.sessionDetails
import dev.johnoreilly.confetti.wear.screenshots.BaseScreenshotTest
import dev.johnoreilly.confetti.wear.screenshots.WearDevice
import dev.johnoreilly.confetti.wear.sessiondetails.SessionDetailView
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.ParameterizedRobolectricTestRunner

@RunWith(ParameterizedRobolectricTestRunner::class)
class SessionsDetailsTest(override val device: WearDevice) : BaseScreenshotTest() {
    init {
        tolerance = 0.02f
    }


    val uiState = SessionDetailsUiState.Success(
        sessionDetails
    )

    @Test
    fun sessionDetailsScreen() {
        composeRule.setContent {
            TestScaffold {
                SessionDetailView(
                    uiState = uiState,
                    navigateToSpeaker = {},
                )
            }
        }
        composeRule.onNodeWithText("Thursday 14:00").assertIsDisplayed()
        takeScreenshot()

        // Disabled temporarily, hangs roborazzi
//        composeRule.onNode(hasScrollToIndexAction())
//            .scrollToBottom()
//        takeScreenshot("_end")
    }

    @Test
    fun sessionDetailsScreenA11y() {
        enableA11yTest()

        composeRule.setContent {
            TestScaffold {
                SessionDetailView(
                    uiState = uiState,
                    navigateToSpeaker = {},
                )
            }
        }
        composeRule.onNodeWithText("Thursday 14:00").assertIsDisplayed()
        takeScreenshot()

        // Disabled temporarily, hangs roborazzi
//        composeRule.onNode(hasScrollToIndexAction())
//            .scrollToBottom()
//        takeScreenshot("_end")
    }
}