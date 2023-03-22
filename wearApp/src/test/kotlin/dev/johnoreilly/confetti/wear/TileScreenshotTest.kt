@file:OptIn(ExperimentalHorologistComposeLayoutApi::class, ExperimentalHorologistTilesApi::class)
@file:Suppress("UnstableApiUsage")

package dev.johnoreilly.confetti.wear

import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.google.android.horologist.compose.layout.ScalingLazyColumnDefaults
import com.google.android.horologist.compose.navscaffold.ExperimentalHorologistComposeLayoutApi
import com.google.android.horologist.compose.tools.TileLayoutPreview
import com.google.android.horologist.tiles.ExperimentalHorologistTilesApi
import dev.johnoreilly.confetti.fragment.SessionDetails
import dev.johnoreilly.confetti.fragment.SpeakerDetails
import dev.johnoreilly.confetti.navigation.ConferenceDayKey
import dev.johnoreilly.confetti.navigation.SessionDetailsKey
import dev.johnoreilly.confetti.type.Session
import dev.johnoreilly.confetti.utils.AndroidDateService
import dev.johnoreilly.confetti.wear.SessionsDetailsTest.Companion.sessionDetails
import dev.johnoreilly.confetti.wear.SessionsDetailsTest.Companion.sessionTime
import dev.johnoreilly.confetti.wear.sessiondetails.SessionDetailView
import dev.johnoreilly.confetti.wear.sessiondetails.SessionDetailsUiState
import dev.johnoreilly.confetti.wear.sessions.SessionListView
import dev.johnoreilly.confetti.wear.sessions.SessionsUiState
import dev.johnoreilly.confetti.wear.tile.CurrentSessionsData
import dev.johnoreilly.confetti.wear.tile.CurrentSessionsTileRenderer
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import org.junit.Test
import kotlin.time.Duration.Companion.hours

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