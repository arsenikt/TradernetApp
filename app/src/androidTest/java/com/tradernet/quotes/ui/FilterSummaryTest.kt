package com.tradernet.quotes.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.tradernet.quotes.domain.DisplaySettings
import com.tradernet.quotes.domain.InstrumentType
import com.tradernet.quotes.domain.SortField
import com.tradernet.quotes.ui.theme.TradernetTheme
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import androidx.test.ext.junit.runners.AndroidJUnit4

@RunWith(AndroidJUnit4::class)
class FilterSummaryTest {

    @get:Rule
    val rule = createComposeRule()

    @Test
    fun isHiddenWhenAllSettingsAreDefault() {
        rule.setContent {
            TradernetTheme {
                FilterSummary(display = DisplaySettings.Default, onClick = {})
            }
        }
        rule.onNodeWithTag(TAG_FILTER_SUMMARY).assertDoesNotExist()
    }

    @Test
    fun isShownWhenAnyFilterIsActive() {
        rule.setContent {
            TradernetTheme {
                FilterSummary(
                    display = DisplaySettings(
                        instrumentType = InstrumentType.STOCKS,
                        sortField = SortField.PRICE,
                        sortDescending = true,
                    ),
                    onClick = {},
                )
            }
        }
        rule.onNodeWithTag(TAG_FILTER_SUMMARY).assertIsDisplayed()
    }

    @Test
    fun tappingInvokesCallback() {
        var clicked = false
        rule.setContent {
            TradernetTheme {
                FilterSummary(
                    display = DisplaySettings(groupByMarket = true),
                    onClick = { clicked = true },
                )
            }
        }
        rule.onNodeWithTag(TAG_FILTER_SUMMARY).performClick()
        assertTrue(clicked)
    }
}
