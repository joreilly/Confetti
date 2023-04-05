@file:OptIn(ExperimentalHorologistApi::class)
@file:Suppress("UnstableApiUsage")

package dev.johnoreilly.confetti.wear

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithText
import com.google.android.horologist.annotations.ExperimentalHorologistApi
import dev.johnoreilly.confetti.navigation.ConferenceDayKey
import dev.johnoreilly.confetti.wear.preview.TestFixtures.sessionDetails
import dev.johnoreilly.confetti.wear.preview.TestFixtures.sessionTime
import dev.johnoreilly.confetti.wear.screenshots.ScreenshotTest
import dev.johnoreilly.confetti.wear.sessions.SessionListView
import dev.johnoreilly.confetti.wear.sessions.SessionsUiState
import org.junit.Test

class SessionsScreenTest : ScreenshotTest() {
    init {
        tolerance = 0.03f
    }

    @Test
    fun sessionsScreen() = takeScrollableScreenshot(
        timeTextMode = TimeTextMode.OnTop,
        checks = { columnState ->
            rule.onNodeWithText("Thursday 14:00").assertIsDisplayed()
        }
    ) { columnState ->
        SessionListView(
            uiState = SessionsUiState.Success(
                ConferenceDayKey("wearconf", sessionTime.date),
                sessionsByTime = listOf(
                    SessionsUiState.SessionAtTime(
                        sessionTime,
                        listOf(sessionDetails)
                    )
                )
            ),
            sessionSelected = {},
            columnState = columnState
        )
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
            SessionListView(
                uiState = SessionsUiState.Success(
                    ConferenceDayKey("wearconf", sessionTime.date),
                    sessionsByTime = listOf(
                        SessionsUiState.SessionAtTime(
                            sessionTime,
                            listOf(sessionDetails)
                        )
                    )
                ),
                sessionSelected = {},
                columnState = columnState
            )
        }
    }
}