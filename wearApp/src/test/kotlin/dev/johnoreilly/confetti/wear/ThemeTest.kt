@file:Suppress("UnstableApiUsage")

package dev.johnoreilly.confetti.wear

import androidx.core.graphics.drawable.toDrawable
import coil.decode.DataSource
import coil.request.SuccessResult
import com.google.android.horologist.annotations.ExperimentalHorologistApi
import com.google.android.horologist.compose.tools.coil.FakeImageLoader
import dev.johnoreilly.confetti.wear.screenshots.ScreenshotTest
import okio.Path.Companion.toPath
import org.junit.Before
import org.junit.Test
import org.robolectric.annotation.Config

class ThemeTest : ScreenshotTest() {
    init {
        // useful for illustrative purpose only
        tolerance = 0.2f
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
    @Config(
        qualifiers = "+h400dp-notround"
    )
    fun themeTest() = takeScreenshot(
        checks = {
        },
        timeText = {}
    ) {
        ThemePreview()
    }

    @Test
    @Config(
        qualifiers = "+w250dp-h250dp-notround"
    )
    fun swatchesTest() = takeScreenshot(
        checks = {
        },
        timeText = {}
    ) {
        ThemeSwatches()
    }
}