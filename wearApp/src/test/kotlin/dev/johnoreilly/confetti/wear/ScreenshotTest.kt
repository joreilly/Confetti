@file:OptIn(ExperimentalCoroutinesApi::class, ExperimentalHorologistApi::class)
@file:Suppress("UnstableApiUsage")

package dev.johnoreilly.confetti.wear

import android.graphics.Bitmap
import android.graphics.Canvas
import android.util.Size
import android.view.View
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.wear.compose.material.Scaffold
import androidx.wear.compose.material.TimeText
import androidx.wear.compose.material.scrollAway
import com.google.android.horologist.annotations.ExperimentalHorologistApi
import com.google.android.horologist.compose.layout.ScalingLazyColumnDefaults
import com.google.android.horologist.compose.layout.ScalingLazyColumnState
import com.quickbird.snapshot.Diffing
import com.quickbird.snapshot.JUnitFileSnapshotTest
import com.quickbird.snapshot.Snapshotting
import com.quickbird.snapshot.fileSnapshotting
import com.quickbird.snapshot.intMean
import dev.johnoreilly.confetti.wear.proto.Theme
import dev.johnoreilly.confetti.wear.ui.ConfettiTheme
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import okio.Buffer
import okio.ByteString
import org.junit.After
import org.junit.Rule
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin
import org.koin.test.KoinTest
import org.robolectric.ParameterizedRobolectricTestRunner
import org.robolectric.ParameterizedRobolectricTestRunner.Parameter
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import org.robolectric.annotation.GraphicsMode.Mode.NATIVE

@RunWith(ParameterizedRobolectricTestRunner::class)
@Config(
    application = KoinTestApp::class,
    sdk = [30],
    qualifiers = "w221dp-h221dp-small-notlong-round-watch-xhdpi-keyshidden-nonav"
)
@GraphicsMode(NATIVE)
abstract class ScreenshotTest : JUnitFileSnapshotTest(), KoinTest {
    var tolerance: Float = 0f

    @get:Rule
    val rule = createComposeRule()

    @Parameter(0)
    @JvmField
    var name: String = ""

    @Parameter(1)
    @JvmField
    var mobileTheme: Theme? = null

    @After
    fun after() {
        stopKoin()
    }

    fun takeScreenshot(
        round: Boolean = true,
        timeText: @Composable () -> Unit = {
            TimeText(
                timeSource = FixedTimeSource
            )
        },
        checks: suspend () -> Unit = {},
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
                    ConfettiTheme(mobileTheme = mobileTheme) {
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
                                timeText()
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
                diffing = Diffing.bitmapWithTolerance(
                    tolerance = tolerance,
                    colorDiffing = Diffing.highlightWithRed
                ),
                snapshot = { node: SemanticsNodeInteraction ->
                    Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888).apply {
                        view.draw(Canvas(this))
                    }
                }
            ).fileSnapshotting

            // Flip to true to record
            snapshotting.snapshot(rule.onRoot(), record = false)
        }
    }

    fun takeScrollableScreenshot(
        round: Boolean = true,
        columnStateFactory: ScalingLazyColumnState.Factory = ScalingLazyColumnDefaults.belowTimeText(),
        checks: suspend (columnState: ScalingLazyColumnState) -> Unit = {},
        content: @Composable (columnState: ScalingLazyColumnState) -> Unit
    ) {
        lateinit var columnState: ScalingLazyColumnState

        takeScreenshot(
            round,
            timeText = {
                TimeText(
                    timeSource = FixedTimeSource,
                    modifier = Modifier.scrollAway(columnState.state)
                )
            },
            checks = {
                checks(columnState)
            }
        ) {
            columnState = columnStateFactory.create()

            content(columnState)
        }
    }

    companion object {
        @JvmStatic
        @ParameterizedRobolectricTestRunner.Parameters(name = "Colors: {0}")
        fun params() = listOf(
            arrayOf("Confetti", null),
            arrayOf("Phone-1", TestFixtures.MobileTheme),
        )
    }
}