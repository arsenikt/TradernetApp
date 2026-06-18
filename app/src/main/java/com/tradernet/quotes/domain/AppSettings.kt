package com.tradernet.quotes.domain

import androidx.compose.runtime.Immutable

@Immutable
data class AppSettings(
    val extendedMode: Boolean = false,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val selectedMarkets: Set<MarketTab> = MarketTab.DEFAULT_SELECTION,
    val instrumentType: InstrumentType = InstrumentType.ALL,
    val sortField: SortField = SortField.DEFAULT,
    val sortDescending: Boolean = true,
    val groupByMarket: Boolean = false,
) {
    val effectiveThemeMode: ThemeMode
        get() = if (extendedMode) themeMode else ThemeMode.SYSTEM

    val effectiveSelectedMarkets: Set<MarketTab>
        get() = if (extendedMode) selectedMarkets else MarketTab.DEFAULT_SELECTION

    val effectiveInstrumentType: InstrumentType
        get() = if (extendedMode) instrumentType else InstrumentType.ALL

    val effectiveSortField: SortField
        get() = if (extendedMode) sortField else SortField.DEFAULT

    val effectiveGroupByMarket: Boolean
        get() = if (extendedMode) groupByMarket else false

    val display: DisplaySettings
        get() = DisplaySettings(
            markets = selectedMarkets,
            instrumentType = instrumentType,
            sortField = sortField,
            sortDescending = sortDescending,
            groupByMarket = groupByMarket,
        )

    companion object {
        val Default = AppSettings()
    }
}

@Immutable
data class DisplaySettings(
    val markets: Set<MarketTab> = MarketTab.DEFAULT_SELECTION,
    val instrumentType: InstrumentType = InstrumentType.ALL,
    val sortField: SortField = SortField.DEFAULT,
    val sortDescending: Boolean = true,
    val groupByMarket: Boolean = false,
) {
    companion object {
        val Default = DisplaySettings()
    }
}
