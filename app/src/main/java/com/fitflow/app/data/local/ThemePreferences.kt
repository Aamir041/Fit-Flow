package com.fitflow.app.data.local

import android.content.Context
import android.content.SharedPreferences
import com.fitflow.app.ui.theme.AccentColor
import com.fitflow.app.ui.theme.ThemeMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Handles persistent storage and reactive emission of user theme mode and accent color settings.
 */
class ThemePreferences(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _themeMode = MutableStateFlow(loadThemeMode())
    val themeMode: StateFlow<ThemeMode> = _themeMode.asStateFlow()

    private val _accentColor = MutableStateFlow(loadAccentColor())
    val accentColor: StateFlow<AccentColor> = _accentColor.asStateFlow()

    private fun loadThemeMode(): ThemeMode {
        val raw = prefs.getString(KEY_THEME_MODE, ThemeMode.SYSTEM.name)
        return ThemeMode.fromName(raw)
    }

    private fun loadAccentColor(): AccentColor {
        val raw = prefs.getString(KEY_ACCENT_COLOR, AccentColor.DEFAULT.id)
        return AccentColor.fromId(raw)
    }

    fun setThemeMode(mode: ThemeMode) {
        prefs.edit().putString(KEY_THEME_MODE, mode.name).apply()
        _themeMode.value = mode
    }

    fun setAccentColor(accent: AccentColor) {
        prefs.edit().putString(KEY_ACCENT_COLOR, accent.id).apply()
        _accentColor.value = accent
    }

    companion object {
        private const val PREFS_NAME = "fitflow_theme_preferences"
        private const val KEY_THEME_MODE = "theme_mode"
        private const val KEY_ACCENT_COLOR = "accent_color"
    }
}
