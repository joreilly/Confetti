@file:Suppress("UnstableApiUsage")

package dev.johnoreilly.confetti.wear

import dev.johnoreilly.confetti.wear.screenshots.BaseScreenshotTest
import dev.johnoreilly.confetti.wear.ui.Theme
import dev.johnoreilly.confetti.wear.ui.ThemePreview
import dev.johnoreilly.confetti.wear.ui.WearPreviewThemes
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.ParameterizedRobolectricTestRunner

@RunWith(ParameterizedRobolectricTestRunner::class)
class ThemeTest(val seedColor: Theme) : BaseScreenshotTest() {
    init {
        // useful for illustrative purpose only
        tolerance = 0.2f
    }

    @Test
    fun sessionDetailsScreen() {
        composeRule.setContent {
            ThemePreview(seedColor)
        }
    }

    companion object {
        @JvmStatic
        @ParameterizedRobolectricTestRunner.Parameters(name = "{0}")
        fun params() = WearPreviewThemes().values.toList()
    }
}