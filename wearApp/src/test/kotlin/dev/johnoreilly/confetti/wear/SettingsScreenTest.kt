@file:Suppress("UnstableApiUsage")

package dev.johnoreilly.confetti.wear

import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.wear.compose.foundation.lazy.TransformingLazyColumnState
import com.google.android.horologist.auth.data.common.model.AuthUser
import dev.johnoreilly.confetti.wear.preview.TestFixtures.JohnUrl
import dev.johnoreilly.confetti.wear.screenshots.BaseScreenshotTest
import dev.johnoreilly.confetti.wear.screenshots.WearDevice
import dev.johnoreilly.confetti.wear.settings.SettingsListView
import dev.johnoreilly.confetti.wear.settings.SettingsUiState
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.ParameterizedRobolectricTestRunner

@RunWith(ParameterizedRobolectricTestRunner::class)
class SettingsScreenTest(override val device: WearDevice) : BaseScreenshotTest() {
    init {
        tolerance = 0.05f
    }

    @Test
    fun loggedOutSettings() {
        val columnState = TransformingLazyColumnState()

        composeRule.setContent {
            TestScaffold {
                SettingsListView(
                    columnState = columnState,
                    uiState = SettingsUiState.Success(null),
                    conferenceCleared = { },
                    navigateToGoogleSignIn = { },
                    navigateToGoogleSignOut = { },
                    onRefreshClick = {},
                    onRefreshToken = {},
                    onEnableDeveloperMode = {},
                    updatePreferences = {}
                )
            }
        }
        takeScreenshot()

        columnState.requestScrollToItem(20, 0)
        takeScreenshot("_end")
    }

    @Test
    fun loggedInSettings() {
        val columnState = TransformingLazyColumnState()

        composeRule.setContent {
            TestScaffold {
                SettingsListView(
                    columnState = columnState,
                    uiState = SettingsUiState.Success(AuthUser("John O'Reilly", avatarUri = JohnUrl)),
                    conferenceCleared = { },
                    navigateToGoogleSignIn = { },
                    navigateToGoogleSignOut = { },
                    onRefreshClick = {},
                    onRefreshToken = {},
                    onEnableDeveloperMode = {},
                    updatePreferences = {}
                )
            }
        }
        takeScreenshot()

        columnState.requestScrollToItem(20, 0)
        takeScreenshot("_end")
    }

    @Test
    fun loggedInSettingsA11y() {
        // allow more tolerance as A11y tests are mainly for illustrating the
        // current observable behaviour
        tolerance = 0.10f

        enableA11yTest()

        composeRule.setContent {
            TestScaffold {
                SettingsListView(
                    uiState = SettingsUiState.Success(AuthUser("John O'Reilly", avatarUri = JohnUrl)),
                    conferenceCleared = { },
                    navigateToGoogleSignIn = { },
                    navigateToGoogleSignOut = { },
                    onRefreshClick = {},
                    onRefreshToken = {},
                    onEnableDeveloperMode = {},
                    updatePreferences = {}
                )
            }
        }
        takeScreenshot()

        scrollToBottom()
        takeScreenshot("_end")
        composeRule.onNodeWithContentDescription("Logged in as John O'Reilly")
            .assertHasClickAction()
            .assertIsDisplayed()
    }
}