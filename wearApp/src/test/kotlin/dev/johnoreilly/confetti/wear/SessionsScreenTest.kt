@file:Suppress("UnstableApiUsage")

package dev.johnoreilly.confetti.wear

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithText
import androidx.wear.compose.foundation.lazy.TransformingLazyColumnState
import dev.johnoreilly.confetti.decompose.SessionsUiState
import dev.johnoreilly.confetti.wear.preview.TestFixtures
import dev.johnoreilly.confetti.wear.screenshots.BaseScreenshotTest
import dev.johnoreilly.confetti.wear.screenshots.WearDevice
import dev.johnoreilly.confetti.wear.sessions.SessionsScreen
import kotlinx.datetime.toKotlinLocalDateTime
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.ParameterizedRobolectricTestRunner
import java.time.LocalDateTime

@RunWith(ParameterizedRobolectricTestRunner::class)
class SessionsScreenTest(override val device: WearDevice) : BaseScreenshotTest() {
    init {
        tolerance = 0.03f
    }

    val uiState = SessionsUiState.Success(
        LocalDateTime.of(2022, 1, 1, 1, 1).toKotlinLocalDateTime(),
        "wearconf", "wearconf",
        null,
        null,
        listOf(),
        listOf(),
        listOf(
            mapOf(
                "Thursday 14:00" to listOf(
                    TestFixtures.sessionDetails
                )
            )
        ),
        listOf(),
        listOf(),
        setOf(),
        false, "",
        null
    )

    @Test
    fun sessionsScreen() {
        val columnState = TransformingLazyColumnState()

        composeRule.setContent {
            TestScaffold {
                SessionsScreen(
                    columnState = columnState,
                    uiState = uiState,
                    sessionSelected = {},
                    addBookmark = {},
                    removeBookmark = {}
                )
            }
        }
        takeScreenshot()

        columnState.requestScrollToItem(20, 0)
        takeScreenshot("_end")
    }

    @Test
    fun sessionsScreenA11y() {
        enableA11yTest()

        composeRule.setContent {
            TestScaffold {
                SessionsScreen(
                    uiState = uiState,
                    sessionSelected = {},
                    addBookmark = {},
                    removeBookmark = {}
                )
            }
        }
        takeScreenshot()

        scrollToBottom()
        takeScreenshot("_end")
        composeRule.onNodeWithText("Thursday 14:00").assertIsDisplayed()
    }
}