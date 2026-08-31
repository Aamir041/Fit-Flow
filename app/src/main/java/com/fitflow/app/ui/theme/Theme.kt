package com.fitflow.app.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

enum class AppThemeMode(val displayName: String, val description: String) {
    EMERALD("Electric Emerald", "High-energy neon green & cyan athletic palette"),
    SPIDERMAN("Spider-Man Edition", "Heroic comic red, vibrant blue & charcoal palette")
}

private val EmeraldDarkColorScheme = darkColorScheme(
    primary = EmeraldPrimary,
    onPrimary = BackgroundDark,
    primaryContainer = SurfaceElevatedDark,
    onPrimaryContainer = EmeraldLight,
    secondary = CyanAccent,
    onSecondary = BackgroundDark,
    secondaryContainer = SurfaceVariantDark,
    onSecondaryContainer = CyanAccent,
    tertiary = AmberAccent,
    onTertiary = BackgroundDark,
    background = BackgroundDark,
    onBackground = TextPrimaryDark,
    surface = SurfaceDark,
    onSurface = TextPrimaryDark,
    surfaceVariant = SurfaceVariantDark,
    onSurfaceVariant = TextSecondaryDark,
    outline = OutlineDark,
    outlineVariant = OutlineVariantDark,
    error = CrimsonAlert,
    onError = TextPrimaryDark
)

private val SpidermanDarkColorScheme = darkColorScheme(
    primary = SpideyRedPrimary,
    onPrimary = SpideyWhiteHighlight,
    primaryContainer = SpideyCrimsonDark,
    onPrimaryContainer = SpideyWhiteHighlight,
    secondary = SpideyBlueSecondary,
    onSecondary = SpideyWhiteHighlight,
    secondaryContainer = SpideySurfaceVariantDark,
    onSecondaryContainer = SpideyBlueLight,
    tertiary = SpideyBlueSecondary,
    onTertiary = SpideyWhiteHighlight,
    background = SpideyBackgroundDark,
    onBackground = SpideyWhiteHighlight,
    surface = SpideySurfaceDark,
    onSurface = SpideyWhiteHighlight,
    surfaceVariant = SpideySurfaceVariantDark,
    onSurfaceVariant = TextSecondaryDark,
    outline = SpideyOutlineDark,
    outlineVariant = SpideyOutlineVariantDark,
    error = CrimsonAlert,
    onError = SpideyWhiteHighlight
)

@Composable
fun FitFlowTheme(
    themeMode: AppThemeMode = AppThemeMode.EMERALD,
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme: ColorScheme = when (themeMode) {
        AppThemeMode.EMERALD -> EmeraldDarkColorScheme
        AppThemeMode.SPIDERMAN -> SpidermanDarkColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            window?.let {
                it.statusBarColor = colorScheme.background.toArgb()
                it.navigationBarColor = colorScheme.background.toArgb()
                WindowCompat.getInsetsController(it, view).apply {
                    isAppearanceLightStatusBars = false
                    isAppearanceLightNavigationBars = false
                }
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}
