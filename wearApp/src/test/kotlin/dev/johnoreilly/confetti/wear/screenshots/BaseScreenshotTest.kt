@file:OptIn(ExperimentalRoborazziApi::class)

package dev.johnoreilly.confetti.wear.screenshots

import android.app.Application
import android.content.Context
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasScrollToIndexAction
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performScrollToIndex
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeUp
import androidx.test.core.app.ApplicationProvider
import androidx.wear.compose.material.MaterialTheme
import com.github.takahirom.roborazzi.ExperimentalRoborazziApi
import com.github.takahirom.roborazzi.RoborazziOptions
import com.github.takahirom.roborazzi.ThresholdValidator
import com.github.takahirom.roborazzi.captureRoboImage
import com.google.android.horologist.compose.layout.AppScaffold
import com.google.android.horologist.images.coil.FakeImageLoader
import dev.johnoreilly.confetti.wear.app.KoinTestApp
import okio.FileSystem
import okio.Path
import org.junit.Assume
import org.junit.Rule
import org.junit.rules.TestName
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import kotlin.test.AfterTest

@RunWith(RobolectricTestRunner::class)
@Config(
    application = KoinTestApp::class,
    sdk = [33],
    qualifiers = "w221dp-h221dp-small-notlong-round-watch-xhdpi-keyshidden-nonav"
)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
abstract class BaseScreenshotTest {
    @get:Rule
    val composeRule = createComposeRule()

    @get:Rule
    val testName = TestName()

    var tolerance: Float = 0.01f

    var fakeImageLoader = FakeImageLoader.Never

    private val applicationContext: Context
        get() = ApplicationProvider.getApplicationContext<Application>()

    val resources: Resources
        get() = applicationContext.resources

    fun enableA11yTest() {
        // TODO reenable in follow up PR
        Assume.assumeTrue(false)
    }

    fun takeScreenshot(suffix: String = "") {
        composeRule.onRoot().captureRoboImage(
            filePath = "snapshot/${this.javaClass.simpleName}/${testName.methodName}$suffix.png",
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
        fun loadTestBitmap(path: Path): Bitmap = FileSystem.RESOURCES.read(path) {
            BitmapFactory.decodeStream(this.inputStream())
        }
    }

    @AfterTest
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
}
