package dev.johnoreilly.confetti.benchmark

import android.os.ParcelFileDescriptor
import androidx.benchmark.macro.junit4.BaselineProfileRule
import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@LargeTest
class BaselineProfile {
    @get:Rule
    val baselineRule = BaselineProfileRule()

    // Wear OS emulator images run dex2oat on CPUs 2,3 (dalvik.vm.dex2oat-cpu-set) but only have
    // 2 CPUs, so compiling the app for profiling fails. They're userdebug builds where the shell
    // user can su, so point dex2oat at the CPUs that exist; on a device without su this is a no-op.
    @Before
    fun fixDex2oatCpuSet() {
        val cpus = (0 until Runtime.getRuntime().availableProcessors()).joinToString(",")
        val output = InstrumentationRegistry.getInstrumentation().uiAutomation
            .executeShellCommand("su root setprop dalvik.vm.dex2oat-cpu-set $cpus")
        // Read to EOF so setprop has finished before collection compiles the app
        ParcelFileDescriptor.AutoCloseInputStream(output).use { it.readBytes() }
    }

    @Test
    fun profile() {
        baselineRule.collect(
            packageName = "dev.johnoreilly.confetti"
        ) {
            startActivityAndWait()
        }
    }
}
