package dev.johnoreilly.confetti

import dev.johnoreilly.confetti.screenshot.RNGScreenshotTestRule
import org.junit.After
import org.junit.Rule
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@Config(
    sdk = [30],
)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
abstract class BaseScreenshotTest(
    record: Boolean,
    tolerance: Float = 0.01f,
    a11yEnabled: Boolean = false
) {

    @get:Rule
    val screenshotTestRule = createScreenshotTestRule(
        record = record,
        tolerance = tolerance,
        a11yEnabled = a11yEnabled
    )

    @After
    fun stop() {
        stopKoin()
    }
}

private fun createScreenshotTestRule(
    record: Boolean,
    tolerance: Float = 0.1f,
    a11yEnabled: Boolean = false
): RNGScreenshotTestRule {
    return ScreenshotTestRule(record, tolerance, a11yEnabled)
}

class ScreenshotTestRule(
    record: Boolean,
    tolerance: Float,
    a11yEnabled: Boolean
): RNGScreenshotTestRule(record, tolerance, a11yEnabled)
