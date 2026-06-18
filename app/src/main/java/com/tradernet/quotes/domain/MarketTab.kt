package com.tradernet.quotes.domain

enum class MarketTab(val exchanges: List<String>) {
    ALL(listOf("russia", "usa", "europe")),
    RUSSIA(listOf("russia")),
    US(listOf("usa")),
    EUROPE(listOf("europe")),
    ;

    companion object {
        val concrete: List<MarketTab> = listOf(RUSSIA, US, EUROPE)

        val DEFAULT_SELECTION: Set<MarketTab> = setOf(ALL)

        fun exchangesFor(markets: Set<MarketTab>): List<String> =
            markets.flatMap { it.exchanges }.distinct()

        fun toggleSelection(current: Set<MarketTab>, tab: MarketTab): Set<MarketTab> {
            if (tab == ALL) return setOf(ALL)
            val base = current - ALL
            val next = if (tab in base) base - tab else base + tab
            return next.ifEmpty { setOf(ALL) }
        }

        fun forExchange(exchange: String): MarketTab = when (exchange.lowercase()) {
            "russia" -> RUSSIA
            "usa" -> US
            "europe" -> EUROPE
            else -> ALL
        }

        fun forTicker(ticker: String): MarketTab = when {
            ticker.endsWith(".US", ignoreCase = true) -> US
            ticker.endsWith(".EU", ignoreCase = true) -> EUROPE
            ticker.contains('.') -> ALL
            else -> RUSSIA
        }
    }
}
