package dev.johnoreilly.confetti.benchmark

import androidx.benchmark.macro.junit4.BaselineProfileRule
import androidx.test.filters.LargeTest
import androidx.test.uiautomator.By
import androidx.test.uiautomator.By.descContains
import androidx.test.uiautomator.Direction
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.UiObject2
import androidx.test.uiautomator.Until
import org.junit.Rule
import org.junit.Test

@LargeTest
class BaselineProfile {
    @get:Rule
    val baselineRule = BaselineProfileRule()

    @Test
    fun openAndScroll() {
        baselineRule.collectBaselineProfile(
            packageName = "dev.johnoreilly.confetti"
        ) {
            pressHome()
            startActivityAndWait()

            val kotlinConf = device.findObject(descContains("KotlinConf 2023"))

            if (kotlinConf != null) {
                kotlinConf.click()
                device.wait(Until.hasObject(descContains("Apr 13, 2023")), 5_000)
            }

            val sessionList = device.findObject(By.scrollable(true))

            device.downDownUpUp(sessionList)
        }
    }

    fun UiDevice.downDownUpUp(element: UiObject2) {
        element.setGestureMargin(displayWidth / 4)

        element.fling(Direction.DOWN)
        waitForIdle()
        element.fling(Direction.DOWN)
        waitForIdle()
        element.fling(Direction.UP)
        waitForIdle()
        element.fling(Direction.UP)
        waitForIdle()
    }
}
