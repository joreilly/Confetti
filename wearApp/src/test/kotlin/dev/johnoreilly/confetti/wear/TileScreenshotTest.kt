@file:OptIn(ExperimentalHorologistApi::class)
@file:Suppress("UnstableApiUsage")

package dev.johnoreilly.confetti.wear

import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.google.android.horologist.annotations.ExperimentalHorologistApi
import com.google.android.horologist.compose.tools.TileLayoutPreview
import dev.johnoreilly.confetti.wear.TestFixtures.sessionDetails
import dev.johnoreilly.confetti.wear.TestFixtures.sessionTime
import dev.johnoreilly.confetti.wear.tile.CurrentSessionsData
import dev.johnoreilly.confetti.wear.tile.CurrentSessionsTileRenderer
import org.junit.Test

class TileScreenshotTest : ScreenshotTest() {
    @Test
    fun tile() = takeScreenshot(showTimeText = false) {
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
        val renderer = remember { CurrentSessionsTileRenderer(context) }

        TileLayoutPreview(tileState, tileState, renderer)
    }
}