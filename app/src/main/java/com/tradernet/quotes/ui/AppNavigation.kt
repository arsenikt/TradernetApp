package com.tradernet.quotes.ui

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.tradernet.quotes.domain.AppSettings
import com.tradernet.quotes.domain.ThemeMode
import com.tradernet.quotes.ui.settings.SettingsScreen

private object Routes {
    const val QUOTES = "quotes"
    const val SETTINGS = "settings"
}

@Composable
fun AppNavHost(
    settings: AppSettings,
    onExtendedModeChange: (Boolean) -> Unit,
    onThemeModeChange: (ThemeMode) -> Unit,
    navController: NavHostController = rememberNavController(),
) {
    NavHost(navController = navController, startDestination = Routes.QUOTES) {
        composable(Routes.QUOTES) {
            QuotesScreen(onOpenSettings = { navController.navigate(Routes.SETTINGS) })
        }
        composable(Routes.SETTINGS) {
            SettingsScreen(
                settings = settings,
                onExtendedModeChange = onExtendedModeChange,
                onThemeModeChange = onThemeModeChange,
                onBack = { navController.popBackStack() },
            )
        }
    }
}
