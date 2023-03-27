@file:OptIn(ExperimentalHorologistApi::class)
@file:Suppress("UnstableApiUsage")

package dev.johnoreilly.confetti.wear

import androidx.core.graphics.drawable.toDrawable
import androidx.wear.compose.material.Colors
import androidx.wear.compose.material.MaterialTheme
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
        record = true
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
        qualifiers = "+notround"
    )
    fun swatchesTest() = takeScreenshot(
        checks = {
        },
        timeText = {}
    ) {
        ThemeSwatches()
    }

    @Test
    @Config(
        qualifiers = "+h400dp-notround"
    )
    fun themeTestMaterial() = takeScreenshot(
        checks = {
        },
        timeText = {}
    ) {
        MaterialTheme(colors = Colors()) {
            ThemePreview()
        }
    }

    @Test
    @Config(
        qualifiers = "+notround"
    )
    fun swatchesTestMaterial() = takeScreenshot(
        checks = {
        },
        timeText = {}
    ) {
        MaterialTheme(colors = Colors()) {
            ThemeSwatches()
        }
    }
}