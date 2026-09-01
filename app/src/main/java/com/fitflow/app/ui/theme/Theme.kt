package com.fitflow.app.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

val LocalDualToneAccent = staticCompositionLocalOf { AccentColor.DEFAULT }
val LocalThemeMode = staticCompositionLocalOf { ThemeMode.SYSTEM }

/**
 * Builds a Material3 ColorScheme adhering strictly to the dual-tone architecture:
 * - Base (Dark or Light) drives background, surfaces, outlines, and readable text typography.
 * - Accent drives primary highlights, interactive buttons, active indicators, and focus states.
 */
fun buildDualToneColorScheme(
    isDark: Boolean,
    accent: AccentColor = AccentColor.DEFAULT
): ColorScheme {
    return if (isDark) {
        darkColorScheme(
            primary = accent.color,
            onPrimary = accent.onAccentColor,
            primaryContainer = DarkSurfaceElevated,
            onPrimaryContainer = accent.color,
            secondary = accent.color,
            onSecondary = accent.onAccentColor,
            secondaryContainer = DarkSurfaceVariant,
            onSecondaryContainer = DarkTextPrimary,
            tertiary = accent.color,
            onTertiary = accent.onAccentColor,
            background = DarkBackground,
            onBackground = DarkTextPrimary,
            surface = DarkSurface,
            onSurface = DarkTextPrimary,
            surfaceVariant = DarkSurfaceVariant,
            onSurfaceVariant = DarkTextSecondary,
            surfaceContainerHighest = DarkSurfaceElevated,
            outline = DarkOutline,
            outlineVariant = DarkOutlineVariant,
            error = ErrorCrimson,
            onError = OnErrorWhite
        )
    } else {
        lightColorScheme(
            primary = accent.color,
            onPrimary = accent.onAccentColor,
            primaryContainer = LightSurfaceElevated,
            onPrimaryContainer = LightTextPrimary,
            secondary = accent.color,
            onSecondary = accent.onAccentColor,
            secondaryContainer = LightSurfaceVariant,
            onSecondaryContainer = LightTextPrimary,
            tertiary = accent.color,
            onTertiary = accent.onAccentColor,
            background = LightBackground,
            onBackground = LightTextPrimary,
            surface = LightSurface,
            onSurface = LightTextPrimary,
            surfaceVariant = LightSurfaceVariant,
            onSurfaceVariant = LightTextSecondary,
            surfaceContainerHighest = LightSurfaceElevated,
            outline = LightOutline,
            outlineVariant = LightOutlineVariant,
            error = ErrorCrimson,
            onError = OnErrorWhite
        )
    }
}

@Composable
fun FitFlowTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    accentColor: AccentColor = AccentColor.DEFAULT,
    content: @Composable () -> Unit
) {
    val isSystemDark = isSystemInDarkTheme()
    val isDark = when (themeMode) {
        ThemeMode.SYSTEM -> isSystemDark
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }

    val colorScheme = buildDualToneColorScheme(isDark = isDark, accent = accentColor)

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            window?.let {
                it.statusBarColor = colorScheme.background.toArgb()
                it.navigationBarColor = colorScheme.background.toArgb()
                WindowCompat.getInsetsController(it, view).apply {
                    isAppearanceLightStatusBars = !isDark
                    isAppearanceLightNavigationBars = !isDark
                }
            }
        }
    }

    CompositionLocalProvider(
        LocalDualToneAccent provides accentColor,
        LocalThemeMode provides themeMode
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            shapes = Shapes,
            content = content
        )
    }
}

/**
 * Preview convenience overload accepting an explicit boolean for dark theme.
 */
@Composable
fun FitFlowTheme(
    darkTheme: Boolean,
    accentColor: AccentColor = AccentColor.DEFAULT,
    content: @Composable () -> Unit
) {
    FitFlowTheme(
        themeMode = if (darkTheme) ThemeMode.DARK else ThemeMode.LIGHT,
        accentColor = accentColor,
        content = content
    )
}
