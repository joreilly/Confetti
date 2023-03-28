@file:OptIn(ExperimentalHorologistApi::class)
@file:Suppress("UnstableApiUsage")

package dev.johnoreilly.confetti.wear

import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.wear.compose.material.Colors
import com.google.android.horologist.annotations.ExperimentalHorologistApi
import com.google.android.horologist.compose.tools.TileLayoutPreview
import dev.johnoreilly.confetti.wear.preview.TestFixtures.sessionDetails
import dev.johnoreilly.confetti.wear.preview.TestFixtures.sessionTime
import dev.johnoreilly.confetti.wear.screenshots.ScreenshotTest
import dev.johnoreilly.confetti.wear.settings.toMaterialThemeColors
import dev.johnoreilly.confetti.wear.tile.CurrentSessionsData
import dev.johnoreilly.confetti.wear.tile.CurrentSessionsTileRenderer
import org.junit.Test

class TileScreenshotTest : ScreenshotTest() {
    @Test
    fun tile() = takeScreenshot(timeText = {}) {
        val context = LocalContext.current

        val tileState = remember {
            CurrentSessionsData(
                "kotlinconf",
                sessionTime,
                listOf(
                    sessionDetails
                )
            )
        }

        val colors = mobileTheme?.toMaterialThemeColors() ?: Colors()

        val renderer = remember { CurrentSessionsTileRenderer(context).apply {
            updateTheme(colors)
        } }

        TileLayoutPreview(tileState, tileState, renderer)
    }
}