@file:Suppress("UnstableApiUsage")

package dev.johnoreilly.confetti.wear

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithText
import androidx.wear.compose.foundation.lazy.TransformingLazyColumnState
import dev.johnoreilly.confetti.decompose.SessionDetailsUiState
import dev.johnoreilly.confetti.wear.preview.TestFixtures.conference
import dev.johnoreilly.confetti.wear.preview.TestFixtures.sessionDetails
import dev.johnoreilly.confetti.wear.screenshots.BaseScreenshotTest
import dev.johnoreilly.confetti.wear.screenshots.WearDevice
import dev.johnoreilly.confetti.wear.sessiondetails.SessionDetailView
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * Session details is a scrolling detail view — the layout behaviour is
 * adequately covered by a single large-round device. The list screens
 * upstream ([SessionsScreenTest]) exercise font-scale and small-round
 * variants for us.
 */
@RunWith(RobolectricTestRunner::class)
class SessionsDetailsTest : BaseScreenshotTest() {
    init {
        tolerance = 0.02f
    }

    override val device: WearDevice = WearDevice.GenericLargeRound

    val uiState = SessionDetailsUiState.Success(
        conference, sessionDetails
    )

    @Test
    fun sessionDetailsScreen() {
        val columnState = TransformingLazyColumnState()

        composeRule.setContent {
            TestScaffold {
                SessionDetailView(
                    columnState = columnState,
                    uiState = uiState,
                    navigateToSpeaker = {},
                )
            }
        }
        composeRule.onNodeWithText("Thursday 14:00").assertIsDisplayed()
        takeScreenshot()

        columnState.requestScrollToItem(20, 0)
        takeScreenshot("_end")
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

        scrollToBottom()
        takeScreenshot("_end")
    }
}