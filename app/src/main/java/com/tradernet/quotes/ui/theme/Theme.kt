package com.tradernet.quotes.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

/**
 * The app's semantic color roles. The brand accents (positive/negative/neutral)
 * are theme-independent; only the surfaces and text invert between light and dark.
 */
@Immutable
data class QuoteColors(
    val background: Color,
    val surface: Color,
    val surfaceVariant: Color,
    val divider: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val positive: Color,
    val negative: Color,
    val neutral: Color,
)

private val DarkQuoteColors = QuoteColors(
    background = DarkBackground,
    surface = DarkSurface,
    surfaceVariant = DarkSurfaceVariant,
    divider = DarkDivider,
    textPrimary = DarkTextPrimary,
    textSecondary = DarkTextSecondary,
    positive = PositiveGreen,
    negative = NegativeRed,
    neutral = NeutralGray,
)

private val LightQuoteColors = QuoteColors(
    background = LightBackground,
    surface = LightSurface,
    surfaceVariant = LightSurfaceVariant,
    divider = LightDivider,
    textPrimary = LightTextPrimary,
    textSecondary = LightTextSecondary,
    positive = PositiveGreen,
    negative = NegativeRed,
    neutral = NeutralGray,
)

private val LocalQuoteColors = staticCompositionLocalOf { DarkQuoteColors }

object QuoteTheme {
    val colors: QuoteColors
        @Composable
        @ReadOnlyComposable
        get() = LocalQuoteColors.current
}

private val DarkColorScheme = darkColorScheme(
    primary = PositiveGreen,
    background = DarkBackground,
    surface = DarkSurface,
    onPrimary = DarkBackground,
    onBackground = DarkTextPrimary,
    onSurface = DarkTextPrimary,
)

private val LightColorScheme = lightColorScheme(
    primary = PositiveGreen,
    background = LightBackground,
    surface = LightSurface,
    onPrimary = LightBackground,
    onBackground = LightTextPrimary,
    onSurface = LightTextPrimary,
)

@Composable
fun TradernetTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val quoteColors = if (darkTheme) DarkQuoteColors else LightQuoteColors
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = quoteColors.background.toArgb()
            window.navigationBarColor = quoteColors.background.toArgb()
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = !darkTheme
                isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    CompositionLocalProvider(LocalQuoteColors provides quoteColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = AppTypography,
            content = content,
        )
    }
}
