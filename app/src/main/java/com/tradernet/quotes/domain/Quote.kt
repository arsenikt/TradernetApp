package com.tradernet.quotes.domain

data class Quote(
    val ticker: String,
    val name: String,
    val exchange: String,
    val lastPrice: Double,
    val changePoints: Double,
    val changePercent: Double,
    val minStep: Double,
    val currency: String,
    val marketStatus: String = "",
)
