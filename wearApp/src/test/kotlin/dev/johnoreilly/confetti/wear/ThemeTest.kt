@file:OptIn(ExperimentalHorologistApi::class)
@file:Suppress("UnstableApiUsage")

package dev.johnoreilly.confetti.wear

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.ui.Modifier
import androidx.core.graphics.drawable.toDrawable
import androidx.wear.compose.material.ListHeader
import androidx.wear.compose.material.Text
import coil.decode.DataSource
import coil.request.SuccessResult
import com.google.android.horologist.annotations.ExperimentalHorologistApi
import com.google.android.horologist.base.ui.components.StandardChip
import com.google.android.horologist.base.ui.components.StandardChipType
import com.google.android.horologist.composables.PlaceholderChip
import com.google.android.horologist.compose.layout.ScalingLazyColumn
import com.google.android.horologist.compose.tools.coil.FakeImageLoader
import dev.johnoreilly.confetti.wear.components.SessionSpeakerChip
import dev.johnoreilly.confetti.wear.screenshots.ScreenshotTest
import dev.johnoreilly.confetti.wear.screenshots.TestFixtures
import dev.johnoreilly.confetti.wear.sessions.SessionCard
import okio.Path.Companion.toPath
import org.junit.Before
import org.junit.Test
import org.robolectric.annotation.Config

class ThemeTest : ScreenshotTest() {
    init {
        tolerance = 0.05f
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
        qualifiers = "+h450dp-notround"
    )
    fun themeTest() = takeScrollableScreenshot(
        timeTextMode = TimeTextMode.OnTop,
        checks = {
        }
    ) { columnState ->
        ScalingLazyColumn(columnState = columnState) {
            item {
                ListHeader {
                    Text("List Header")
                }
            }
            item {
                Text("Confetti: building a Kotlin Multiplatform conference app in 40min")
            }
            item {
                SessionSpeakerChip(
                    conference = "kotlinconf2023",
                    speaker = TestFixtures.JohnOreilly.speakerDetails,
                    navigateToSpeaker = {}
                )
            }
            item {
                StandardChip(
                    label = "Secondary Chip",
                    secondaryLabel = "with secondary label",
                    onClick = { /*TODO*/ },
                    chipType = StandardChipType.Secondary
                )
            }
            item {
                PlaceholderChip(
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            item {
                SessionCard(TestFixtures.sessionDetails) {}
            }
        }
    }
}