package com.tradernet.quotes.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tradernet.quotes.R
import com.tradernet.quotes.domain.DisplaySettings
import com.tradernet.quotes.domain.InstrumentType
import com.tradernet.quotes.domain.SortField
import com.tradernet.quotes.ui.theme.QuoteTheme

internal const val TAG_FILTER_SUMMARY = "filter_summary"

@Composable
fun FilterSummary(
    display: DisplaySettings,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val hasType = display.instrumentType != InstrumentType.ALL
    val hasSort = display.sortField != SortField.DEFAULT
    if (!hasType && !hasSort && !display.groupByMarket) return

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .testTag(TAG_FILTER_SUMMARY),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (hasType) {
            FilterTag(label = stringResource(display.instrumentType.labelRes()))
        }
        if (hasSort) {
            FilterTag(
                label = stringResource(display.sortField.labelRes()),
                arrowDescending = display.sortDescending,
            )
        }
        if (display.groupByMarket) {
            FilterTag(label = stringResource(R.string.filter_grouped))
        }
    }
}

@Composable
private fun FilterTag(label: String, arrowDescending: Boolean? = null) {
    val colors = QuoteTheme.colors
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(percent = 50))
            .background(colors.surfaceVariant)
            .padding(horizontal = 10.dp, vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(text = label, color = colors.textSecondary, fontSize = 11.sp)
        if (arrowDescending != null) {
            Spacer(Modifier.width(3.dp))
            Icon(
                painter = painterResource(R.drawable.ic_arrow_upward),
                contentDescription = null,
                tint = colors.textSecondary,
                modifier = Modifier
                    .size(11.dp)
                    .rotate(if (arrowDescending) 180f else 0f),
            )
        }
    }
}
