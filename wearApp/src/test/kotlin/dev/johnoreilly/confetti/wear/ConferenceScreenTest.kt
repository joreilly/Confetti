@file:OptIn(ExperimentalHorologistComposeLayoutApi::class)
@file:Suppress("UnstableApiUsage")

package dev.johnoreilly.confetti.wear

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithText
import com.google.android.horologist.compose.layout.ScalingLazyColumnDefaults
import com.google.android.horologist.compose.navscaffold.ExperimentalHorologistComposeLayoutApi
import dev.johnoreilly.confetti.GetConferencesQuery
import dev.johnoreilly.confetti.wear.TestFixtures.conferences
import dev.johnoreilly.confetti.wear.conferences.ConferencesUiState
import dev.johnoreilly.confetti.wear.conferences.ConferencesView
import org.junit.Test

class ConferenceScreenTest : ScreenshotTest() {
    @Test
    fun conferencesScreen() {
        takeScreenshot(
            checks = {
                rule.onNodeWithText("KotlinConf 2023").assertIsDisplayed()
            }
        ) {
            ConferencesView(
                uiState = ConferencesUiState.Success(
                    conferences
                ),
                navigateToConference = {},
                columnState = ScalingLazyColumnDefaults.belowTimeText().create()
            )
        }
    }
}