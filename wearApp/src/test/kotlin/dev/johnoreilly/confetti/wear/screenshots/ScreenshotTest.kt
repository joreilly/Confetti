@file:OptIn(
    ExperimentalCoroutinesApi::class, ExperimentalHorologistApi::class,
    ExperimentalTestApi::class
)
@file:Suppress("UnstableApiUsage")

package dev.johnoreilly.confetti.wear.screenshots

import android.app.Application
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.view.View
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.test.core.app.ApplicationProvider
import androidx.wear.compose.material.PositionIndicator
import androidx.wear.compose.material.Scaffold
import androidx.wear.compose.material.TimeText
import androidx.wear.compose.material.scrollAway
import coil.compose.LocalImageLoader
import com.google.android.horologist.annotations.ExperimentalHorologistApi
import com.google.android.horologist.compose.layout.ScalingLazyColumnDefaults
import com.google.android.horologist.compose.layout.ScalingLazyColumnState
import com.google.android.horologist.compose.tools.coil.FakeImageLoader
import com.quickbird.snapshot.Diffing
import com.quickbird.snapshot.JUnitFileSnapshotTest
import com.quickbird.snapshot.Snapshotting
import com.quickbird.snapshot.fileSnapshotting
import dev.johnoreilly.confetti.wear.a11y.A11ySnapshotTransformer
import dev.johnoreilly.confetti.wear.app.KoinTestApp
import dev.johnoreilly.confetti.wear.proto.Theme
import dev.johnoreilly.confetti.wear.ui.ConfettiTheme
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import okio.FileSystem
import okio.Path
import org.junit.After
import org.junit.Assume
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

    var record = false

    var fakeImageLoader = FakeImageLoader.Never

    var snapshotTransformer: SnapshotTransformer = SnapshotTransformer.None

    val resources: Resources
        get() = ApplicationProvider.getApplicationContext<Application>().resources

    @After
    fun after() {
        stopKoin()
    }

    @Composable
    fun FakeImageLoader.apply(content: @Composable () -> Unit) {
        // Not sure why this is needed, but Coil has improved
        // test support in next release
        this.override {
            CompositionLocalProvider(LocalImageLoader provides this) {
                content()
            }
        }
    }

    fun takeScreenshot(
        round: Boolean = true,
        timeText: @Composable () -> Unit = {
            TimeText(
                timeSource = FixedTimeSource
            )
        },
        positionIndicator: @Composable () -> Unit = {
        },
        checks: suspend () -> Unit = {},
        content: @Composable () -> Unit
    ) {
        runTest {
            lateinit var view: View

            rule.setContent {
                view = LocalView.current
                fakeImageLoader.apply {
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
                                },
                                positionIndicator = positionIndicator
                            ) {
                                content()
                            }
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
                    val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888).apply {
                        view.draw(Canvas(this))
                    }
                    snapshotTransformer.transform(node, bitmap)
                }
            ).fileSnapshotting

            // Flip to true to record
            snapshotting.snapshot(rule.onRoot(), record = record)
        }
    }

    fun takeComponentScreenshot(
        round: Boolean = true,
        checks: suspend () -> Unit = {},
        content: @Composable BoxScope.() -> Unit
    ) {
        runTest {
            lateinit var view: View

            rule.setContent {
                view = LocalView.current
                fakeImageLoader.override {
                    Box(
                        modifier = Modifier
                            .background(Color.Transparent),
                        contentAlignment = Alignment.Center
                    ) {
                        ConfettiTheme(mobileTheme = mobileTheme) {
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
            snapshotting.snapshot(rule.onRoot(), record = record)
        }
    }

    fun takeScrollableScreenshot(
        round: Boolean = true,
        timeTextMode: TimeTextMode,
        columnStateFactory: ScalingLazyColumnState.Factory = ScalingLazyColumnDefaults.belowTimeText(),
        checks: suspend (columnState: ScalingLazyColumnState) -> Unit = {},
        content: @Composable (columnState: ScalingLazyColumnState) -> Unit
    ) {
        lateinit var columnState: ScalingLazyColumnState

        takeScreenshot(
            round,
            timeText = {
                if (timeTextMode != TimeTextMode.Off) {
                    TimeText(
                        timeSource = FixedTimeSource,
                        modifier = if (timeTextMode == TimeTextMode.Scrolling)
                            Modifier.scrollAway(columnState.state)
                        else
                            Modifier
                    )
                }
            },
            positionIndicator = {
                PositionIndicator(scalingLazyListState = columnState.state)
            },
            checks = {
                checks(columnState)
            }
        ) {
            columnState = columnStateFactory.create()

            content(columnState)
        }
    }

    fun enableA11yTest() {
        Assume.assumeTrue(mobileTheme == null)

        // allow more tolerance as A11y tests are mainly for illustrating the
        // current observable behaviour
        tolerance = 0.10f
        record = true

        snapshotTransformer = A11ySnapshotTransformer()
    }

    enum class TimeTextMode {
        OnTop,
        Off,
        Scrolling
    }

    companion object {
        @JvmStatic
        @ParameterizedRobolectricTestRunner.Parameters(name = "Colors: {0}")
        fun params() = listOf(
            arrayOf("Confetti", null),
            arrayOf("Phone-1", TestFixtures.MobileTheme),
        )

        fun loadTestBitmap(path: Path): Bitmap = FileSystem.RESOURCES.read(path) {
            BitmapFactory.decodeStream(this.inputStream())
        }
    }
}