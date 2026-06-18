package com.tradernet.quotes.baselineprofile

import androidx.benchmark.macro.CompilationMode
import androidx.benchmark.macro.FrameTimingMetric
import androidx.benchmark.macro.StartupMode
import androidx.benchmark.macro.junit4.MacrobenchmarkRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ScrollBenchmark {

    @get:Rule
    val rule = MacrobenchmarkRule()

    @Test
    fun scrollNoCompilation() = scroll(CompilationMode.None())

    @Test
    fun scrollBaselineProfile() = scroll(CompilationMode.Partial())

    private fun scroll(mode: CompilationMode) = rule.measureRepeated(
        packageName = PACKAGE_NAME,
        metrics = listOf(FrameTimingMetric()),
        iterations = 8,
        startupMode = StartupMode.WARM,
        compilationMode = mode,
    ) {
        startActivityAndWait()
        scrollQuotes()
    }
}
