package com.fitflow.app.data.local

import android.content.Context
import android.content.SharedPreferences
import com.fitflow.app.ui.theme.AppThemeMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ThemePreferences(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("fitflow_theme_prefs", Context.MODE_PRIVATE)

    private val _themeMode = MutableStateFlow(loadThemeMode())
    val themeMode: StateFlow<AppThemeMode> = _themeMode.asStateFlow()

    private fun loadThemeMode(): AppThemeMode {
        val savedName = prefs.getString(KEY_THEME_MODE, AppThemeMode.EMERALD.name)
        return try {
            AppThemeMode.valueOf(savedName ?: AppThemeMode.EMERALD.name)
        } catch (e: Exception) {
            AppThemeMode.EMERALD
        }
    }

    fun setThemeMode(mode: AppThemeMode) {
        prefs.edit().putString(KEY_THEME_MODE, mode.name).apply()
        _themeMode.value = mode
    }

    companion object {
        private const val KEY_THEME_MODE = "key_app_theme_mode"
    }
}