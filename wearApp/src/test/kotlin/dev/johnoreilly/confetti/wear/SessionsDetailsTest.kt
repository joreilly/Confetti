@file:OptIn(ExperimentalHorologistApi::class)
@file:Suppress("UnstableApiUsage")

package dev.johnoreilly.confetti.wear

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithText
import com.google.android.horologist.annotations.ExperimentalHorologistApi
import com.google.android.horologist.compose.layout.ScalingLazyColumnDefaults
import dev.johnoreilly.confetti.navigation.SessionDetailsKey
import dev.johnoreilly.confetti.utils.AndroidDateService
import dev.johnoreilly.confetti.wear.TestFixtures.sessionDetails
import dev.johnoreilly.confetti.wear.sessiondetails.SessionDetailView
import dev.johnoreilly.confetti.wear.sessiondetails.SessionDetailsUiState
import kotlinx.datetime.TimeZone
import org.junit.Test

class SessionsDetailsTest : ScreenshotTest() {
    @Test
    fun sessionDetailsScreen() = takeScreenshot(
        checks = {
            rule.onNodeWithText("Thursday 14:00").assertIsDisplayed()
        }
    ) {
        SessionDetailView(
            uiState = SessionDetailsUiState.Success(
                "wearconf",
                SessionDetailsKey("fosdem", "14997"),
                sessionDetails,
                TimeZone.UTC
            ),
            columnState = ScalingLazyColumnDefaults.belowTimeText().create(),
            navigateToSpeaker = {},
            formatter = { AndroidDateService().format(it, TimeZone.UTC, "eeee HH:mm") }
        )
    }
}