@file:OptIn(ExperimentalHorologistApi::class)
@file:Suppress("UnstableApiUsage")

package dev.johnoreilly.confetti.wear

import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertHasNoClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTouchHeightIsEqualTo
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.unit.dp
import com.google.android.horologist.annotations.ExperimentalHorologistApi
import dev.johnoreilly.confetti.wear.preview.TestFixtures.conferences
import dev.johnoreilly.confetti.wear.conferences.ConferencesUiState
import dev.johnoreilly.confetti.wear.conferences.ConferencesView
import dev.johnoreilly.confetti.wear.screenshots.ScreenshotTest
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

    @Test
    fun conferencesScreenA11y() {
        enableA11yTest()

        takeScrollableScreenshot(
            timeTextMode = TimeTextMode.OnTop,
            checks = { columnState ->
                rule.onNodeWithText("Conferences")
                    .assertIsDisplayed()
                    .assertHasNoClickAction()

                rule.onNodeWithText("KotlinConf 2023")
                    .assertIsDisplayed()
                    .assertHasClickAction()
                    .assertTouchHeightIsEqualTo(52.dp)
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