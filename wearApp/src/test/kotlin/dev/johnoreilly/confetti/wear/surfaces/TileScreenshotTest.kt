
@file:Suppress("UnstableApiUsage", "DEPRECATION")

package dev.johnoreilly.confetti.wear.surfaces

import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.google.android.horologist.screenshots.tiles.TileLayoutPreview
import dev.johnoreilly.confetti.GetBookmarkedSessionsQuery
import dev.johnoreilly.confetti.wear.preview.TestFixtures
import dev.johnoreilly.confetti.wear.screenshots.BaseScreenshotTest
import dev.johnoreilly.confetti.wear.screenshots.WearDevice
import dev.johnoreilly.confetti.wear.tile.ConfettiTileData
import dev.johnoreilly.confetti.wear.tile.ConfettiTileData.CurrentSessionsData
import dev.johnoreilly.confetti.wear.tile.CurrentSessionsTileRenderer
import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.ParameterizedRobolectricTestRunner

@Ignore("Failing after preview changes")
@RunWith(ParameterizedRobolectricTestRunner::class)
class TileScreenshotTest(override val device: WearDevice) : BaseScreenshotTest() {

    @Test
    fun tile() {
        composeRule.setContent {
            val context = LocalContext.current

            val tileState = remember {
                CurrentSessionsData(
                    GetBookmarkedSessionsQuery.Config(
                        TestFixtures.kotlinConf2023.id,
                        "",
                        TestFixtures.kotlinConf2023.days,
                        TestFixtures.kotlinConf2023.name,
                        __typename = "Conference",
                    ),
                    listOf(
                        TestFixtures.sessionDetails
                    )
                )
            }

            val renderer = remember {
                CurrentSessionsTileRenderer(context)
            }

            TileLayoutPreview(tileState, tileState, renderer)
        }

        takeScreenshot()
    }

    @Test
    fun notLoggedIn() {
        composeRule.setContent {
            val context = LocalContext.current

            val tileState = remember {
                ConfettiTileData.NotLoggedIn(
                    GetBookmarkedSessionsQuery.Config(
                        TestFixtures.kotlinConf2023.id,
                        "",
                        TestFixtures.kotlinConf2023.days,
                        TestFixtures.kotlinConf2023.name,
                        __typename = "Conference",
                    )
                )
            }

            val renderer = remember {
                CurrentSessionsTileRenderer(context)
            }

            TileLayoutPreview(tileState, tileState, renderer)
        }

        takeScreenshot()
    }

    @Test
    fun noConferenceSelected() {
        composeRule.setContent {
            val context = LocalContext.current

            val tileState = remember {
                ConfettiTileData.NoConference
            }

            val renderer = remember {
                CurrentSessionsTileRenderer(context)
            }

            TileLayoutPreview(tileState, tileState, renderer)
        }

        takeScreenshot()
    }
}
