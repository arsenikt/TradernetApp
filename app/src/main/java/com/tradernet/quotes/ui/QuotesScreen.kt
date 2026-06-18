package com.tradernet.quotes.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tradernet.quotes.R
import com.tradernet.quotes.domain.MarketTab
import com.tradernet.quotes.ui.theme.QuoteTheme
import kotlinx.coroutines.flow.distinctUntilChanged

@Composable
fun QuotesScreen(
    onOpenSettings: () -> Unit,
    viewModel: QuotesViewModel = viewModel(factory = QuotesViewModel.Factory),
) {
    var showDisplaySheet by remember { mutableStateOf(false) }

    Surface(modifier = Modifier.fillMaxSize(), color = QuoteTheme.colors.background) {
        Column(modifier = Modifier.fillMaxSize()) {
            Header(
                viewModel = viewModel,
                onOpenSettings = onOpenSettings,
                onOpenDisplaySettings = { showDisplaySheet = true },
            )
            HorizontalDivider(color = QuoteTheme.colors.divider, thickness = 1.dp)
            if (viewModel.tabsVisible) {
                MarketTabs(
                    tabs = viewModel.tabs,
                    selected = viewModel.selectedMarkets,
                    onToggle = viewModel::onToggleMarket,
                )
                FilterSummary(
                    display = viewModel.display,
                    onClick = { showDisplaySheet = true },
                )
                if (viewModel.loading) {
                    LinearProgressIndicator(
                        modifier = Modifier.fillMaxWidth(),
                        color = QuoteTheme.colors.positive,
                        trackColor = QuoteTheme.colors.surfaceVariant,
                    )
                } else {
                    HorizontalDivider(color = QuoteTheme.colors.divider, thickness = 1.dp)
                }
            }
            QuotesList(viewModel)
        }
    }

    if (showDisplaySheet) {
        DisplaySettingsSheet(
            initial = viewModel.display,
            onApply = viewModel::applyDisplaySettings,
            onDismiss = { showDisplaySheet = false },
        )
    }
}

@Composable
private fun MarketTabs(
    tabs: List<MarketTab>,
    selected: Set<MarketTab>,
    onToggle: (MarketTab) -> Unit,
) {
    val colors = QuoteTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        tabs.forEach { tab ->
            val isSelected = tab in selected
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(percent = 50))
                    .background(if (isSelected) colors.positive else colors.surfaceVariant)
                    .clickable { onToggle(tab) }
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                MarketFlag(tab)
                Text(
                    text = stringResource(tab.labelRes()),
                    color = if (isSelected) Color.White else colors.textSecondary,
                    fontSize = 14.sp,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                )
            }
        }
    }
}

@Composable
private fun MarketFlag(market: MarketTab, size: Int = 16) {
    val flag = market.flagRes() ?: return
    Image(
        painter = painterResource(flag),
        contentDescription = null,
        modifier = Modifier
            .size(size.dp)
            .clip(CircleShape),
    )
    Spacer(Modifier.width(6.dp))
}

@Composable
private fun Header(
    viewModel: QuotesViewModel,
    onOpenSettings: () -> Unit,
    onOpenDisplaySettings: () -> Unit,
) {
    val colors = QuoteTheme.colors

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 4.dp, top = 14.dp, bottom = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stringResource(R.string.app_name),
                color = colors.textPrimary,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = stringResource(R.string.title_quotes),
                color = colors.textSecondary,
                fontSize = 13.sp,
            )
        }
        if (viewModel.tabsVisible) {
            IconButton(onClick = onOpenDisplaySettings) {
                Icon(
                    painter = painterResource(R.drawable.ic_tune),
                    contentDescription = stringResource(R.string.action_open_display_settings),
                    tint = colors.textSecondary,
                )
            }
        }
        IconButton(onClick = onOpenSettings) {
            Icon(
                painter = painterResource(R.drawable.ic_settings),
                contentDescription = stringResource(R.string.action_open_settings),
                tint = colors.textSecondary,
            )
        }
    }
}

private val QuoteRowModifier = Modifier
    .fillMaxWidth()
    .height(68.dp)

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun QuotesList(viewModel: QuotesViewModel) {
    val listState = rememberLazyListState()

    LaunchedEffect(listState, viewModel) {
        snapshotFlow { listState.isScrollInProgress }
            .distinctUntilChanged()
            .collect(viewModel::setScrolling)
    }

    LaunchedEffect(
        viewModel.selectedMarkets,
        viewModel.display.instrumentType,
        viewModel.display.sortField,
        viewModel.display.sortDescending,
    ) {
        listState.scrollToItem(0)
    }

    val groups = viewModel.displayGroups
    if (groups.isEmpty() && !viewModel.loading) {
        EmptyState()
        return
    }

    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
    ) {
        groups.forEach { group ->
            val market = group.market
            if (market != null) {
                stickyHeader(key = "h:${market.name}", contentType = "header") {
                    MarketSectionHeader(market)
                }
            }
            items(
                items = group.tickers,
                key = { ticker -> "r:$ticker" },
                contentType = { "quote" },
            ) { ticker ->
                val quote by viewModel.quoteState(ticker)
                val logo by viewModel.logoState(ticker)
                Column(modifier = Modifier.animateItem()) {
                    QuoteRow(quote = quote, logo = logo, modifier = QuoteRowModifier)
                    HorizontalDivider(
                        modifier = Modifier.padding(start = 16.dp),
                        thickness = 0.5.dp,
                        color = QuoteTheme.colors.divider,
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = stringResource(R.string.quotes_empty),
            color = QuoteTheme.colors.textSecondary,
            fontSize = 15.sp,
        )
    }
}

@Composable
private fun MarketSectionHeader(market: MarketTab) {
    val colors = QuoteTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.surfaceVariant)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        MarketFlag(market)
        Text(
            text = stringResource(market.labelRes()),
            color = colors.textSecondary,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
        )
    }
}
