package com.tradernet.quotes.baselineprofile

import androidx.benchmark.macro.MacrobenchmarkScope
import androidx.benchmark.macro.junit4.BaselineProfileRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.uiautomator.By
import androidx.test.uiautomator.Direction
import androidx.test.uiautomator.Until
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class BaselineProfileGenerator {

    @get:Rule
    val rule = BaselineProfileRule()

    @Test
    fun generate() = rule.collect(packageName = PACKAGE_NAME) {
        startActivityAndWait()
        scrollQuotes()
    }
}

internal const val PACKAGE_NAME = "com.tradernet.quotes"

internal fun MacrobenchmarkScope.scrollQuotes() {
    val list = device.wait(Until.findObject(By.scrollable(true)), 5_000) ?: return
    list.setGestureMargin(device.displayWidth / 5)
    repeat(3) {
        list.fling(Direction.DOWN)
        device.waitForIdle()
    }
    repeat(3) {
        list.fling(Direction.UP)
        device.waitForIdle()
    }
}
