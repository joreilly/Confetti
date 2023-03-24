@file:OptIn(ExperimentalCoroutinesApi::class)
@file:Suppress("UnstableApiUsage")

package dev.johnoreilly.confetti.wear

import android.graphics.Bitmap
import android.graphics.Canvas
import android.view.View
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.wear.compose.material.Scaffold
import androidx.wear.compose.material.TimeText
import com.quickbird.snapshot.Diffing
import com.quickbird.snapshot.JUnitFileSnapshotTest
import com.quickbird.snapshot.Snapshotting
import com.quickbird.snapshot.bitmap
import com.quickbird.snapshot.fileSnapshotting
import com.quickbird.snapshot.intMean
import dev.johnoreilly.confetti.wear.ui.ConfettiTheme
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Rule
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin
import org.koin.test.KoinTest
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import org.robolectric.annotation.GraphicsMode.Mode.NATIVE

@RunWith(RobolectricTestRunner::class)
@Config(
    application = KoinTestApp::class,
    sdk = [30],
    qualifiers = "w221dp-h221dp-small-notlong-round-watch-xhdpi-keyshidden-nonav"
)
@GraphicsMode(NATIVE)
abstract class ScreenshotTest : JUnitFileSnapshotTest(), KoinTest {
    @get:Rule
    val rule = createComposeRule()

    @After
    fun after() {
        stopKoin()
    }

    fun takeScreenshot(
        round: Boolean = true,
        showTimeText: Boolean = true,
        checks: () -> Unit = {},
        content: @Composable () -> Unit
    ) {
        runTest {
            lateinit var view: View

            rule.setContent {
                view = LocalView.current
                Box(
                    modifier = Modifier
                        .background(Color.Transparent)
                ) {
                    ConfettiTheme {
                        Scaffold(
                            modifier = Modifier
                                .fillMaxSize()
                                .run {
                                    if (round) {
                                        clip(CircleShape)
                                    } else {
                                        this
                                    }
                                }
                                .background(Color.Black),
                            timeText = {
                                if (showTimeText) {
                                    TimeText(timeSource = FixedTimeSource)
                                }
                            }
                        ) {
                            content()
                        }
                    }

                }
            }

            rule.awaitIdle()

            checks()

            val snapshotting = Snapshotting(
                diffing = Diffing.bitmap(colorDiffing = Diffing.intMean),
                snapshot = { node: SemanticsNodeInteraction ->
                    Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888).apply {
                        view.draw(Canvas(this))
                    }
                }
            ).fileSnapshotting

            // Flip to true to record
            snapshotting.snapshot(rule.onRoot(), record = true)
        }
    }
}