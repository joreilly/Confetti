 @file:Suppress("UnstableApiUsage")
 @file:OptIn(ExperimentalHorologistApi::class)

 package dev.johnoreilly.confetti.wear.surfaces

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.google.android.horologist.annotations.ExperimentalHorologistApi
import dev.johnoreilly.confetti.wear.app.KoinTestApp
import dev.johnoreilly.confetti.wear.complication.ComplicationRendererPreview
import dev.johnoreilly.confetti.wear.complication.NextSessionComplicationData
import dev.johnoreilly.confetti.wear.complication.NextSessionTemplate
import dev.johnoreilly.confetti.wear.preview.TestFixtures
import dev.johnoreilly.confetti.wear.screenshots.BaseScreenshotTest
import org.junit.Test
import org.robolectric.annotation.Config

 @Config(
    application = KoinTestApp::class,
    sdk = [30],
    qualifiers = "w221dp-h221dp-small-notlong-notround-watch-xhdpi-keyshidden-nonav"
)
class ComplicationScreenshotTest : BaseScreenshotTest() {
    @Test
    fun session() {
        composeRule.setContent {
            val data = remember {
                NextSessionComplicationData(
                    sessionDetails = TestFixtures.sessionDetails,
                    conference = TestFixtures.kotlinConf2023Config,
                    launchIntent = null
                )
            }

            ComplicationPreview(data)
        }

        takeScreenshot("")
    }

    @Composable
    private fun ComplicationPreview(data: NextSessionComplicationData) {
        val context = LocalContext.current

        val renderer = remember {
            NextSessionTemplate(context)
        }

        ComplicationRendererPreview(renderer, data)
    }
}