package com.fitflow.app.ui.settings

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fitflow.app.data.local.ThemePreferences
import com.fitflow.app.data.repository.FitFlowRepository
import com.fitflow.app.ui.theme.AccentColor
import com.fitflow.app.ui.theme.ThemeMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SettingsUiState(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val selectedAccent: AccentColor = AccentColor.DEFAULT,
    val availableAccents: List<AccentColor> = AccentColor.entries,
    val message: String? = null,
    val isSuccessMessage: Boolean = true
)

class SettingsViewModel(
    private val themePreferences: ThemePreferences,
    private val repository: FitFlowRepository
) : ViewModel() {

    private val _messageState = MutableStateFlow<Pair<String?, Boolean>>(null to true)

    val uiState: StateFlow<SettingsUiState> = combine(
        themePreferences.themeMode,
        themePreferences.accentColor,
        _messageState
    ) { mode, accent, msgPair ->
        SettingsUiState(
            themeMode = mode,
            selectedAccent = accent,
            availableAccents = AccentColor.entries,
            message = msgPair.first,
            isSuccessMessage = msgPair.second
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

    fun exportHistory(context: Context, uri: Uri) {
        viewModelScope.launch {
            try {
                val json = repository.exportHistoryToJson()
                context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                    outputStream.write(json.toByteArray())
                }
                _messageState.update { "History exported successfully" to true }
            } catch (e: Exception) {
                _messageState.update { "Export failed: ${e.message}" to false }
            }
        }
    }

    fun importHistory(context: Context, uri: Uri) {
        viewModelScope.launch {
            try {
                val json = context.contentResolver.openInputStream(uri)?.use { inputStream ->
                    inputStream.bufferedReader().readText()
                } ?: throw Exception("Could not read file")

                val result = repository.importHistoryFromJson(json)
                if (result.isSuccess) {
                    _messageState.update { "${result.getOrNull()} logs imported" to true }
                } else {
                    _messageState.update { "Import failed: ${result.exceptionOrNull()?.message}" to false }
                }
            } catch (e: Exception) {
                _messageState.update { "Import failed: ${e.message}" to false }
            }
        }
    }

    fun deleteAllWeightLogs() {
        viewModelScope.launch {
            try {
                repository.deleteAllWeightLogs()
                _messageState.update { "All weight logs deleted" to true }
            } catch (e: Exception) {
                _messageState.update { "Failed to delete weight logs: ${e.message}" to false }
            }
        }
    }

    fun clearAllHistory() {
        viewModelScope.launch {
            try {
                repository.clearAllHistory()
                _messageState.update { "All workout, food, and weight history cleared" to true }
            } catch (e: Exception) {
                _messageState.update { "Failed to clear history: ${e.message}" to false }
            }
        }
    }

    fun clearMessage() {
        _messageState.update { null to true }
    }
}
