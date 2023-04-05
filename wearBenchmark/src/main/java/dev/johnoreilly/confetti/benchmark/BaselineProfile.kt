package dev.johnoreilly.confetti.benchmark

import androidx.benchmark.macro.junit4.BaselineProfileRule
import androidx.test.filters.LargeTest
import org.junit.Rule
import org.junit.Test

@LargeTest
class BaselineProfile {
    @get:Rule
    val baselineRule = BaselineProfileRule()

    @Test
    fun profile() {
        baselineRule.collectBaselineProfile(
            packageName = "dev.johnoreilly.confetti"
        ) {
            startActivityAndWait()
        }
    }
}
