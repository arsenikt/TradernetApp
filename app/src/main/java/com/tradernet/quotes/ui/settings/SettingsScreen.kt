package com.tradernet.quotes.ui.settings

import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tradernet.quotes.R
import com.tradernet.quotes.domain.AppSettings
import com.tradernet.quotes.domain.ThemeMode
import com.tradernet.quotes.ui.theme.QuoteTheme
import com.tradernet.quotes.ui.theme.TradernetTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    settings: AppSettings,
    onExtendedModeChange: (Boolean) -> Unit,
    onThemeModeChange: (ThemeMode) -> Unit,
    onBack: () -> Unit,
) {
    val colors = QuoteTheme.colors
    Scaffold(
        containerColor = colors.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.title_settings),
                        fontWeight = FontWeight.Bold,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            painter = painterResource(R.drawable.ic_arrow_back),
                            contentDescription = stringResource(R.string.action_back),
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colors.background,
                    titleContentColor = colors.textPrimary,
                    navigationIconContentColor = colors.textPrimary,
                ),
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState()),
        ) {
            SectionTitle(stringResource(R.string.settings_section_general))
            SwitchRow(
                title = stringResource(R.string.settings_extended_title),
                subtitle = stringResource(R.string.settings_extended_subtitle),
                checked = settings.extendedMode,
                onCheckedChange = onExtendedModeChange,
            )

            AnimatedVisibility(visible = settings.extendedMode) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    HorizontalDivider(
                        modifier = Modifier.padding(top = 8.dp),
                        thickness = 0.5.dp,
                        color = colors.divider,
                    )
                    SectionTitle(stringResource(R.string.settings_section_appearance))
                    Column(modifier = Modifier.selectableGroup()) {
                        ThemeMode.entries.forEach { mode ->
                            ThemeOptionRow(
                                label = stringResource(mode.labelRes()),
                                selected = settings.themeMode == mode,
                                onSelect = { onThemeModeChange(mode) },
                            )
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        color = QuoteTheme.colors.textSecondary,
        fontSize = 13.sp,
        fontWeight = FontWeight.Medium,
        modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 20.dp, bottom = 8.dp),
    )
}

@Composable
private fun SwitchRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    val colors = QuoteTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .toggleable(
                value = checked,
                onValueChange = onCheckedChange,
                role = Role.Switch,
            )
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, color = colors.textPrimary, fontSize = 16.sp)
            Spacer(Modifier.height(2.dp))
            Text(text = subtitle, color = colors.textSecondary, fontSize = 13.sp)
        }
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

@Composable
private fun ThemeOptionRow(
    label: String,
    selected: Boolean,
    onSelect: () -> Unit,
) {
    val colors = QuoteTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .selectable(
                selected = selected,
                onClick = onSelect,
                role = Role.RadioButton,
            )
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RadioButton(
            selected = selected,
            onClick = null,
            colors = RadioButtonDefaults.colors(
                selectedColor = colors.positive,
                unselectedColor = colors.textSecondary,
            ),
        )
        Spacer(Modifier.width(12.dp))
        Text(text = label, color = colors.textPrimary, fontSize = 16.sp)
    }
}

private fun ThemeMode.labelRes(): Int = when (this) {
    ThemeMode.SYSTEM -> R.string.settings_theme_system
    ThemeMode.LIGHT -> R.string.settings_theme_light
    ThemeMode.DARK -> R.string.settings_theme_dark
}

@Preview(name = "Settings — Light", showBackground = true)
@Preview(
    name = "Settings — Dark",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun SettingsScreenPreview() {
    TradernetTheme {
        SettingsScreen(
            settings = AppSettings(extendedMode = true, themeMode = ThemeMode.DARK),
            onExtendedModeChange = {},
            onThemeModeChange = {},
            onBack = {},
        )
    }
}
