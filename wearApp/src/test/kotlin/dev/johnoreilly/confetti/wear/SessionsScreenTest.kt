@file:Suppress("UnstableApiUsage")

package dev.johnoreilly.confetti.wear

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithText
import com.google.android.horologist.compose.layout.rememberResponsiveColumnState
import dev.johnoreilly.confetti.decompose.SessionsUiState
import dev.johnoreilly.confetti.wear.preview.TestFixtures
import dev.johnoreilly.confetti.wear.screenshots.BaseScreenshotTest
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
        runScreenshotTest {
            SessionsScreen(
                uiState = uiState,
                columnState = rememberResponsiveColumnState(),
                sessionSelected = {}
            )
        }
        composeRule.onNodeWithText("Thursday 14:00").assertIsDisplayed()
    }

    @Test
    fun sessionsScreenA11y() {
        enableA11yTest()

        runScreenshotTest {
            SessionsScreen(
                uiState = uiState,
                sessionSelected = {},
                columnState = rememberResponsiveColumnState()
            )
        }
        composeRule.onNodeWithText("Thursday 14:00").assertIsDisplayed()
    }
}