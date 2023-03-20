@file:OptIn(ExperimentalHorologistComposeLayoutApi::class)
@file:Suppress("UnstableApiUsage")

package dev.johnoreilly.confetti.wear

import com.google.android.horologist.compose.layout.ScalingLazyColumnDefaults
import com.google.android.horologist.compose.navscaffold.ExperimentalHorologistComposeLayoutApi
import dev.johnoreilly.confetti.GetConferencesQuery
import dev.johnoreilly.confetti.wear.conferences.ConferencesUiState
import dev.johnoreilly.confetti.wear.conferences.ConferencesView
import org.junit.Test

class ConferenceScreenTest : ScreenshotTest() {
    @Test
    fun conferencesScreen() = takeScreenshot {
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
            // TODO switch back to belowTimeText when screen size is correct
            columnState = ScalingLazyColumnDefaults.scalingLazyColumnDefaults().create()
        )
    }
}