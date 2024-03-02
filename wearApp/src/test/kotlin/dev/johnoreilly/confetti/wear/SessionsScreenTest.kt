@file:Suppress("UnstableApiUsage")

package dev.johnoreilly.confetti.wear

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasScrollToIndexAction
import androidx.compose.ui.test.onNodeWithText
import androidx.wear.compose.material.MaterialTheme
import com.google.android.horologist.compose.layout.AppScaffold
import com.google.android.horologist.compose.layout.rememberResponsiveColumnState
import dev.johnoreilly.confetti.decompose.SessionsUiState
import dev.johnoreilly.confetti.wear.preview.TestFixtures
import dev.johnoreilly.confetti.wear.screenshots.BaseScreenshotTest
import dev.johnoreilly.confetti.wear.screenshots.TestScaffold
import dev.johnoreilly.confetti.wear.sessions.SessionsScreen
import kotlinx.datetime.toKotlinLocalDateTime
import org.junit.Test
import java.time.LocalDateTime

class SessionsScreenTest : BaseScreenshotTest() {
    init {
        tolerance = 0.03f
    }

    val uiState = SessionsUiState.Success(
        LocalDateTime.of(2022, 1, 1, 1, 1).toKotlinLocalDateTime(),
        "wearconf",
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
        composeRule.setContent {
            TestScaffold {
                SessionsScreen(
                    uiState = uiState,
                    sessionSelected = {}
                )
            }
        }
        takeScreenshot()
        composeRule.onNode(hasScrollToIndexAction())
            .scrollToBottom()
        takeScreenshot("_end")
        composeRule.onNodeWithText("Thursday 14:00").assertIsDisplayed()
    }

    @Test
    fun sessionsScreenA11y() {
        enableA11yTest()

        composeRule.setContent {
            TestScaffold {
                SessionsScreen(
                    uiState = uiState,
                    sessionSelected = {},
                )
            }
        }
        takeScreenshot()
        composeRule.onNode(hasScrollToIndexAction())
            .scrollToBottom()
        takeScreenshot("_end")
        composeRule.onNodeWithText("Thursday 14:00").assertIsDisplayed()
    }
}