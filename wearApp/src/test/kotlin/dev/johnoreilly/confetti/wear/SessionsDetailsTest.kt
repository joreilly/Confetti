@file:OptIn(ExperimentalHorologistApi::class)
@file:Suppress("UnstableApiUsage")

package dev.johnoreilly.confetti.wear

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithText
import androidx.core.graphics.drawable.toDrawable
import coil.decode.DataSource
import coil.request.SuccessResult
import com.google.android.horologist.annotations.ExperimentalHorologistApi
import com.google.android.horologist.compose.tools.coil.FakeImageLoader
import dev.johnoreilly.confetti.navigation.SessionDetailsKey
import dev.johnoreilly.confetti.utils.AndroidDateService
import dev.johnoreilly.confetti.wear.screenshots.TestFixtures.JohnUrl
import dev.johnoreilly.confetti.wear.screenshots.TestFixtures.MartinUrl
import dev.johnoreilly.confetti.wear.screenshots.TestFixtures.sessionDetails
import dev.johnoreilly.confetti.wear.screenshots.ScreenshotTest
import dev.johnoreilly.confetti.wear.sessiondetails.SessionDetailView
import dev.johnoreilly.confetti.wear.sessiondetails.SessionDetailsUiState
import kotlinx.datetime.TimeZone
import okio.Path.Companion.toPath
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals

class SessionsDetailsTest : ScreenshotTest() {
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
    fun sessionDetailsScreen() = takeScrollableScreenshot(
        timeTextMode = TimeTextMode.OnTop,
        checks = {
            rule.onNodeWithText("Thursday 14:00").assertIsDisplayed()
        }
    ) { columnState ->
        SessionDetailView(
            uiState = SessionDetailsUiState.Success(
                "wearconf",
                SessionDetailsKey("fosdem", "14997"),
                sessionDetails,
                TimeZone.UTC
            ),
            navigateToSpeaker = {},
            columnState = columnState,
            formatter = { AndroidDateService().format(it, TimeZone.UTC, "eeee HH:mm") }
        )
    }

    @Test
    fun sessionDetailsScreenEnd() = takeScrollableScreenshot(
        timeTextMode = TimeTextMode.Off,
        checks = { columnState ->
            columnState.state.scrollToItem(100)
            rule.onNodeWithText("Martin Bonnin").assertIsDisplayed()
            assertEquals(7, columnState.state.centerItemIndex)
        }
    ) { columnState ->
        SessionDetailView(
            uiState = SessionDetailsUiState.Success(
                "wearconf",
                SessionDetailsKey("fosdem", "14997"),
                sessionDetails,
                TimeZone.UTC
            ),
            navigateToSpeaker = {},
            columnState = columnState,
            formatter = { AndroidDateService().format(it, TimeZone.UTC, "eeee HH:mm") }
        )
    }
}