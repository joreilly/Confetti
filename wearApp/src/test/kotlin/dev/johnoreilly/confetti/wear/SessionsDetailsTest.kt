@file:Suppress("UnstableApiUsage")

package dev.johnoreilly.confetti.wear

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.core.graphics.drawable.toDrawable
import coil.decode.DataSource
import coil.request.SuccessResult
import com.google.android.horologist.compose.layout.rememberResponsiveColumnState
import com.google.android.horologist.images.coil.FakeImageLoader
import dev.johnoreilly.confetti.decompose.SessionDetailsUiState
import dev.johnoreilly.confetti.wear.preview.TestFixtures.JohnUrl
import dev.johnoreilly.confetti.wear.preview.TestFixtures.MartinUrl
import dev.johnoreilly.confetti.wear.preview.TestFixtures.sessionDetails
import dev.johnoreilly.confetti.wear.screenshots.BaseScreenshotTest
import dev.johnoreilly.confetti.wear.sessiondetails.SessionDetailView
import okio.Path.Companion.toPath
import org.junit.Before
import org.junit.Test

class SessionsDetailsTest : BaseScreenshotTest() {
    init {
        tolerance = 0.02f
    }


    val uiState = SessionDetailsUiState.Success(
        sessionDetails
    )

    @Before
    fun loadImages() {
        val martinBitmap = loadTestBitmap("martin.jpg".toPath())
        val johnBitmap = loadTestBitmap("john.jpg".toPath())

        fakeImageLoader = FakeImageLoader {
            val bitmap = when (it.data) {
                "$JohnUrl?size=Watch" -> johnBitmap
                "$MartinUrl?size=Watch" -> martinBitmap
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
    fun sessionDetailsScreen() {
        runScreenshotTest {
            SessionDetailView(
                uiState = uiState,
                navigateToSpeaker = {},
                columnState = rememberResponsiveColumnState()
            )
        }
        composeRule.onNodeWithText("Thursday 14:00").assertIsDisplayed()
    }

    @Test
    fun sessionDetailsScreenA11y() {
        enableA11yTest()

        runScreenshotTest {
            SessionDetailView(
                uiState = uiState,
                navigateToSpeaker = {},
                columnState = rememberResponsiveColumnState()
            )
        }
        composeRule.onNodeWithText("Thursday 14:00").assertIsDisplayed()
    }
}