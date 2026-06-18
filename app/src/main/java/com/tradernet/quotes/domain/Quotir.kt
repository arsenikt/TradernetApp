package com.tradernet.quotes.domain

import androidx.compose.runtime.Immutable
import com.tradernet.quotes.format.formatPercent
import com.tradernet.quotes.format.formatPoints
import com.tradernet.quotes.format.formatPrice

enum class PriceFlash { NONE, UP, DOWN }

private const val EM_DASH = "—"

@Immutable
data class Quotir(
    val ticker: String,
    val name: String = "",
    val exchange: String = "",
    val lastPrice: Double = 0.0,
    val changePoints: Double = 0.0,
    val changePercent: Double = 0.0,
    val minStep: Double = 0.01,
    val hasData: Boolean = false,
    val marketClosed: Boolean = false,
    val flash: PriceFlash = PriceFlash.NONE,
) {
    val priceText: String = if (hasData) formatPrice(lastPrice, minStep) else EM_DASH
    val pointsText: String = if (hasData) formatPoints(changePoints, minStep) else ""
    val percentText: String = if (hasData) formatPercent(changePercent) else ""

    val subtitle: String = when {
        exchange.isNotBlank() && name.isNotBlank() -> "$exchange | $name"
        name.isNotBlank() -> name
        exchange.isNotBlank() -> exchange
        else -> EM_DASH
    }

    fun reduce(quote: Quote): Quotir {
        val nextFlash = when {
            hasData && quote.lastPrice > lastPrice -> PriceFlash.UP
            hasData && quote.lastPrice < lastPrice -> PriceFlash.DOWN
            else -> PriceFlash.NONE
        }

        return copy(
            name = quote.name.ifBlank { name },
            exchange = quote.exchange.ifBlank { exchange },
            lastPrice = quote.lastPrice,
            changePoints = quote.changePoints,
            changePercent = quote.changePercent,
            minStep = if (quote.minStep > 0.0) quote.minStep else minStep,
            hasData = true,
            marketClosed = quote.marketStatus.isMarketClosed(),
            flash = nextFlash,
        )
    }
}

private fun String.isMarketClosed(): Boolean =
    isNotBlank() && !equals("OPEN", ignoreCase = true)
