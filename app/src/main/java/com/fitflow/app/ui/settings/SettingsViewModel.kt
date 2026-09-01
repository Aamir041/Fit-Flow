package com.fitflow.app.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fitflow.app.data.local.ThemePreferences
import com.fitflow.app.ui.theme.AccentColor
import com.fitflow.app.ui.theme.ThemeMode
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class SettingsUiState(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val selectedAccent: AccentColor = AccentColor.DEFAULT,
    val availableAccents: List<AccentColor> = AccentColor.entries
)

class SettingsViewModel(
    private val themePreferences: ThemePreferences
) : ViewModel() {

    val uiState: StateFlow<SettingsUiState> = combine(
        themePreferences.themeMode,
        themePreferences.accentColor
    ) { mode, accent ->
        SettingsUiState(
            themeMode = mode,
            selectedAccent = accent,
            availableAccents = AccentColor.entries
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SettingsUiState(
            themeMode = themePreferences.themeMode.value,
            selectedAccent = themePreferences.accentColor.value
        )
    )

    fun setThemeMode(mode: ThemeMode) {
        themePreferences.setThemeMode(mode)
    }

    fun setAccentColor(accent: AccentColor) {
        themePreferences.setAccentColor(accent)
    }
}
