package com.fitflow.app.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import android.content.Context
import android.net.Uri
import com.fitflow.app.data.repository.FitFlowRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class HistoryViewModel(
    private val repository: FitFlowRepository
) : ViewModel() {

    private val _messageState = MutableStateFlow<Pair<String?, Boolean>>(null to true)

    val uiState: StateFlow<HistoryUiState> = combine(
        repository.getCompletedLogs(),
        repository.getAllFoodLogs(),
        _messageState
    ) { logs, foodLogs, messagePair ->
        val groupedWorkouts = logs.groupBy { it.log.date }
        val groupedFoods = foodLogs.groupBy { it.date }
        val distinctDatesCount = groupedWorkouts.keys.size
        val totalExercises = logs.size
        val totalVolume = logs.filter { !it.exercise.isSprint }.sumOf {
            it.log.actualSets * it.log.actualReps * it.log.actualWeight
        }

        HistoryUiState(
            completedLogs = logs,
            totalWorkoutsCount = distinctDatesCount,
            totalExercisesLogged = totalExercises,
            totalVolumeKg = (totalVolume * 10).toInt() / 10.0,
            groupedByDate = groupedWorkouts,
            foodLogsByDate = groupedFoods,
            isLoading = false,
            message = messagePair.first,
            isSuccess = messagePair.second
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HistoryUiState()
    )

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

    fun deleteHistory() {
        viewModelScope.launch {
            repository.clearAllHistory()
            _messageState.update { "Workout history cleared" to true }
        }
    }

    fun clearMessage() {
        _messageState.update { null to true }
    }
}
