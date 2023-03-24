@file:OptIn(ExperimentalHorologistComposeLayoutApi::class)
@file:Suppress("UnstableApiUsage")

package dev.johnoreilly.confetti.wear

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithText
import com.google.android.horologist.compose.layout.ScalingLazyColumnDefaults
import com.google.android.horologist.compose.navscaffold.ExperimentalHorologistComposeLayoutApi
import dev.johnoreilly.confetti.fragment.SessionDetails
import dev.johnoreilly.confetti.navigation.ConferenceDayKey
import dev.johnoreilly.confetti.type.Session
import dev.johnoreilly.confetti.wear.TestFixtures.sessionDetails
import dev.johnoreilly.confetti.wear.TestFixtures.sessionTime
import dev.johnoreilly.confetti.wear.sessions.SessionListView
import dev.johnoreilly.confetti.wear.sessions.SessionsUiState
import kotlinx.datetime.LocalDateTime
import org.junit.Test

class SessionsScreenTest : ScreenshotTest() {
    @Test
    fun sessionsScreen() = takeScreenshot(
        checks = {
            rule.onNodeWithText("Thursday 14:00").assertIsDisplayed()
        }
    ) {
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
            columnState = ScalingLazyColumnDefaults.belowTimeText().create()
        )
    }
}