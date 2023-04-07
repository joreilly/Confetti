package dev.johnoreilly.confetti

import androidx.compose.runtime.Composable
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import dev.johnoreilly.confetti.di.KoinTestApp
import dev.johnoreilly.confetti.screenshot.RNGScreenshotTestRule
import dev.johnoreilly.confetti.ui.ConfettiTheme
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.After
import org.junit.Rule
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
    record: Boolean,
    tolerance: Float = 0.01f,
    a11yEnabled: Boolean = false
): KoinTest {

    @get:Rule
    val screenshotTestRule = createScreenshotTestRule(
        record = record,
        tolerance = tolerance,
        a11yEnabled = a11yEnabled,
        directoryName = this::class.simpleName!!
    )

    @After
    fun teardown() {
        stopKoin()
    }
}

private fun createScreenshotTestRule(
    record: Boolean,
    tolerance: Float = 0.1f,
    a11yEnabled: Boolean = false,
    directoryName: String
): RNGScreenshotTestRule {
    return ScreenshotTestRule(record, tolerance, a11yEnabled, directoryName)
}

class ScreenshotTestRule(
    record: Boolean,
    tolerance: Float,
    a11yEnabled: Boolean,
    directoryName: String,
) : RNGScreenshotTestRule(record, tolerance, a11yEnabled, directoryName) {
    @ExperimentalCoroutinesApi
    override fun takeScreenshot(
        checks: suspend (rule: ComposeContentTestRule) -> Unit,
        content: @Composable () -> Unit
    ) {
        super.takeScreenshot(
            checks,
            content = {
                ConfettiTheme {
                    content()
                }
            }
        )
    }
}
