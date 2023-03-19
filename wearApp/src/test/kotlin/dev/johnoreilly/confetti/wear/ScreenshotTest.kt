@file:OptIn(ExperimentalCoroutinesApi::class, ExperimentalHorologistComposeLayoutApi::class)
@file:Suppress("UnstableApiUsage")

package dev.johnoreilly.confetti.wear

import android.graphics.Bitmap
import android.graphics.Canvas
import android.view.View
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.Scaffold
import androidx.wear.compose.material.TimeSource
import androidx.wear.compose.material.TimeText
import com.google.accompanist.testharness.TestHarness
import com.google.android.horologist.compose.navscaffold.ExperimentalHorologistComposeLayoutApi
import com.quickbird.snapshot.Diffing
import com.quickbird.snapshot.JUnitFileSnapshotTest
import com.quickbird.snapshot.Snapshotting
import com.quickbird.snapshot.bitmap
import com.quickbird.snapshot.fileSnapshotting
import com.quickbird.snapshot.intMean
import dev.johnoreilly.confetti.wear.ui.ConfettiTheme
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.runner.RunWith
import org.koin.test.KoinTest
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import org.robolectric.annotation.GraphicsMode.Mode.NATIVE

@RunWith(RobolectricTestRunner::class)
@Config(application = KoinTestApp::class, sdk = [30])
@GraphicsMode(NATIVE)
open class ScreenshotTest : JUnitFileSnapshotTest(), KoinTest {
    @get:Rule
    val rule = createComposeRule()

    fun takeScreenshot(
        round: Boolean = true,
        showTimeText: Boolean = true,
        size: DpSize = DpSize(221.dp, 221.dp),
        content: @Composable () -> Unit
    ) {
        runTest {
            lateinit var view: View

            rule.setContent {
                view = LocalView.current
                Box(
                    modifier = Modifier
                        .size(size)
                        .background(Color.Transparent)
                ) {
                    ConfettiTheme {
                        TestHarness(isScreenRound = round, size = size, darkMode = true) {
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
                                        TimeText(timeSource = object : TimeSource {
                                            override val currentTime: String
                                                @Composable get() = "10:10"
                                        })
                                    }
                                }
                            ) {
                                content()
                            }
                        }
                    }
                }
            }

            rule.awaitIdle()

            val snapshotting = Snapshotting(
                diffing = Diffing.bitmap(colorDiffing = Diffing.intMean),
                snapshot = { node: SemanticsNodeInteraction ->
                    Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888).apply {
                        view.draw(Canvas(this))
                    }
                }
            ).fileSnapshotting

            snapshotting.snapshot(rule.onRoot(), record = false)
        }
    }
}