@file:Suppress("UnstableApiUsage")

package dev.johnoreilly.confetti.wear

import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.wear.compose.foundation.lazy.TransformingLazyColumnState
import dev.johnoreilly.confetti.auth.DefaultUser
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
                    uiState = SettingsUiState.Success(),
                    conferenceCleared = { },
                    onSignIn = { },
                    onSignOut = { },
                    onRefreshClick = {},
                    onEnableDeveloperMode = {},
                    updatePreferences = {}
                )
            }
        }
        takeScreenshot()

        columnState.dispatchRawDelta(1000f)
        takeScreenshot("_end")
    }

    @Test
    fun loggedInSettings() {
        val columnState = TransformingLazyColumnState()

        composeRule.setContent {
            TestScaffold {
                SettingsListView(
                    columnState = columnState,
                    uiState = SettingsUiState.Success(
                        authUser = DefaultUser(
                            name = "John O'Reilly",
                            photoUrl = JohnUrl,
                            email = null,
                            user_ = null,
                            uid = "uid"
                        )
                    ),
                    conferenceCleared = { },
                    onSignIn = { },
                    onSignOut = { },
                    onRefreshClick = {},
                    onEnableDeveloperMode = {},
                    updatePreferences = {}
                )
            }
        }
        takeScreenshot()

        columnState.dispatchRawDelta(1000f)
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
                    uiState = SettingsUiState.Success(
                        authUser = DefaultUser(
                            name = "John O'Reilly",
                            photoUrl = JohnUrl,
                            email = null,
                            user_ = null,
                            uid = "uid"
                        )
                    ),
                    conferenceCleared = { },
                    onSignIn = { },
                    onSignOut = { },
                    onRefreshClick = {},
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