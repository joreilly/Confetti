
@file:Suppress("UnstableApiUsage")

package dev.johnoreilly.confetti.wear

import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.core.graphics.drawable.toDrawable
import coil.decode.DataSource
import coil.request.SuccessResult
import com.google.android.horologist.auth.data.common.model.AuthUser
import com.google.android.horologist.compose.layout.rememberResponsiveColumnState
import com.google.android.horologist.images.coil.FakeImageLoader
import dev.johnoreilly.confetti.wear.preview.TestFixtures.JohnUrl
import dev.johnoreilly.confetti.wear.screenshots.BaseScreenshotTest
import dev.johnoreilly.confetti.wear.settings.SettingsListView
import dev.johnoreilly.confetti.wear.settings.SettingsUiState
import okio.Path.Companion.toPath
import org.junit.Assume.assumeTrue
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
        runScreenshotTest {
            SettingsListView(
                uiState = SettingsUiState.Success(null),
                conferenceCleared = { },
                navigateToGoogleSignIn = { },
                navigateToGoogleSignOut = { },
                columnState = rememberResponsiveColumnState(),
                onRefreshClick = {},
                onRefreshToken = {},
                onEnableDeveloperMode = {},
                updatePreferences = {}
            )
        }
        composeRule.onNodeWithText("Sign In").assertIsDisplayed()
    }

    @Test
    fun loggedInSettings() {
        runScreenshotTest {
            SettingsListView(
                uiState = SettingsUiState.Success(AuthUser("John O'Reilly", avatarUri = JohnUrl)),
                conferenceCleared = { },
                navigateToGoogleSignIn = { },
                navigateToGoogleSignOut = { },
                columnState = rememberResponsiveColumnState(),
                onRefreshClick = {},
                onRefreshToken = {},
                onEnableDeveloperMode = {},
                updatePreferences = {}
            )
        }
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

        runScreenshotTest {
            SettingsListView(
                uiState = SettingsUiState.Success(AuthUser("John O'Reilly", avatarUri = JohnUrl)),
                conferenceCleared = { },
                navigateToGoogleSignIn = { },
                navigateToGoogleSignOut = { },
                columnState = rememberResponsiveColumnState(),
                onRefreshClick = {},
                onRefreshToken = {},
                onEnableDeveloperMode = {},
                updatePreferences = {}
            )
        }
        composeRule.onNodeWithContentDescription("Logged in as John O'Reilly")
            .assertHasClickAction()
            .assertIsDisplayed()
    }
}