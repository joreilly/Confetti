@file:OptIn(
    ExperimentalCoroutinesApi::class, ExperimentalTestApi::class
)
@file:Suppress("UnstableApiUsage")

package dev.johnoreilly.confetti.wear.screenshots

import android.app.Application
import android.content.Context
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
import androidx.compose.ui.test.printToString
import androidx.test.core.app.ApplicationProvider
import androidx.wear.compose.material.Colors
import androidx.wear.compose.material.PositionIndicator
import androidx.wear.compose.material.Scaffold
import androidx.wear.compose.material.TimeText
import androidx.wear.compose.material.scrollAway
import coil.compose.LocalImageLoader
import com.google.android.horologist.compose.layout.ScalingLazyColumnDefaults
import com.google.android.horologist.compose.layout.ScalingLazyColumnState
import com.google.android.horologist.compose.tools.coil.FakeImageLoader
import com.quickbird.snapshot.Diffing
import com.quickbird.snapshot.FileSnapshotting
import com.quickbird.snapshot.FileSnapshottingNames
import com.quickbird.snapshot.Snapshotting
import com.quickbird.snapshot.fileSnapshotting
import com.quickbird.snapshot.snapshot
import dev.johnoreilly.confetti.screenshot.SnapshotTransformer
import dev.johnoreilly.confetti.screenshot.a11y.A11ySnapshotTransformer
import dev.johnoreilly.confetti.screenshot.bitmapWithTolerance
import dev.johnoreilly.confetti.screenshot.highlightWithRed
import dev.johnoreilly.confetti.wear.app.KoinTestApp
import dev.johnoreilly.confetti.wear.preview.TestFixtures
import dev.johnoreilly.confetti.wear.proto.Theme
import dev.johnoreilly.confetti.wear.settings.toMaterialThemeColors
import dev.johnoreilly.confetti.wear.ui.ConfettiThemeFixed
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import okio.FileSystem
import okio.Path
import org.junit.After
import org.junit.Assume.assumeTrue
import org.junit.Rule
import org.junit.rules.TestName
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin
import org.koin.test.KoinTest
import org.robolectric.ParameterizedRobolectricTestRunner
import org.robolectric.ParameterizedRobolectricTestRunner.Parameter
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import org.robolectric.annotation.GraphicsMode.Mode.NATIVE

@Deprecated("Please use BaseScreenshotTest instead")
@RunWith(ParameterizedRobolectricTestRunner::class)
@Config(
    application = KoinTestApp::class,
    sdk = [30],
    qualifiers = "w221dp-h221dp-small-notlong-round-watch-xhdpi-keyshidden-nonav"
)
@GraphicsMode(NATIVE)
abstract class ScreenshotTest : KoinTest {
    var tolerance: Float = 0f

    @get:Rule
    val rule = createComposeRule()

    @get:Rule
    val testName: TestName = TestName()

    private val directoryName = this::class.simpleName!!

    @Parameter(0)
    @JvmField
    var name: String = ""

    @Parameter(1)
    @JvmField
    var mobileTheme: Theme? = null

    // Flip to true to record
    var record = false

    var fakeImageLoader = FakeImageLoader.Never

    var snapshotTransformer: SnapshotTransformer = SnapshotTransformer.None

    val resources: Resources
        get() = applicationContext.resources

    val applicationContext: Context
        get() = ApplicationProvider.getApplicationContext<Application>()

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
        round: Boolean = resources.configuration.isScreenRound,
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
                        ConfettiThemeFixed(mobileTheme?.toMaterialThemeColors() ?: Colors()) {
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
                    val bitmap =
                        Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
                            .apply {
                                view.draw(Canvas(this))
                            }
                    snapshotTransformer.transform(node, bitmap)
                }
            ).fileSnapshotting

            // Flip to true to record
            snapshotting.snapshot(rule.onRoot())
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
                        ConfettiThemeFixed(mobileTheme?.toMaterialThemeColors() ?: Colors()) {
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

            snapshotting.snapshot(rule.onRoot())
        }
    }

    fun takeScrollableScreenshot(
        round: Boolean = resources.configuration.isScreenRound,
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
                try {
                    checks(columnState)
                } catch (e: AssertionError) {
                    println("State at failure: ${rule.onRoot().printToString()}")
                    throw e
                }
            }
        ) {
            columnState = columnStateFactory.create()

            content(columnState)
        }
    }

    fun enableA11yTest() {
        assumeTrue(mobileTheme == null)

        // allow more tolerance as A11y tests are mainly for illustrating the
        // current observable behaviour
        tolerance = 0.10f

        snapshotTransformer = A11ySnapshotTransformer()
    }

    suspend fun FileSnapshotting<SemanticsNodeInteraction, Bitmap>.snapshot(
        value: SemanticsNodeInteraction,
        fileSnapshottingNames: FileSnapshottingNames = FileSnapshottingNames.default
    ) = with(fileSnapshottingNames) {
        val methodName = testName.methodName.replace("\\W+".toRegex(), "_")
        snapshot(
            value = value,
            record = record,
            fileName = methodName + "_$referenceFilePrefix",
            diffFileName = methodName + "_$diffFilePrefix",
            directoryName = directoryName,
            path = parentDirectory
        )
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
            arrayOf("Material", null),
            arrayOf("MobileDefault", TestFixtures.MobileTheme),
            arrayOf("MobileAndroid", TestFixtures.AndroidTheme),
        )

        fun loadTestBitmap(path: Path): Bitmap = FileSystem.RESOURCES.read(path) {
            BitmapFactory.decodeStream(this.inputStream())
        }
    }
}