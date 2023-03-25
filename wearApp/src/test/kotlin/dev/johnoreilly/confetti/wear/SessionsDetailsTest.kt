@file:OptIn(ExperimentalHorologistApi::class)
@file:Suppress("UnstableApiUsage")

package dev.johnoreilly.confetti.wear

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithText
import com.google.android.horologist.annotations.ExperimentalHorologistApi
import dev.johnoreilly.confetti.navigation.SessionDetailsKey
import dev.johnoreilly.confetti.utils.AndroidDateService
import dev.johnoreilly.confetti.wear.TestFixtures.sessionDetails
import dev.johnoreilly.confetti.wear.sessiondetails.SessionDetailView
import dev.johnoreilly.confetti.wear.sessiondetails.SessionDetailsUiState
import kotlinx.datetime.TimeZone
import org.junit.Test
import kotlin.test.assertEquals

class SessionsDetailsTest : ScreenshotTest() {
    @Test
    fun sessionDetailsScreen() = takeScrollableScreenshot(
        checks = {
            rule.onNodeWithText("Thursday 14:00").assertIsDisplayed()
        }
    ) { columnState ->
        SessionDetailView(
            uiState = SessionDetailsUiState.Success(
                "wearconf",
                SessionDetailsKey("fosdem", "14997"),
                sessionDetails,
                TimeZone.UTC
            ),
            navigateToSpeaker = {},
            columnState = columnState,
            formatter = { AndroidDateService().format(it, TimeZone.UTC, "eeee HH:mm") }
        )
    }

    @Test
    fun sessionDetailsScreenEnd() = takeScrollableScreenshot(
        checks = { columnState ->
            columnState.state.scrollToItem(100)
            rule.onNodeWithText("Martin Bonnin").assertIsDisplayed()
            assertEquals(6, columnState.state.centerItemIndex)
        }
    ) { columnState ->
        SessionDetailView(
            uiState = SessionDetailsUiState.Success(
                "wearconf",
                SessionDetailsKey("fosdem", "14997"),
                sessionDetails,
                TimeZone.UTC
            ),
            navigateToSpeaker = {},
            columnState = columnState,
            formatter = { AndroidDateService().format(it, TimeZone.UTC, "eeee HH:mm") }
        )
    }
}