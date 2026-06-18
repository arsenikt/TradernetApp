package com.tradernet.quotes.ui

import androidx.compose.runtime.Immutable
import com.tradernet.quotes.domain.MarketTab
import com.tradernet.quotes.domain.Quotir
import com.tradernet.quotes.domain.SortField

@Immutable
data class QuoteGroup(
    val market: MarketTab?,
    val tickers: List<String>,
)

internal object QuoteListBuilder {

    fun build(
        tickers: List<String>,
        quotes: Map<String, Quotir>,
        markets: Map<String, MarketTab>,
        sortField: SortField,
        descending: Boolean,
        groupByMarket: Boolean,
    ): List<QuoteGroup> {
        val ordered = order(tickers, quotes, sortField, descending)
        if (!groupByMarket) {
            return if (ordered.isEmpty()) emptyList() else listOf(QuoteGroup(null, ordered))
        }
        return group(ordered, markets)
    }

    private fun order(
        tickers: List<String>,
        quotes: Map<String, Quotir>,
        sortField: SortField,
        descending: Boolean,
    ): List<String> {
        if (sortField == SortField.DEFAULT) return tickers
        if (sortField == SortField.NAME) {
            val sorted = tickers.sortedWith(String.CASE_INSENSITIVE_ORDER)
            return if (descending) sorted.asReversed() else sorted
        }
        
        val value: (String) -> Double = when (sortField) {
            SortField.PRICE -> { ticker -> quotes[ticker]?.lastPrice ?: 0.0 }
            SortField.CHANGE -> { ticker -> quotes[ticker]?.changePoints ?: 0.0 }
            SortField.CHANGE_PERCENT -> { ticker -> quotes[ticker]?.changePercent ?: 0.0 }
            else -> { _ -> 0.0 }
        }
        val (loaded, pending) = tickers.partition { quotes[it]?.hasData == true }
        val ranked = loaded.sortedBy(value)
        return (if (descending) ranked.asReversed() else ranked) + pending
    }

    private fun group(ordered: List<String>, markets: Map<String, MarketTab>): List<QuoteGroup> {
        val byMarket = ordered.groupBy { markets[it] ?: MarketTab.ALL }
        val groups = ArrayList<QuoteGroup>(byMarket.size)
        val seen = HashSet<MarketTab>()
        fun appendGroup(market: MarketTab) {
            val tickers = byMarket[market] ?: return
            if (tickers.isEmpty() || !seen.add(market)) return
            groups += QuoteGroup(market, tickers)
        }
        MarketTab.concrete.forEach(::appendGroup)
        byMarket.keys.forEach(::appendGroup) 
        return groups
    }
}
