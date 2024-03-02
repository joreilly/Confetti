@file:Suppress("UnstableApiUsage")

package dev.johnoreilly.confetti.wear

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasScrollToIndexAction
import androidx.compose.ui.test.onNodeWithText
import androidx.core.graphics.drawable.toDrawable
import androidx.wear.compose.material.MaterialTheme
import coil.decode.DataSource
import coil.request.SuccessResult
import com.google.android.horologist.compose.layout.AppScaffold
import com.google.android.horologist.compose.layout.rememberResponsiveColumnState
import com.google.android.horologist.images.coil.FakeImageLoader
import dev.johnoreilly.confetti.decompose.SpeakerDetailsUiState
import dev.johnoreilly.confetti.wear.preview.TestFixtures.JohnOreilly
import dev.johnoreilly.confetti.wear.preview.TestFixtures.JohnUrl
import dev.johnoreilly.confetti.wear.preview.TestFixtures.MartinUrl
import dev.johnoreilly.confetti.wear.screenshots.BaseScreenshotTest
import dev.johnoreilly.confetti.wear.speakerdetails.SpeakerDetailsView
import okio.Path.Companion.toPath
import org.junit.Before
import org.junit.Test

class SpeakerDetailsTest : BaseScreenshotTest() {
    init {
        tolerance = 0.02f
    }

    @Before
    fun loadImages() {
        val martinBitmap = loadTestBitmap("martin.jpg".toPath())
        val johnBitmap = loadTestBitmap("john.jpg".toPath())

        fakeImageLoader = FakeImageLoader {
            val bitmap = when (it.data) {
                JohnUrl -> johnBitmap
                MartinUrl -> martinBitmap
                else -> null
            }
            if (bitmap != null) {
                SuccessResult(
                    drawable = bitmap.toDrawable(resources),
                    dataSource = DataSource.MEMORY,
                    request = it
                )
            } else {
                FakeImageLoader.Never.execute(it)
            }
        }
    }

    @Test
    fun speakerDetailsScreen() {
        composeRule.setContent {
            AppScaffold(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colors.background)
            ) {
                SpeakerDetailsView(
                    uiState = SpeakerDetailsUiState.Success(JohnOreilly.speakerDetails),
                    columnState = rememberResponsiveColumnState()
                )
            }
        }
        composeRule.onNodeWithText("John O'Reilly").assertIsDisplayed()
        takeScreenshot()
        composeRule.onNode(hasScrollToIndexAction())
            .scrollToBottom()
        takeScreenshot("_end")
    }

    @Test
    fun speakerDetailsScreenA11y() {
        enableA11yTest()

        composeRule.setContent {
            AppScaffold(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colors.background)
            ) {
                SpeakerDetailsView(
                    uiState = SpeakerDetailsUiState.Success(JohnOreilly.speakerDetails),
                    columnState = rememberResponsiveColumnState()
                )
            }
        }
        composeRule.onNodeWithText("John O'Reilly").assertIsDisplayed()
        takeScreenshot()
        composeRule.onNode(hasScrollToIndexAction())
            .scrollToBottom()
        takeScreenshot("_end")
    }
}