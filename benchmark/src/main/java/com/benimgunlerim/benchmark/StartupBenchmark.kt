package com.benimgunlerim.benchmark

import androidx.benchmark.macro.CompilationMode
import androidx.benchmark.macro.StartupMode
import androidx.benchmark.macro.StartupTimingMetric
import androidx.benchmark.macro.junit4.MacrobenchmarkRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class StartupBenchmark {

    @get:Rule
    val benchmarkRule = MacrobenchmarkRule()

    @Test
    fun coldStartup() = benchmarkRule.measureRepeated(
        packageName = TARGET_PACKAGE,
        metrics = listOf(StartupTimingMetric()),
        iterations = 10,
        startupMode = StartupMode.COLD,
        compilationMode = CompilationMode.None(),
    ) {
        pressHome()
        device.executeShellCommand("am start -W -n $TARGET_PACKAGE/$TARGET_ACTIVITY")
        device.waitForIdle()
    }

    @Ignore("Warm startup metric extraction is flaky on current device profile; keep as deep-gate candidate.")
    @Test
    fun warmStartup() = benchmarkRule.measureRepeated(
        packageName = TARGET_PACKAGE,
        metrics = listOf(StartupTimingMetric()),
        iterations = 10,
        startupMode = StartupMode.WARM,
        compilationMode = CompilationMode.None(),
    ) {
        pressHome()
        device.executeShellCommand("am start -W -n $TARGET_PACKAGE/$TARGET_ACTIVITY")
        device.waitForIdle()
    }

    companion object {
        private const val TARGET_PACKAGE = "com.benimgunlerim.debug"
        private const val TARGET_ACTIVITY = "com.benimgunlerim.MainActivity"
    }
}
