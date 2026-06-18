package com.tradernet.quotes

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tradernet.quotes.domain.ThemeMode
import com.tradernet.quotes.ui.AppNavHost
import com.tradernet.quotes.ui.settings.SettingsViewModel
import com.tradernet.quotes.ui.theme.TradernetTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val settingsViewModel: SettingsViewModel = viewModel(factory = SettingsViewModel.Factory)
            val settings by settingsViewModel.settings.collectAsStateWithLifecycle()

            val darkTheme = when (settings.effectiveThemeMode) {
                ThemeMode.SYSTEM -> isSystemInDarkTheme()
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
            }

            TradernetTheme(darkTheme = darkTheme) {
                AppNavHost(
                    settings = settings,
                    onExtendedModeChange = settingsViewModel::setExtendedMode,
                    onThemeModeChange = settingsViewModel::setThemeMode,
                )
            }
        }
    }
}
