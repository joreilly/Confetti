@file:OptIn(ExperimentalHorologistApi::class)
@file:Suppress("UnstableApiUsage")

package dev.johnoreilly.confetti.wear

import androidx.compose.ui.test.assertAll
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertHasNoClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTouchHeightIsEqualTo
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.isNotEnabled
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.printToLog
import androidx.compose.ui.test.printToString
import androidx.compose.ui.unit.dp
import com.google.android.horologist.annotations.ExperimentalHorologistApi
import dev.johnoreilly.confetti.wear.home.HomeListView
import dev.johnoreilly.confetti.wear.home.HomeUiState
import dev.johnoreilly.confetti.wear.preview.TestFixtures.kotlinConf2023
import dev.johnoreilly.confetti.wear.screenshots.ScreenshotTest
import org.junit.Test

class ConferenceHomeScreenTest : ScreenshotTest() {
    init {
        tolerance = 0.03f
    }

    @Test
    fun conferenceHomeScreen() {
        takeScrollableScreenshot(
            timeTextMode = TimeTextMode.OnTop,
            checks = { _ ->
                rule.onNodeWithText("KotlinConf 2023").assertIsDisplayed()
            }
        ) { columnState ->
            HomeListView(
                uiState = HomeUiState.Success(
                    kotlinConf2023.id,
                    kotlinConf2023.name,
                    kotlinConf2023.days,
                    listOf()
                ),
                sessionSelected = {},
                daySelected = {},
                onSettingsClick = {},
                onRefreshClick = {},
                columnState = columnState
            )
        }
    }

    @Test
    fun conferenceHomeScreenA11y() {
        enableA11yTest()

        takeScrollableScreenshot(
            timeTextMode = TimeTextMode.OnTop,
            checks = { _ ->
                rule.onNodeWithText("KotlinConf 2023")
                    .assertIsDisplayed()
                    .assertHasNoClickAction()

                rule.onNodeWithText("Wednesday")
                    .assertIsDisplayed()
                    .assertHasClickAction()
                    .assertTouchHeightIsEqualTo(52.dp)
            }
        ) { columnState ->
            HomeListView(
                uiState = HomeUiState.Success(
                    kotlinConf2023.id,
                    kotlinConf2023.name,
                    kotlinConf2023.days,
                    listOf()
                ),
                sessionSelected = {},
                daySelected = {},
                onSettingsClick = {},
                onRefreshClick = {},
                columnState = columnState
            )
        }
    }

    @Test
    fun conferenceHomeScreenLoading() {
        // Placeholders are not stable
        tolerance = 1.0f

        takeScrollableScreenshot(
            timeTextMode = TimeTextMode.OnTop,
            checks = { _ ->
                rule.onNodeWithText("Conference Days")
                    .assertIsDisplayed()
            }
        ) { columnState ->
            HomeListView(
                uiState = HomeUiState.Loading,
                sessionSelected = {},
                daySelected = {},
                onSettingsClick = {},
                onRefreshClick = {},
                columnState = columnState
            )
        }
    }

    @Test
    fun conferenceHomeScreenLoadingA11y() {
        // Placeholders are not stable
        tolerance = 1.0f

        enableA11yTest()

        takeScrollableScreenshot(
            timeTextMode = TimeTextMode.OnTop,
            checks = { _ ->
                rule.onNodeWithText("Conference Days")
                    .assertIsDisplayed()
                    .assertHasNoClickAction()

                rule.onAllNodesWithContentDescription("")
                    .assertCountEquals(2)
                    .assertAll(hasClickAction())
                    .assertAll(isNotEnabled())
            }
        ) { columnState ->
            HomeListView(
                uiState = HomeUiState.Loading,
                sessionSelected = {},
                daySelected = {},
                onSettingsClick = {},
                onRefreshClick = {},
                columnState = columnState
            )
        }
    }
}