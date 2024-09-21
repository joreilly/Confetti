@file:OptIn(ExperimentalRoborazziApi::class, ExperimentalCoilApi::class)

package dev.johnoreilly.confetti

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import coil.ImageLoader
import coil.annotation.ExperimentalCoilApi
import coil.compose.LocalImageLoader
import coil.test.FakeImageLoaderEngine
import com.github.takahirom.roborazzi.ExperimentalRoborazziApi
import com.github.takahirom.roborazzi.RoborazziOptions
import com.github.takahirom.roborazzi.captureRoboImage
import dev.johnoreilly.confetti.di.KoinTestApp
import dev.johnoreilly.confetti.ui.ConfettiTheme
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.PreviewContextConfigurationEffect
import org.junit.After
import org.junit.Rule
import org.junit.rules.TestName
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin
import org.koin.test.KoinTest
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@Config(
    application = KoinTestApp::class,
    sdk = [30],
)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
abstract class BaseScreenshotTest(
) : KoinTest {

    @get:Rule
    val composeRule: ComposeContentTestRule = createComposeRule()

    @get:Rule
    val testName = TestName()

    open val tolerance: Float = 0.05f

    public open val imageLoader: FakeImageLoaderEngine? = null

    @OptIn(ExperimentalResourceApi::class)
    @ExperimentalCoroutinesApi
    fun takeScreenshot(
        darkTheme: Boolean,
        disableDynamicTheming: Boolean,
        checks: suspend (rule: ComposeContentTestRule) -> Unit,
        content: @Composable () -> Unit
    ) = runTest {
        composeRule.setContent {
            TestScaffold {
                CompositionLocalProvider(LocalInspectionMode provides true) {
                    // https://github.com/robolectric/robolectric/issues/9603
                    PreviewContextConfigurationEffect() // This sets the context required the compose resources
                    content()
                }
            }
        }

        composeRule.onRoot()
            .captureRoboImage(
                filePath = filename(), roborazziOptions = RoborazziOptions(
                    compareOptions = RoborazziOptions.CompareOptions(changeThreshold = tolerance)
                )
            )

        checks(composeRule)
    }

    open fun filename() = "snapshot/${this.javaClass.simpleName}/${testName.methodName}.png"

    @After
    fun teardown() {
        stopKoin()
    }

    @Composable
    open fun TestScaffold(content: @Composable () -> Unit) {
        ConfettiTheme(
            darkTheme = true,
            androidTheme = true,
            disableDynamicTheming = true
        ) {
            Box(modifier = Modifier.background(MaterialTheme.colorScheme.background)) {
                content()
            }
        }
    }

    companion object {
        @Suppress("DEPRECATION")
        @Composable
        public fun withImageLoader(
            imageLoaderEngine: FakeImageLoaderEngine?,
            content: @Composable () -> Unit,
        ) {
            if (imageLoaderEngine == null) {
                content()
            } else {
                val imageLoader = ImageLoader.Builder(LocalContext.current)
                    .components { add(imageLoaderEngine) }
                    .build()
                CompositionLocalProvider(LocalImageLoader provides imageLoader) {
                    content()
                }
            }
        }
    }
}
