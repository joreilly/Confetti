@file:Suppress("UnstableApiUsage")

package dev.johnoreilly.confetti.wear

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertHasNoClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTouchHeightIsEqualTo
import androidx.compose.ui.test.hasScrollToIndexAction
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.MaterialTheme
import com.google.android.horologist.compose.layout.AppScaffold
import com.google.android.horologist.compose.layout.rememberResponsiveColumnState
import dev.johnoreilly.confetti.decompose.ConferencesComponent
import dev.johnoreilly.confetti.wear.conferences.ConferencesView
import dev.johnoreilly.confetti.wear.preview.TestFixtures.conferences
import dev.johnoreilly.confetti.wear.screenshots.BaseScreenshotTest
import org.junit.Test

class ConferenceScreenTest : BaseScreenshotTest() {
    init {
        tolerance = 0.03f
    }

    @Test
    fun conferencesScreen() {
        composeRule.setContent {
            AppScaffold(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colors.background)
            ) {
                ConferencesView(
                    uiState = ConferencesComponent.Success(
                        conferences.groupBy { it.days.first().year }
                    ),
                    navigateToConference = {},
                    columnState = rememberResponsiveColumnState()
                )
            }
        }
        composeRule.onNodeWithText("KotlinConf 2023").assertIsDisplayed()
        takeScreenshot()
        composeRule.onNode(hasScrollToIndexAction())
            .scrollToBottom()
        takeScreenshot("_end")
    }

    @Test
    fun conferencesScreenA11y() {
        enableA11yTest()

        composeRule.setContent {
            AppScaffold(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colors.background)
            ) {
                ConferencesView(
                    uiState = ConferencesComponent.Success(
                        conferences.groupBy { it.days.first().year }
                    ),
                    navigateToConference = {},
                    columnState = rememberResponsiveColumnState()
                )
            }
        }

        composeRule.onNodeWithText("KotlinConf 2023")
            .assertIsDisplayed()
            // TODO https://github.com/google/horologist/issues/2039
//                    .assertHasClickAction()
            .assertTouchHeightIsEqualTo(52.dp)

        takeScreenshot()
        composeRule.onNode(hasScrollToIndexAction())
            .scrollToBottom()
        takeScreenshot("_end")
        composeRule.onNodeWithText("Conferences")
            .assertIsDisplayed()
            .assertHasNoClickAction()
    }
}