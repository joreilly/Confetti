@file:OptIn(ExperimentalHorologistApi::class)
@file:Suppress("UnstableApiUsage")

package dev.johnoreilly.confetti.wear

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithText
import com.google.android.horologist.annotations.ExperimentalHorologistApi
import dev.johnoreilly.confetti.navigation.ConferenceDayKey
import dev.johnoreilly.confetti.wear.TestFixtures.sessionDetails
import dev.johnoreilly.confetti.wear.TestFixtures.sessionTime
import dev.johnoreilly.confetti.wear.sessions.SessionListView
import dev.johnoreilly.confetti.wear.sessions.SessionsUiState
import org.junit.Test

class SessionsScreenTest : ScreenshotTest() {

    @Test
    fun sessionsScreen() = takeScrollableScreenshot(
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