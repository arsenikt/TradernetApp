package com.tradernet.quotes.domain

enum class InstrumentType(val apiTypes: List<String>) {
    ALL(listOf("stocks", "bonds", "funds")),
    STOCKS(listOf("stocks")),
    BONDS(listOf("bonds")),
    FUNDS(listOf("funds")),
}
