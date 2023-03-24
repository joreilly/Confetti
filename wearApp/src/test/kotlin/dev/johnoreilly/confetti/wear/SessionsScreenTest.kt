@file:OptIn(ExperimentalHorologistApi::class)
@file:Suppress("UnstableApiUsage")

package dev.johnoreilly.confetti.wear

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithText
import com.google.android.horologist.compose.layout.ScalingLazyColumnDefaults
import com.google.android.horologist.annotations.ExperimentalHorologistApi
import dev.johnoreilly.confetti.fragment.SessionDetails
import dev.johnoreilly.confetti.navigation.ConferenceDayKey
import dev.johnoreilly.confetti.type.Session
import dev.johnoreilly.confetti.wear.sessions.SessionListView
import dev.johnoreilly.confetti.wear.sessions.SessionsUiState
import kotlinx.datetime.LocalDateTime
import org.junit.Test

class SessionsScreenTest : ScreenshotTest() {
    @Test
    fun sessionsScreen() = takeScreenshot(
        checks = {
            rule.onNodeWithText("Sunday 12:30").assertIsDisplayed()
        }
    ) {
        val sessionTime = LocalDateTime(2022, 12, 25, 12, 30)

        SessionListView(
            uiState = SessionsUiState.Success(
                ConferenceDayKey("wearconf", sessionTime.date),
                sessionsByTime = listOf(
                    SessionsUiState.SessionAtTime(
                        sessionTime,
                        listOf(
                            SessionDetails(
                                "1",
                                "Wear it's at",
                                "Talk",
                                sessionTime,
                                sessionTime,
                                "Be aWear of what's coming",
                                "en",
                                listOf(),
                                SessionDetails.Room("Main Hall"),
                                listOf(),
                                Session.type.name
                            )
                        )
                    )
                )
            ),
            sessionSelected = {},
            columnState = ScalingLazyColumnDefaults.belowTimeText().create()
        )
    }
}