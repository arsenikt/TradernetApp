package com.tradernet.quotes.ui

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.tradernet.quotes.R
import com.tradernet.quotes.domain.InstrumentType
import com.tradernet.quotes.domain.MarketTab
import com.tradernet.quotes.domain.SortField

@StringRes
internal fun MarketTab.labelRes(): Int = when (this) {
    MarketTab.ALL -> R.string.market_tab_all
    MarketTab.RUSSIA -> R.string.market_tab_russia
    MarketTab.US -> R.string.market_tab_us
    MarketTab.EUROPE -> R.string.market_tab_europe
}

@DrawableRes
internal fun MarketTab.flagRes(): Int? = when (this) {
    MarketTab.ALL -> null
    MarketTab.RUSSIA -> R.drawable.ic_flag_ru
    MarketTab.US -> R.drawable.ic_flag_us
    MarketTab.EUROPE -> R.drawable.ic_flag_eu
}

@StringRes
internal fun InstrumentType.labelRes(): Int = when (this) {
    InstrumentType.ALL -> R.string.instrument_type_all
    InstrumentType.STOCKS -> R.string.instrument_type_stocks
    InstrumentType.BONDS -> R.string.instrument_type_bonds
    InstrumentType.FUNDS -> R.string.instrument_type_funds
}

@StringRes
internal fun SortField.labelRes(): Int = when (this) {
    SortField.NAME -> R.string.sort_name
    SortField.PRICE -> R.string.sort_price
    SortField.CHANGE -> R.string.sort_change
    SortField.CHANGE_PERCENT -> R.string.sort_change_percent
    SortField.DEFAULT -> R.string.sort_name
}
