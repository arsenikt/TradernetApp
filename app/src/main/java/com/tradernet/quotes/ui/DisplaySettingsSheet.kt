package com.tradernet.quotes.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tradernet.quotes.R
import com.tradernet.quotes.domain.DisplaySettings
import com.tradernet.quotes.domain.InstrumentType
import com.tradernet.quotes.domain.MarketTab
import com.tradernet.quotes.domain.SortField
import com.tradernet.quotes.ui.theme.QuoteTheme
import kotlinx.coroutines.launch

private val MARKET_OPTIONS = listOf(MarketTab.ALL, MarketTab.RUSSIA, MarketTab.US, MarketTab.EUROPE)
private val TYPE_OPTIONS = listOf(
    InstrumentType.ALL,
    InstrumentType.STOCKS,
    InstrumentType.BONDS,
    InstrumentType.FUNDS,
)
private val SORT_OPTIONS = listOf(
    SortField.NAME,
    SortField.PRICE,
    SortField.CHANGE,
    SortField.CHANGE_PERCENT,
)

internal const val TAG_DISPLAY_APPLY = "display_apply"
internal const val TAG_DISPLAY_RESET = "display_reset"
internal const val TAG_DISPLAY_GROUP = "display_group"
internal fun marketTag(market: MarketTab) = "market_${market.name}"
internal fun typeTag(type: InstrumentType) = "type_${type.name}"
internal fun sortTag(field: SortField) = "sort_${field.name}"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DisplaySettingsSheet(
    initial: DisplaySettings,
    onApply: (DisplaySettings) -> Unit,
    onDismiss: () -> Unit,
) {
    val colors = QuoteTheme.colors
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = colors.surface,
    ) {
        DisplaySettingsContent(
            initial = initial,
            onApply = { settings ->
                onApply(settings)
                scope.launch { sheetState.hide() }.invokeOnCompletion {
                    if (!sheetState.isVisible) onDismiss()
                }
            },
            modifier = Modifier.navigationBarsPadding(),
        )
    }
}

@Composable
internal fun DisplaySettingsContent(
    initial: DisplaySettings,
    onApply: (DisplaySettings) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = QuoteTheme.colors
    var draft by remember { mutableStateOf(initial) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .padding(bottom = 12.dp),
    ) {
        Text(
            text = stringResource(R.string.display_settings_title),
            color = colors.textPrimary,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
        )

        SectionLabel(stringResource(R.string.display_section_markets))
        ChipRow {
            MARKET_OPTIONS.forEach { market ->
                ChoiceChip(
                    label = stringResource(market.labelRes()),
                    selected = market in draft.markets,
                    onClick = { draft = draft.copy(markets = MarketTab.toggleSelection(draft.markets, market)) },
                    modifier = Modifier.testTag(marketTag(market)),
                    leadingFlag = market.flagRes(),
                )
            }
        }

        SectionLabel(stringResource(R.string.display_section_types))
        ChipRow {
            TYPE_OPTIONS.forEach { type ->
                ChoiceChip(
                    label = stringResource(type.labelRes()),
                    selected = draft.instrumentType == type,
                    onClick = { draft = draft.copy(instrumentType = type) },
                    modifier = Modifier.testTag(typeTag(type)),
                )
            }
        }

        SectionLabel(stringResource(R.string.display_section_sort))
        ChipRow {
            SORT_OPTIONS.forEach { field ->
                val isSelected = draft.sortField == field
                ChoiceChip(
                    label = stringResource(field.labelRes()),
                    selected = isSelected,
                    onClick = { draft = draft.selectSort(field) },
                    modifier = Modifier.testTag(sortTag(field)),
                    arrowDescending = if (isSelected) draft.sortDescending else null,
                )
            }
        }

        Spacer(Modifier.height(8.dp))
        HorizontalDivider(thickness = 0.5.dp, color = colors.divider)
        GroupToggleRow(
            checked = draft.groupByMarket,
            onChange = { draft = draft.copy(groupByMarket = it) },
            modifier = Modifier.testTag(TAG_DISPLAY_GROUP),
        )
        HorizontalDivider(thickness = 0.5.dp, color = colors.divider)

        Spacer(Modifier.height(12.dp))
        Button(
            onClick = { onApply(draft) },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag(TAG_DISPLAY_APPLY),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = colors.positive,
                contentColor = Color.White,
            ),
        ) {
            Text(
                text = stringResource(R.string.action_apply),
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
            )
        }

        TextButton(
            onClick = { draft = DisplaySettings.Default },
            modifier = Modifier
                .fillMaxWidth()
                .testTag(TAG_DISPLAY_RESET),
        ) {
            Text(
                text = stringResource(R.string.action_reset_all),
                color = colors.positive,
                fontSize = 14.sp,
            )
        }
    }
}

private fun DisplaySettings.selectSort(field: SortField): DisplaySettings =
    if (sortField == field) {
        copy(sortDescending = !sortDescending)
    } else {
        copy(sortField = field, sortDescending = field != SortField.NAME)
    }

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        color = QuoteTheme.colors.textSecondary,
        fontSize = 12.sp,
        fontWeight = FontWeight.Medium,
        modifier = Modifier.padding(top = 14.dp, bottom = 8.dp),
    )
}

@Composable
private fun ChipRow(content: @Composable () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        content()
    }
}

@Composable
private fun ChoiceChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    leadingFlag: Int? = null,
    arrowDescending: Boolean? = null,
) {
    val colors = QuoteTheme.colors
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(percent = 50))
            .background(if (selected) colors.positive else colors.surfaceVariant)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (leadingFlag != null) {
            Image(
                painter = painterResource(leadingFlag),
                contentDescription = null,
                modifier = Modifier
                    .size(16.dp)
                    .clip(RoundedCornerShape(percent = 50)),
            )
            Spacer(Modifier.width(6.dp))
        }
        Text(
            text = label,
            color = if (selected) Color.White else colors.textSecondary,
            fontSize = 13.sp,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
        )
        if (arrowDescending != null) {
            Spacer(Modifier.width(4.dp))
            Icon(
                painter = painterResource(R.drawable.ic_arrow_upward),
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier
                    .size(14.dp)
                    .rotate(if (arrowDescending) 180f else 0f),
            )
        }
    }
}

@Composable
private fun GroupToggleRow(
    checked: Boolean,
    onChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = QuoteTheme.colors
    Row(
        modifier = modifier
            .fillMaxWidth()
            .toggleable(value = checked, onValueChange = onChange, role = Role.Switch)
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = stringResource(R.string.display_group_by_market),
            color = colors.textPrimary,
            fontSize = 14.sp,
            modifier = Modifier.weight(1f),
        )
        Spacer(Modifier.width(16.dp))
        Switch(
            checked = checked,
            onCheckedChange = null,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = colors.positive,
                uncheckedThumbColor = colors.textSecondary,
                uncheckedTrackColor = colors.surfaceVariant,
                uncheckedBorderColor = colors.divider,
            ),
        )
    }
}
