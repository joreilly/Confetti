@file:Suppress("UnstableApiUsage")

package dev.johnoreilly.confetti.wear

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithText
import androidx.wear.compose.foundation.rememberExpandableStateMapping
import dev.johnoreilly.confetti.decompose.SessionsUiState
import dev.johnoreilly.confetti.wear.preview.TestFixtures
import dev.johnoreilly.confetti.wear.screenshots.ScreenshotTest
import dev.johnoreilly.confetti.wear.sessions.SessionsScreen
import kotlinx.datetime.toKotlinLocalDateTime
import org.junit.Test
import java.time.LocalDateTime

class SessionsScreenTest : ScreenshotTest() {
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

    init {
        tolerance = 0.03f
    }

    @Test
    fun sessionsScreen() {
        takeScrollableScreenshot(
            timeTextMode = TimeTextMode.OnTop,
            checks = { columnState ->
                rule.onNodeWithText("Thursday 14:00").assertIsDisplayed()
            }
        ) { columnState ->
            SessionsScreen(
                uiState = uiState,
                columnState = columnState,
                sessionSelected = {}
            )
        }
    }

    @Test
    fun sessionsScreenA11y() {
        enableA11yTest()

        takeScrollableScreenshot(
            timeTextMode = TimeTextMode.OnTop,
            checks = { columnState ->
                rule.onNodeWithText("Thursday 14:00").assertIsDisplayed()
            }
        ) { columnState ->
            SessionsScreen(
                uiState = uiState,
                sessionSelected = {},
                columnState = columnState
            )
        }
    }

    @Test
    fun sessionsScreenExpanded() {
        takeScrollableScreenshot(
            timeTextMode = TimeTextMode.OnTop,
            checks = { columnState ->
                rule.onNodeWithText("Thursday 14:00").assertIsDisplayed()
            }
        ) { columnState ->
            SessionsScreen(
                uiState = uiState,
                columnState = columnState,
                sessionSelected = {},
                states = rememberExpandableStateMapping(initiallyExpanded = { true })
            )
        }
    }
}
