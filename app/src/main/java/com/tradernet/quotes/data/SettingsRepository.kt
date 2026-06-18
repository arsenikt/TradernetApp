package com.tradernet.quotes.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.tradernet.quotes.domain.AppSettings
import com.tradernet.quotes.domain.DisplaySettings
import com.tradernet.quotes.domain.InstrumentType
import com.tradernet.quotes.domain.MarketTab
import com.tradernet.quotes.domain.SortField
import com.tradernet.quotes.domain.ThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

private val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class SettingsRepository(private val context: Context) {

    val settings: Flow<AppSettings> = context.settingsDataStore.data
        .catch { error ->
            if (error is IOException) emit(emptyPreferences()) else throw error
        }
        .map { prefs ->
            AppSettings(
                extendedMode = prefs[Keys.ExtendedMode] ?: AppSettings.Default.extendedMode,
                themeMode = prefs[Keys.ThemeMode].toThemeMode(),
                selectedMarkets = prefs[Keys.SelectedMarkets].toMarketTabs(),
                instrumentType = prefs[Keys.InstrumentType].toInstrumentType(),
                sortField = prefs[Keys.SortField].toSortField(),
                sortDescending = prefs[Keys.SortDescending] ?: AppSettings.Default.sortDescending,
                groupByMarket = prefs[Keys.GroupByMarket] ?: AppSettings.Default.groupByMarket,
            )
        }

    suspend fun setExtendedMode(enabled: Boolean) {
        context.settingsDataStore.edit { prefs -> prefs[Keys.ExtendedMode] = enabled }
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        context.settingsDataStore.edit { prefs -> prefs[Keys.ThemeMode] = mode.name }
    }

    suspend fun setSelectedMarkets(markets: Set<MarketTab>) {
        context.settingsDataStore.edit { prefs -> prefs[Keys.SelectedMarkets] = markets.encode() }
    }

    /** Commits the full set of display settings in a single atomic edit. */
    suspend fun setDisplaySettings(display: DisplaySettings) {
        context.settingsDataStore.edit { prefs ->
            prefs[Keys.SelectedMarkets] = display.markets.encode()
            prefs[Keys.InstrumentType] = display.instrumentType.name
            prefs[Keys.SortField] = display.sortField.name
            prefs[Keys.SortDescending] = display.sortDescending
            prefs[Keys.GroupByMarket] = display.groupByMarket
        }
    }

    private fun String?.toThemeMode(): ThemeMode =
        ThemeMode.entries.firstOrNull { it.name == this } ?: AppSettings.Default.themeMode

    private fun Set<MarketTab>.encode(): Set<String> = mapTo(HashSet()) { it.name }

    private fun Set<String>?.toMarketTabs(): Set<MarketTab> {
        if (this.isNullOrEmpty()) return AppSettings.Default.selectedMarkets
        val parsed = mapNotNull { name -> MarketTab.entries.firstOrNull { it.name == name } }.toSet()
        return parsed.ifEmpty { AppSettings.Default.selectedMarkets }
    }

    private fun String?.toInstrumentType(): InstrumentType =
        InstrumentType.entries.firstOrNull { it.name == this } ?: AppSettings.Default.instrumentType

    private fun String?.toSortField(): SortField =
        SortField.entries.firstOrNull { it.name == this } ?: AppSettings.Default.sortField

    private object Keys {
        val ExtendedMode = booleanPreferencesKey("extended_mode")
        val ThemeMode = stringPreferencesKey("theme_mode")
        val SelectedMarkets = stringSetPreferencesKey("selected_markets")
        val InstrumentType = stringPreferencesKey("instrument_type")
        val SortField = stringPreferencesKey("sort_field")
        val SortDescending = booleanPreferencesKey("sort_descending")
        val GroupByMarket = booleanPreferencesKey("group_by_market")
    }
}
