@file:OptIn(ExperimentalHorologistApi::class)
@file:Suppress("UnstableApiUsage")

package dev.johnoreilly.confetti.wear

import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.wear.compose.material.Colors
import com.google.android.horologist.annotations.ExperimentalHorologistApi
import com.google.android.horologist.compose.tools.TileLayoutPreview
import dev.johnoreilly.confetti.GetBookmarkedSessionsQuery
import dev.johnoreilly.confetti.wear.preview.TestFixtures
import dev.johnoreilly.confetti.wear.screenshots.ScreenshotTest
import dev.johnoreilly.confetti.wear.settings.toMaterialThemeColors
import dev.johnoreilly.confetti.wear.tile.ConfettiTileData
import dev.johnoreilly.confetti.wear.tile.ConfettiTileData.CurrentSessionsData
import dev.johnoreilly.confetti.wear.tile.CurrentSessionsTileRenderer
import org.junit.Test

class TileScreenshotTest : ScreenshotTest() {

    @Test
    fun tile() = takeScreenshot(timeText = {}) {
        val context = LocalContext.current

        val tileState = remember {
            CurrentSessionsData(
                GetBookmarkedSessionsQuery.Config(
                    TestFixtures.kotlinConf2023.id,
                    "",
                    TestFixtures.kotlinConf2023.days,
                    TestFixtures.kotlinConf2023.name
                ),
                listOf(
                    TestFixtures.sessionDetails
                )
            )
        }

        val colors = mobileTheme?.toMaterialThemeColors() ?: Colors()

        val renderer = remember {
            CurrentSessionsTileRenderer(context).apply {
                this.colors.value = colors
            }
        }

        TileLayoutPreview(tileState, tileState, renderer)
    }

    @Test
    fun notLoggedIn() = takeScreenshot(timeText = {}) {
        val context = LocalContext.current

        val tileState = remember {
            ConfettiTileData.NotLoggedIn(
                GetBookmarkedSessionsQuery.Config(
                    TestFixtures.kotlinConf2023.id,
                    "",
                    TestFixtures.kotlinConf2023.days,
                    TestFixtures.kotlinConf2023.name
                )
            )
        }

        val colors = mobileTheme?.toMaterialThemeColors() ?: Colors()

        val renderer = remember {
            CurrentSessionsTileRenderer(context).apply {
                this.colors.value = colors
            }
        }

        TileLayoutPreview(tileState, tileState, renderer)
    }

    @Test
    fun noConferenceSelected() = takeScreenshot(timeText = {}) {
        val context = LocalContext.current

        val tileState = remember {
            ConfettiTileData.NoConference
        }

        val colors = mobileTheme?.toMaterialThemeColors() ?: Colors()

        val renderer = remember {
            CurrentSessionsTileRenderer(context).apply {
                this.colors.value = colors
            }
        }

        TileLayoutPreview(tileState, tileState, renderer)
    }
}