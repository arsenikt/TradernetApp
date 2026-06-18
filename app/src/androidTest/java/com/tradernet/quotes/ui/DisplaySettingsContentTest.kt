package com.tradernet.quotes.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.onNodeWithTag
import com.tradernet.quotes.domain.DisplaySettings
import com.tradernet.quotes.domain.InstrumentType
import com.tradernet.quotes.domain.MarketTab
import com.tradernet.quotes.domain.SortField
import com.tradernet.quotes.ui.theme.TradernetTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import androidx.test.ext.junit.runners.AndroidJUnit4

@RunWith(AndroidJUnit4::class)
class DisplaySettingsContentTest {

    @get:Rule
    val rule = createComposeRule()

    private fun setContent(
        initial: DisplaySettings = DisplaySettings.Default,
        onApply: (DisplaySettings) -> Unit = {},
    ) {
        rule.setContent {
            TradernetTheme {
                DisplaySettingsContent(initial = initial, onApply = onApply)
            }
        }
    }

    private fun tap(tag: String) {
        rule.onNodeWithTag(tag).performScrollTo().performClick()
    }

    @Test
    fun appliesSelectedMarketTypeAndSort() {
        var applied: DisplaySettings? = null
        setContent(onApply = { applied = it })

        tap(marketTag(MarketTab.US))
        tap(typeTag(InstrumentType.STOCKS))
        tap(sortTag(SortField.PRICE))
        tap(TAG_DISPLAY_APPLY)

        assertEquals(
            DisplaySettings(
                markets = setOf(MarketTab.US),
                instrumentType = InstrumentType.STOCKS,
                sortField = SortField.PRICE,
                sortDescending = true,
                groupByMarket = false,
            ),
            applied,
        )
    }

    @Test
    fun selectsMultipleMarkets() {
        var applied: DisplaySettings? = null
        setContent(onApply = { applied = it })

        // Default is ALL; tapping concrete markets builds a multi-market selection.
        tap(marketTag(MarketTab.RUSSIA))
        tap(marketTag(MarketTab.EUROPE))
        tap(TAG_DISPLAY_APPLY)

        assertEquals(setOf(MarketTab.RUSSIA, MarketTab.EUROPE), applied?.markets)
    }

    @Test
    fun deselectingTheLastMarketFallsBackToAll() {
        var applied: DisplaySettings? = null
        setContent(initial = DisplaySettings(markets = setOf(MarketTab.US)), onApply = { applied = it })

        tap(marketTag(MarketTab.US)) // toggling the only market off should restore ALL
        tap(TAG_DISPLAY_APPLY)

        assertEquals(setOf(MarketTab.ALL), applied?.markets)
    }

    @Test
    fun numericSortDefaultsToDescendingAndTogglesOnRetap() {
        var applied: DisplaySettings? = null
        setContent(onApply = { applied = it })

        tap(sortTag(SortField.PRICE)) // select -> descending by default
        tap(sortTag(SortField.PRICE)) // re-tap -> toggle to ascending
        tap(TAG_DISPLAY_APPLY)

        assertEquals(SortField.PRICE, applied?.sortField)
        assertFalse("Re-tapping the selected sort should flip direction", applied!!.sortDescending)
    }

    @Test
    fun nameSortDefaultsToAscending() {
        var applied: DisplaySettings? = null
        setContent(onApply = { applied = it })

        tap(sortTag(SortField.NAME))
        tap(TAG_DISPLAY_APPLY)

        assertEquals(SortField.NAME, applied?.sortField)
        assertFalse("Name sort should default to ascending", applied!!.sortDescending)
    }

    @Test
    fun groupToggleEnablesGrouping() {
        var applied: DisplaySettings? = null
        setContent(onApply = { applied = it })

        tap(TAG_DISPLAY_GROUP)
        tap(TAG_DISPLAY_APPLY)

        assertTrue(applied!!.groupByMarket)
    }

    @Test
    fun resetRestoresDefaults() {
        var applied: DisplaySettings? = null
        setContent(
            initial = DisplaySettings(
                markets = setOf(MarketTab.EUROPE),
                instrumentType = InstrumentType.BONDS,
                sortField = SortField.CHANGE,
                sortDescending = false,
                groupByMarket = true,
            ),
            onApply = { applied = it },
        )

        tap(TAG_DISPLAY_RESET)
        tap(TAG_DISPLAY_APPLY)

        assertEquals(DisplaySettings.Default, applied)
    }

    @Test
    fun bondsAndFundsTypeChipsArePresent() {
        setContent()
        rule.onNodeWithTag(typeTag(InstrumentType.BONDS)).performScrollTo().assertIsDisplayed()
        rule.onNodeWithTag(typeTag(InstrumentType.FUNDS)).performScrollTo().assertIsDisplayed()
    }
}
