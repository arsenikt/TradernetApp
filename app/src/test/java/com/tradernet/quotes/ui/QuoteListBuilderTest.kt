package com.tradernet.quotes.ui

import com.tradernet.quotes.domain.MarketTab
import com.tradernet.quotes.domain.Quotir
import com.tradernet.quotes.domain.SortField
import org.junit.Assert.assertEquals
import org.junit.Test

class QuoteListBuilderTest {

    private fun quote(
        ticker: String,
        price: Double = 0.0,
        change: Double = 0.0,
        percent: Double = 0.0,
        hasData: Boolean = true,
    ) = Quotir(
        ticker = ticker,
        lastPrice = price,
        changePoints = change,
        changePercent = percent,
        hasData = hasData,
    )

    private fun build(
        tickers: List<String>,
        quotes: Map<String, Quotir> = emptyMap(),
        markets: Map<String, MarketTab> = emptyMap(),
        sortField: SortField = SortField.DEFAULT,
        descending: Boolean = true,
        groupByMarket: Boolean = false,
    ) = QuoteListBuilder.build(tickers, quotes, markets, sortField, descending, groupByMarket)

    @Test
    fun `default sort preserves feed order in a single group`() {
        val result = build(listOf("C", "A", "B"))
        assertEquals(listOf(QuoteGroup(null, listOf("C", "A", "B"))), result)
    }

    @Test
    fun `empty input yields no groups`() {
        assertEquals(emptyList<QuoteGroup>(), build(emptyList()))
        assertEquals(emptyList<QuoteGroup>(), build(emptyList(), groupByMarket = true))
    }

    @Test
    fun `name sort orders by ticker case-insensitively`() {
        val result = build(listOf("MSFT", "aapl", "GAZP"), sortField = SortField.NAME, descending = false)
        assertEquals(listOf("aapl", "GAZP", "MSFT"), result.single().tickers)
    }

    @Test
    fun `name sort descending reverses ticker order`() {
        val result = build(listOf("MSFT", "aapl", "GAZP"), sortField = SortField.NAME, descending = true)
        assertEquals(listOf("MSFT", "GAZP", "aapl"), result.single().tickers)
    }

    @Test
    fun `price sort descending puts the highest price first`() {
        val quotes = mapOf(
            "A" to quote("A", price = 10.0),
            "B" to quote("B", price = 30.0),
            "C" to quote("C", price = 20.0),
        )
        val result = build(listOf("A", "B", "C"), quotes, sortField = SortField.PRICE, descending = true)
        assertEquals(listOf("B", "C", "A"), result.single().tickers)
    }

    @Test
    fun `price sort ascending puts the lowest price first`() {
        val quotes = mapOf(
            "A" to quote("A", price = 10.0),
            "B" to quote("B", price = 30.0),
            "C" to quote("C", price = 20.0),
        )
        val result = build(listOf("A", "B", "C"), quotes, sortField = SortField.PRICE, descending = false)
        assertEquals(listOf("A", "C", "B"), result.single().tickers)
    }

    @Test
    fun `not-yet-loaded rows stay at the bottom regardless of direction`() {
        val quotes = mapOf(
            "A" to quote("A", price = 5.0, hasData = true),
            "B" to quote("B", hasData = false),
            "C" to quote("C", price = 10.0, hasData = true),
        )
        val desc = build(listOf("A", "B", "C"), quotes, sortField = SortField.PRICE, descending = true)
        assertEquals(listOf("C", "A", "B"), desc.single().tickers)

        val asc = build(listOf("A", "B", "C"), quotes, sortField = SortField.PRICE, descending = false)
        assertEquals(listOf("A", "C", "B"), asc.single().tickers)
    }

    @Test
    fun `change percent sort ranks by percent`() {
        val quotes = mapOf(
            "A" to quote("A", percent = -1.0),
            "B" to quote("B", percent = 5.0),
            "C" to quote("C", percent = 2.0),
        )
        val result = build(listOf("A", "B", "C"), quotes, sortField = SortField.CHANGE_PERCENT, descending = true)
        assertEquals(listOf("B", "C", "A"), result.single().tickers)
    }

    @Test
    fun `grouping orders markets russia us europe and keeps members`() {
        val markets = mapOf(
            "A" to MarketTab.RUSSIA,
            "B" to MarketTab.US,
            "C" to MarketTab.EUROPE,
            "D" to MarketTab.RUSSIA,
        )
        val result = build(listOf("B", "A", "D", "C"), markets = markets, groupByMarket = true)
        assertEquals(
            listOf(
                QuoteGroup(MarketTab.RUSSIA, listOf("A", "D")),
                QuoteGroup(MarketTab.US, listOf("B")),
                QuoteGroup(MarketTab.EUROPE, listOf("C")),
            ),
            result,
        )
    }

    @Test
    fun `grouping omits markets with no members`() {
        val markets = mapOf("A" to MarketTab.US, "B" to MarketTab.US)
        val result = build(listOf("A", "B"), markets = markets, groupByMarket = true)
        assertEquals(listOf(QuoteGroup(MarketTab.US, listOf("A", "B"))), result)
    }

    @Test
    fun `grouping applies the sort within each market`() {
        val quotes = mapOf(
            "A" to quote("A", price = 10.0),
            "B" to quote("B", price = 30.0),
            "C" to quote("C", price = 20.0),
        )
        val markets = mapOf("A" to MarketTab.RUSSIA, "B" to MarketTab.US, "C" to MarketTab.RUSSIA)
        val result = build(
            listOf("A", "B", "C"),
            quotes,
            markets,
            sortField = SortField.PRICE,
            descending = true,
            groupByMarket = true,
        )
        assertEquals(
            listOf(
                QuoteGroup(MarketTab.RUSSIA, listOf("C", "A")),
                QuoteGroup(MarketTab.US, listOf("B")),
            ),
            result,
        )
    }
}
