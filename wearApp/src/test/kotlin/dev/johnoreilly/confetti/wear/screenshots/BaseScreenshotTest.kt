@file:OptIn(ExperimentalRoborazziApi::class, ExperimentalCoilApi::class)

package dev.johnoreilly.confetti.wear.screenshots

import android.app.Application
import android.content.Context
import android.content.res.Resources
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeUp
import androidx.test.core.app.ApplicationProvider
import androidx.wear.compose.material.MaterialTheme
import coil.ImageLoader
import coil.annotation.ExperimentalCoilApi
import coil.compose.LocalImageLoader
import coil.test.FakeImageLoaderEngine
import com.github.takahirom.roborazzi.ExperimentalRoborazziApi
import com.github.takahirom.roborazzi.RoborazziOptions
import com.github.takahirom.roborazzi.ThresholdValidator
import com.github.takahirom.roborazzi.captureRoboImage
import com.google.android.horologist.compose.layout.AppScaffold
import com.google.android.horologist.compose.layout.ResponsiveTimeText
import dev.johnoreilly.confetti.preview.JohnUrl
import dev.johnoreilly.confetti.wear.app.KoinTestApp
import dev.johnoreilly.confetti.wear.preview.TestFixtures.MartinUrl
import dev.johnoreilly.confetti.wear.ui.ConfettiTheme
import dev.johnoreilly.confetti.wear.ui.toColor
import okio.FileSystem
import okio.Path
import okio.Path.Companion.toPath
import org.junit.After
import org.junit.Assume
import org.junit.Before
import org.junit.Rule
import org.junit.rules.TestName
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin
import org.robolectric.ParameterizedRobolectricTestRunner
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@Config(
    application = KoinTestApp::class,
    sdk = [33],
    qualifiers = "w227dp-h227dp-small-notlong-round-watch-xhdpi-keyshidden-nonav"
)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
abstract class BaseScreenshotTest {
    @get:Rule
    val composeRule = createComposeRule()

    @get:Rule
    val testName = TestName()

    var tolerance: Float = 0.01f

    open var fakeImageLoader: FakeImageLoaderEngine = FakeImageLoaderEngine.Builder()
        .intercept({ it is String && it.startsWith(JohnUrl) }, loadTestBitmap("john.jpg".toPath()))
        .intercept({ it is String && it.startsWith(MartinUrl) }, loadTestBitmap("martin.jpg".toPath()))
        .build()

    open val device: WearDevice? = null

    open fun imageName(suffix: String) = "${testName.methodName}$suffix.png"

    @Before
    fun initDevice() {
        device?.let {
            RuntimeEnvironment.setQualifiers("+w${it.dp}dp-h${it.dp}dp")
            RuntimeEnvironment.setFontScale(it.fontScale)
        }
    }

    private val applicationContext: Context
        get() = ApplicationProvider.getApplicationContext<Application>()

    val resources: Resources
        get() = applicationContext.resources

    fun loadTestBitmap(path: Path): BitmapDrawable = FileSystem.RESOURCES.read(path) {
        BitmapDrawable(resources, BitmapFactory.decodeStream(this.inputStream()))
    }

    fun enableA11yTest() {
        // TODO reenable in follow up PR
        Assume.assumeTrue(false)
    }

    fun takeScreenshot(suffix: String = "") {
        composeRule.onRoot().captureRoboImage(
            filePath = "snapshot/${this.javaClass.simpleName}/${imageName(suffix)}",
            roborazziOptions = RoborazziOptions(
                recordOptions = RoborazziOptions.RecordOptions(
                    applyDeviceCrop = true
                ),
                compareOptions = RoborazziOptions.CompareOptions(
                    resultValidator = ThresholdValidator(tolerance)
                )
            )
        )
    }

    companion object {
        @JvmStatic
        @ParameterizedRobolectricTestRunner.Parameters(name = "{0}")
        fun params() = WearDevice.entries.toList()
    }

    @After
    fun teardown() {
        stopKoin()
    }

    open fun SemanticsNodeInteraction.scrollToBottom() {
        performTouchInput {
            repeat(10) {
                swipeUp(durationMillis = 10)
            }
        }
    }

    @Composable
    fun TestScaffold(content: @Composable () -> Unit) {
        val imageLoader = ImageLoader.Builder(LocalContext.current)
            .components { add(fakeImageLoader) }
            .build()

        @Suppress("DEPRECATION")
        CompositionLocalProvider(LocalImageLoader provides imageLoader) {
            AppScaffold(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colors.background),
                timeText = { ResponsiveTimeText(timeSource = FixedTimeSource) }
            ) {
                ConfettiTheme(seedColor = null.toColor()) {
                    content()
                }
            }
        }
    }
}
