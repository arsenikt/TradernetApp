package com.tradernet.quotes.data

import com.tradernet.quotes.domain.InstrumentType
import com.tradernet.quotes.domain.MarketTab
import com.tradernet.quotes.domain.Quote
import com.tradernet.quotes.domain.Security
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.channelFlow

class QuotesRepository(
    private val socket: QuotesSocketClient = QuotesSocketClient(),
    private val topSecuritiesClient: TopSecuritiesClient = TopSecuritiesClient(),
) {
    val connected: StateFlow<Boolean> get() = socket.connected

    /**
     * Top securities for the selected [markets], narrowed to [type]. Each ticker is
     * tagged with the concrete market it came from (one request per exchange × API
     * type), and duplicates that span markets/types/exchanges keep their first
     * occurrence.
     */
    suspend fun marketSecurities(markets: Set<MarketTab>, type: InstrumentType): List<Security> =
        coroutineScope {
            MarketTab.exchangesFor(markets)
                .flatMap { exchange -> type.apiTypes.map { apiType -> exchange to apiType } }
                .map { (exchange, apiType) ->
                    async {
                        topSecuritiesClient.tickers(exchange, apiType)
                            .map { ticker -> Security(ticker, MarketTab.forExchange(exchange)) }
                    }
                }
                .awaitAll()
                .flatten()
                .distinctBy { it.ticker }
        }

    private class InstrumentState {
        var name: String = ""
        var exchange: String = ""
        var lastPrice: Double = 0.0
        var minStep: Double = 0.01
        var referenceClose: Double = 0.0
        var hasReference: Boolean = false
        var marketStatus: String = ""
        var emittedOnce: Boolean = false
        var lastEmittedPrice: Double = Double.NaN
        var lastEmittedChange: Double = Double.NaN
        var lastEmittedStatus: String = ""
    }

    fun quotes(tickers: List<String>): Flow<Quote> = channelFlow {
        val states = HashMap<String, InstrumentState>()

        socket.quoteTicks(tickers).collect { tick ->
            val state = states.getOrPut(tick.ticker) { InstrumentState() }

            tick.name?.let { if (it.isNotBlank()) state.name = it }
            tick.exchange?.let { if (it.isNotBlank()) state.exchange = it }
            tick.minStep?.let { if (it > 0.0) state.minStep = it }
            tick.marketStatus?.let { if (it.isNotBlank()) state.marketStatus = it }

            if (tick.lastPrice != null && tick.changePoints != null) {
                state.referenceClose = tick.lastPrice - tick.changePoints
                state.hasReference = true
            } else if (!state.hasReference && tick.prevClose != null && tick.prevClose > 0.0) {
                state.referenceClose = tick.prevClose
                state.hasReference = true
            }
            tick.lastPrice?.let { state.lastPrice = it }

            val price = state.lastPrice
            val change = if (state.hasReference) price - state.referenceClose
            else tick.changePoints ?: 0.0
            val percent = if (state.hasReference && state.referenceClose != 0.0) {
                change / state.referenceClose * 100.0
            } else {
                tick.changePercent ?: 0.0
            }

            val priceChanged = state.lastEmittedPrice.isNaN() || price != state.lastEmittedPrice
            val changeChanged = state.lastEmittedChange.isNaN() || change != state.lastEmittedChange
            val statusChanged = state.marketStatus != state.lastEmittedStatus
            if (!state.emittedOnce || priceChanged || changeChanged || statusChanged) {
                state.emittedOnce = true
                state.lastEmittedPrice = price
                state.lastEmittedChange = change
                state.lastEmittedStatus = state.marketStatus
                send(
                    Quote(
                        ticker = tick.ticker,
                        name = state.name,
                        exchange = state.exchange,
                        lastPrice = price,
                        changePoints = change,
                        changePercent = percent,
                        minStep = state.minStep,
                        currency = "",
                        marketStatus = state.marketStatus,
                    ),
                )
            }
        }
    }
}
