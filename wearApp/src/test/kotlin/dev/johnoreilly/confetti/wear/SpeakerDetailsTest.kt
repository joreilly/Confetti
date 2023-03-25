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
import dev.johnoreilly.confetti.wear.TestFixtures.JohnOreilly
import dev.johnoreilly.confetti.wear.TestFixtures.JohnUrl
import dev.johnoreilly.confetti.wear.TestFixtures.MartinUrl
import dev.johnoreilly.confetti.wear.speakerdetails.SpeakerDetailsView
import okio.Path.Companion.toPath
import org.junit.Before
import org.junit.Test

class SpeakerDetailsTest : ScreenshotTest() {
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
    fun speakerDetailsScreen() = takeScrollableScreenshot(
        timeTextMode = TimeTextMode.OnTop,
        checks = {
            rule.onNodeWithText("John O'Reilly").assertIsDisplayed()
        }
    ) { columnState ->
        SpeakerDetailsView(
            speaker = JohnOreilly.speakerDetails,
            columnState = columnState,
        )
    }
}