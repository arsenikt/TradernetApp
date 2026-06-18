package com.tradernet.quotes.ui

import android.app.Application
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.tradernet.quotes.Tickers
import com.tradernet.quotes.data.LogoRepository
import com.tradernet.quotes.data.QuotesRepository
import com.tradernet.quotes.data.SettingsRepository
import com.tradernet.quotes.domain.AppSettings
import com.tradernet.quotes.domain.DisplaySettings
import com.tradernet.quotes.domain.InstrumentType
import com.tradernet.quotes.domain.MarketTab
import com.tradernet.quotes.domain.Quotir
import com.tradernet.quotes.domain.Security
import com.tradernet.quotes.domain.SortField
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit

class QuotesViewModel(
    private val repository: QuotesRepository = QuotesRepository(),
    private val logoRepository: LogoRepository = LogoRepository(),
    private val settingsRepository: SettingsRepository? = null,
    private val defaultTickers: List<String> = Tickers.LIST,
) : ViewModel() {
    @OptIn(ExperimentalCoroutinesApi::class)
    private val worker = Dispatchers.Default.limitedParallelism(1)

    @Volatile
    private var quoteStates: Map<String, MutableState<Quotir>> = emptyMap()

    @Volatile
    private var logoStates: Map<String, MutableState<ImageBitmap?>> = emptyMap()

    private val fallbackQuote: State<Quotir> = mutableStateOf(Quotir(ticker = ""))
    private val fallbackLogo: State<ImageBitmap?> = mutableStateOf(null)

    private val current = HashMap<String, Quotir>()

    private val dirty = HashSet<String>()

    private val scrolling = MutableStateFlow(false)

    private var membership: List<String> = emptyList()
    private var securityMarket: Map<String, MarketTab> = emptyMap()

    private var appliedSort: SortField = SortField.DEFAULT
    private var appliedDescending: Boolean = true
    private var appliedGroup: Boolean = false

    private var lastFeedKey: FeedKey? = null

    var displayGroups: List<QuoteGroup> by mutableStateOf(emptyList())
        private set

    val tabs: List<MarketTab> = MarketTab.entries

    var tabsVisible: Boolean by mutableStateOf(false)
        private set

    var selectedMarkets: Set<MarketTab> by mutableStateOf(MarketTab.DEFAULT_SELECTION)
        private set

    var display: DisplaySettings by mutableStateOf(DisplaySettings.Default)
        private set

    var loading: Boolean by mutableStateOf(false)
        private set

    val connected: StateFlow<Boolean> = repository.connected

    private val settingsFlow: Flow<AppSettings> =
        settingsRepository?.settings ?: flowOf(AppSettings.Default)

    init {
        seed(defaultTickers, marketsFor(defaultTickers))

        viewModelScope.launch {
            settingsFlow.collect { settings ->
                tabsVisible = settings.extendedMode
                selectedMarkets = settings.effectiveSelectedMarkets
                display = settings.display
            }
        }

        viewModelScope.launch(worker) {
            launch { flushWhenScrollSettles() }
            launch { resortWhileLive() }
            launch { observeOrderSettings() }

            settingsFlow
                .map { settings -> settings.toFeedKey() }
                .distinctUntilChanged()
                .collectLatest { key -> streamMarket(key) }
        }
    }

    private data class FeedKey(val markets: Set<MarketTab>, val type: InstrumentType)

    private fun AppSettings.toFeedKey(): FeedKey? =
        if (extendedMode) FeedKey(selectedMarkets, instrumentType) else null

    private suspend fun observeOrderSettings() {
        settingsFlow.collect { settings ->
            val nextSort = settings.effectiveSortField
            val nextDescending = settings.sortDescending
            val nextGroup = settings.effectiveGroupByMarket
            val changed = nextSort != appliedSort ||
                nextDescending != appliedDescending ||
                nextGroup != appliedGroup
            appliedSort = nextSort
            appliedDescending = nextDescending
            appliedGroup = nextGroup

            val feedKey = settings.toFeedKey()
            val feedChanging = feedKey != lastFeedKey
            lastFeedKey = feedKey
            if (changed && !feedChanging) recomputeDisplay()
        }
    }

    private suspend fun streamMarket(key: FeedKey?) = coroutineScope {
        loading = true
        val securities: List<Security> = if (key == null) {
            defaultTickers.map { Security(it, MarketTab.forTicker(it)) }
        } else {
            repository.marketSecurities(key.markets, key.type)
        }
        val active = securities.map { it.ticker }
        seed(active, securities.associate { it.ticker to it.market })
        val carried = HashMap(current)
        current.clear()
        dirty.clear()
        active.forEach { current[it] = carried[it] ?: Quotir(ticker = it) }
        recomputeDisplay()
        loading = false

        launch(Dispatchers.IO) { loadLogos(active) }

        repository.quotes(active).collect { quote ->
            val reduced = (current[quote.ticker] ?: return@collect).reduce(quote)
            current[quote.ticker] = reduced
            if (scrolling.value) {
                dirty += quote.ticker
            } else {
                quoteStates[quote.ticker]?.value = reduced
            }
        }
    }

    fun onToggleMarket(tab: MarketTab) {
        val next = MarketTab.toggleSelection(selectedMarkets, tab)
        selectedMarkets = next
        display = display.copy(markets = next)
        viewModelScope.launch { settingsRepository?.setSelectedMarkets(next) }
    }

    fun applyDisplaySettings(settings: DisplaySettings) {
        viewModelScope.launch { settingsRepository?.setDisplaySettings(settings) }
    }

    fun setScrolling(value: Boolean) {
        scrolling.value = value
    }

    fun quoteState(ticker: String): State<Quotir> = quoteStates[ticker] ?: fallbackQuote

    fun logoState(ticker: String): State<ImageBitmap?> = logoStates[ticker] ?: fallbackLogo

    private suspend fun flushWhenScrollSettles() {
        scrolling.collectLatest { isScrolling ->
            if (isScrolling || dirty.isEmpty()) return@collectLatest
            delay(SCROLL_SETTLE_MS)
            dirty.forEach { ticker -> quoteStates[ticker]?.value = current.getValue(ticker) }
            dirty.clear()
            recomputeDisplay()
        }
    }

    private suspend fun resortWhileLive() {
        while (true) {
            delay(RESORT_INTERVAL_MS)
            if (appliedSort != SortField.DEFAULT && !scrolling.value && membership.isNotEmpty()) {
                recomputeDisplay()
            }
        }
    }

    private fun recomputeDisplay() {
        val groups = QuoteListBuilder.build(
            tickers = membership,
            quotes = current,
            markets = securityMarket,
            sortField = appliedSort,
            descending = appliedDescending,
            groupByMarket = appliedGroup,
        )
        if (groups != displayGroups) displayGroups = groups
    }

    private fun seed(list: List<String>, markets: Map<String, MarketTab>) {
        val previousQuotes = quoteStates
        val previousLogos = logoStates
        quoteStates = list.associateWith { previousQuotes[it] ?: mutableStateOf(Quotir(ticker = it)) }
        logoStates = list.associateWith { previousLogos[it] ?: mutableStateOf<ImageBitmap?>(null) }
        membership = list
        securityMarket = markets
        displayGroups = if (list.isEmpty()) emptyList() else listOf(QuoteGroup(null, list))
    }

    private fun marketsFor(tickers: List<String>): Map<String, MarketTab> =
        tickers.associateWith { MarketTab.forTicker(it) }

    private suspend fun loadLogos(active: List<String>) = coroutineScope {
        val gate = Semaphore(LOGO_CONCURRENCY)
        active.forEach { ticker ->
            if (logoStates[ticker]?.value != null) return@forEach
            launch(Dispatchers.IO) {
                gate.withPermit {
                    val bitmap = logoRepository.logo(ticker, LOGO_SIZE_PX) ?: return@withPermit
                    logoStates[ticker]?.value = bitmap.asImageBitmap()
                }
            }
        }
    }

    companion object {
        private const val LOGO_SIZE_PX = 120
        private const val SCROLL_SETTLE_MS = 120L
        private const val RESORT_INTERVAL_MS = 1_000L
        private const val LOGO_CONCURRENCY = 8

        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as Application
                QuotesViewModel(settingsRepository = SettingsRepository(app.applicationContext))
            }
        }
    }
}
