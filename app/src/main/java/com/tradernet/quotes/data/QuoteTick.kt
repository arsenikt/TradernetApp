package com.tradernet.quotes.data

data class QuoteTick(
    val ticker: String,
    val lastPrice: Double? = null,
    val changePoints: Double? = null,
    val changePercent: Double? = null,
    val exchange: String? = null,
    val name: String? = null,
    val minStep: Double? = null,
    val prevClose: Double? = null,
    val marketStatus: String? = null,
    val isSnapshot: Boolean = false,
)
