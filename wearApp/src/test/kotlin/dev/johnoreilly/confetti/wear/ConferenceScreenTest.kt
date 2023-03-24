@file:OptIn(ExperimentalHorologistApi::class)
@file:Suppress("UnstableApiUsage")

package dev.johnoreilly.confetti.wear

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithText
import com.google.android.horologist.compose.layout.ScalingLazyColumnDefaults
import com.google.android.horologist.annotations.ExperimentalHorologistApi
import dev.johnoreilly.confetti.GetConferencesQuery
import dev.johnoreilly.confetti.wear.conferences.ConferencesUiState
import dev.johnoreilly.confetti.wear.conferences.ConferencesView
import org.junit.Test

class ConferenceScreenTest : ScreenshotTest() {
    @Test
    fun conferencesScreen() = takeScreenshot(
        checks = {
            rule.onNodeWithText("FrenchKit 2022").assertIsDisplayed()
        }
    ) {
        ConferencesView(
            uiState = ConferencesUiState.Success(
                listOf(
                    GetConferencesQuery.Conference("0", emptyList(), "Droidcon San Francisco 2022"),
                    GetConferencesQuery.Conference("1", emptyList(), "FrenchKit 2022"),
                    GetConferencesQuery.Conference("2", emptyList(), "Droidcon London 2022"),
                    GetConferencesQuery.Conference("3", emptyList(), "DevFest Ukraine 2023"),
                )
            ),
            navigateToConference = {},
            columnState = ScalingLazyColumnDefaults.belowTimeText().create()
        )
    }
}