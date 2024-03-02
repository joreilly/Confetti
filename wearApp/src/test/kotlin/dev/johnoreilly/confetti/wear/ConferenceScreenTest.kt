
@file:Suppress("UnstableApiUsage")

package dev.johnoreilly.confetti.wear

import androidx.compose.ui.test.assertHasNoClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTouchHeightIsEqualTo
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.unit.dp
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
        runScreenshotTest {
            ConferencesView(
                uiState = ConferencesComponent.Success(
                    conferences.groupBy { it.days.first().year }
                ),
                navigateToConference = {},
                columnState = rememberResponsiveColumnState()
            )
        }
        composeRule.onNodeWithText("KotlinConf 2023").assertIsDisplayed()
    }

    @Test
    fun conferencesScreenA11y() {
        enableA11yTest()

        runScreenshotTest {
            ConferencesView(
                uiState = ConferencesComponent.Success(
                    conferences.groupBy { it.days.first().year }
                ),
                navigateToConference = {},
                columnState = rememberResponsiveColumnState()
            )
        }
        composeRule.onNodeWithText("Conferences")
            .assertIsDisplayed()
            .assertHasNoClickAction()

        composeRule.onNodeWithText("KotlinConf 2023")
            .assertIsDisplayed()
            // TODO https://github.com/google/horologist/issues/2039
//                    .assertHasClickAction()
            .assertTouchHeightIsEqualTo(52.dp)
    }
}