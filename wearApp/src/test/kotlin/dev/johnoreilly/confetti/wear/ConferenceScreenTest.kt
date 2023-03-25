@file:OptIn(ExperimentalHorologistApi::class)
@file:Suppress("UnstableApiUsage")

package dev.johnoreilly.confetti.wear

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithText
import com.google.android.horologist.annotations.ExperimentalHorologistApi
import dev.johnoreilly.confetti.wear.TestFixtures.conferences
import dev.johnoreilly.confetti.wear.conferences.ConferencesUiState
import dev.johnoreilly.confetti.wear.conferences.ConferencesView
import org.junit.Test

class ConferenceScreenTest : ScreenshotTest() {
    init {
        tolerance = 0.03f
    }

    @Test
    fun conferencesScreen() {
        takeScrollableScreenshot (
            timeTextMode = TimeTextMode.OnTop,
            checks = { columnState ->
                rule.onNodeWithText("KotlinConf 2023").assertIsDisplayed()
            }
        ) { columnState ->
            ConferencesView(
                uiState = ConferencesUiState.Success(
                    conferences
                ),
                navigateToConference = {},
                columnState = columnState
            )
        }
    }
}