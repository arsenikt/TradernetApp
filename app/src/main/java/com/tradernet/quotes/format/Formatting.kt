package com.tradernet.quotes.format

import java.util.Locale
import kotlin.math.abs
import kotlin.math.roundToLong

fun decimalsForStep(step: Double): Int {
    if (step <= 0.0) return 2
    if (step >= 1.0) return 0
    var decimals = 0
    var value = step
    while (value < 1.0 && decimals < 8) {
        value *= 10.0
        decimals++
    }
    return decimals
}

fun snapToStep(value: Double, step: Double): Double {
    if (step <= 0.0) return value
    return (value / step).roundToLong() * step
}

fun formatPrice(value: Double, step: Double): String {
    val decimals = decimalsForStep(step)
    return String.format(Locale.US, "%,.${decimals}f", snapToStep(value, step))
}

fun formatPoints(value: Double, step: Double): String {
    val decimals = decimalsForStep(step)
    val snapped = snapToStep(value, step)
    val sign = if (snapped > 0.0) "+" else if (snapped < 0.0) "-" else ""
    return sign + String.format(Locale.US, "%,.${decimals}f", abs(snapped))
}

fun formatPercent(value: Double): String {
    val sign = if (value > 0.0) "+" else if (value < 0.0) "-" else ""
    return sign + String.format(Locale.US, "%.2f", abs(value)) + "%"
}
