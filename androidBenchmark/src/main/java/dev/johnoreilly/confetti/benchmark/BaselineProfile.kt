package dev.johnoreilly.confetti.benchmark

import androidx.benchmark.macro.junit4.BaselineProfileRule
import androidx.test.filters.LargeTest
import androidx.test.uiautomator.By
import androidx.test.uiautomator.Direction
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.UiObject2
import androidx.test.uiautomator.Until
import org.junit.Rule
import org.junit.Test
import java.util.regex.Pattern

// Same journey as maestro/Confetti.flow: welcome screen -> conference list -> schedule.
// Conferences come from the live backend, so pick the first one rather than a name that goes stale.
@LargeTest
class BaselineProfile {
    @get:Rule
    val baselineRule = BaselineProfileRule()

    @Test
    fun openAndScroll() {
        baselineRule.collect(
            packageName = "dev.johnoreilly.confetti"
        ) {
            pressHome()
            startActivityAndWait()

            // Only shown on first launch; later iterations go straight to the conference list
            device.wait(Until.findObject(By.text("Skip")), 5_000)?.click()

            val conferenceList = device.wait(Until.findObject(By.scrollable(true)), 30_000)
                ?: error("Conference list didn't load")
            conferenceList.children.first().click()

            // The schedule's day tabs, e.g. "20 May, 2026"
            device.wait(Until.hasObject(By.text(Pattern.compile("\\d{2} \\w+, \\d{4}"))), 30_000)

            device.wait(Until.findObject(By.scrollable(true)), 10_000)?.let { device.downUp(it) }
        }
    }

    private fun UiDevice.downUp(element: UiObject2) {
        element.setGestureMargin(displayWidth / 4)

        element.fling(Direction.DOWN)
        waitForIdle()
        element.fling(Direction.UP)
        waitForIdle()
    }
}
