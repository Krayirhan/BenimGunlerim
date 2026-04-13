package com.benimgunlerim.benchmark

import androidx.benchmark.macro.CompilationMode
import androidx.benchmark.macro.FrameTimingMetric
import androidx.benchmark.macro.StartupMode
import androidx.benchmark.macro.junit4.MacrobenchmarkRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.uiautomator.By
import androidx.test.uiautomator.Until
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ScrollJankBenchmark {

    @get:Rule
    val benchmarkRule = MacrobenchmarkRule()

    @Ignore("FrameTiming metric needs stable renderthread slices on benchmark-targeted build/device profile.")
    @Test
    fun todayScreenScrollJank() = benchmarkRule.measureRepeated(
        packageName = TARGET_PACKAGE,
        metrics = listOf(FrameTimingMetric()),
        iterations = 8,
        startupMode = StartupMode.WARM,
        compilationMode = CompilationMode.None(),
    ) {
        device.executeShellCommand("am start -W -n $TARGET_PACKAGE/$TARGET_ACTIVITY")
        device.wait(Until.hasObject(By.pkg(TARGET_PACKAGE).depth(0)), 5_000)

        repeat(3) {
            device.swipe(540, 1800, 540, 500, 20)
            device.waitForIdle()
            device.swipe(540, 700, 540, 1900, 20)
            device.waitForIdle()
        }
    }

    companion object {
        private const val TARGET_PACKAGE = "com.benimgunlerim.debug"
        private const val TARGET_ACTIVITY = "com.benimgunlerim.MainActivity"
    }
}
