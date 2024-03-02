@file:Suppress("UnstableApiUsage")

package dev.johnoreilly.confetti.wear

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasScrollToIndexAction
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.core.graphics.drawable.toDrawable
import androidx.wear.compose.material.MaterialTheme
import coil.decode.DataSource
import coil.request.SuccessResult
import com.google.android.horologist.auth.data.common.model.AuthUser
import com.google.android.horologist.compose.layout.AppScaffold
import com.google.android.horologist.compose.layout.rememberResponsiveColumnState
import com.google.android.horologist.images.coil.FakeImageLoader
import dev.johnoreilly.confetti.wear.preview.TestFixtures.JohnUrl
import dev.johnoreilly.confetti.wear.screenshots.BaseScreenshotTest
import dev.johnoreilly.confetti.wear.settings.SettingsListView
import dev.johnoreilly.confetti.wear.settings.SettingsUiState
import okio.Path.Companion.toPath
import org.junit.Before
import org.junit.Test

class SettingsScreenTest : BaseScreenshotTest() {
    init {
        tolerance = 0.05f
    }

    @Before
    fun loadImages() {
        val johnBitmap = loadTestBitmap("john.jpg".toPath())

        fakeImageLoader = FakeImageLoader {
            SuccessResult(
                drawable = johnBitmap.toDrawable(resources),
                dataSource = DataSource.MEMORY,
                request = it
            )
        }
    }

    @Test
    fun loggedOutSettings() {
        composeRule.setContent {
            AppScaffold(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colors.background)
            ) {
                SettingsListView(
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
        composeRule.onNode(hasScrollToIndexAction())
            .scrollToBottom()
        takeScreenshot("_end")
        composeRule.onNodeWithText("Sign In").assertIsDisplayed()
    }

    @Test
    fun loggedInSettings() {
        composeRule.setContent {
            AppScaffold(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colors.background)
            ) {
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
        composeRule.onNode(hasScrollToIndexAction())
            .scrollToBottom()
        takeScreenshot("_end")
        composeRule.onNodeWithContentDescription("Logged in as John O'Reilly")
            .assertHasClickAction()
            .assertIsDisplayed()
    }

    @Test
    fun loggedInSettingsA11y() {
        // allow more tolerance as A11y tests are mainly for illustrating the
        // current observable behaviour
        tolerance = 0.10f

        enableA11yTest()

        composeRule.setContent {
            AppScaffold(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colors.background)
            ) {
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
        composeRule.onNode(hasScrollToIndexAction())
            .scrollToBottom()
        takeScreenshot("_end")
        composeRule.onNodeWithContentDescription("Logged in as John O'Reilly")
            .assertHasClickAction()
            .assertIsDisplayed()
    }
}