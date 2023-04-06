package dev.johnoreilly.confetti

import dev.johnoreilly.confetti.screenshot.createScreenshotTestRule
import org.junit.After
import org.junit.Rule
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin
import org.koin.test.KoinTest
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

// TODO: Add qualifiers
@RunWith(RobolectricTestRunner::class)
@Config(
    sdk = [30],

)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
abstract class BaseScreenshot(record: Boolean): KoinTest {

    @get:Rule
    val screenshotTestRule = createScreenshotTestRule(record = record)

    @After
    fun stop() {
        stopKoin()
    }
}
