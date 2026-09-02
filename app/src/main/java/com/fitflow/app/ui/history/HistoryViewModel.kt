package com.fitflow.app.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import android.content.Context
import android.net.Uri
import com.fitflow.app.data.local.model.calculateWeightSummaryStats
import com.fitflow.app.data.local.model.calculateWeightTimeline
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
        repository.getAllWeightLogs(),
        _messageState
    ) { logs, weightLogs, messagePair ->
        val groupedWorkouts = logs.groupBy { it.log.date }
        val distinctDatesCount = groupedWorkouts.keys.size
        val totalExercises = logs.size
        val totalVolume = logs.filter { !it.exercise.isSprint }.sumOf { logWithEx ->
            val setsJson = logWithEx.log.setsDataJson
            if (setsJson.isNotBlank()) {
                val sets = com.fitflow.app.data.local.model.WorkoutSetRecord.parseSetsFromJson(setsJson)
                val completedSets = sets.filter { it.isCompleted }
                if (completedSets.isNotEmpty()) {
                    completedSets.sumOf { it.reps * it.weight }
                } else {
                    // Fallback to all sets or aggregate
                    sets.sumOf { it.reps * it.weight }
                }
            } else {
                logWithEx.log.actualSets * logWithEx.log.actualReps * logWithEx.log.actualWeight
            }
        }

        val timeline = calculateWeightTimeline(weightLogs)
        val stats = calculateWeightSummaryStats(weightLogs, timeline)

        HistoryUiState(
            completedLogs = logs,
            totalWorkoutsCount = distinctDatesCount,
            totalExercisesLogged = totalExercises,
            totalVolumeKg = (totalVolume * 10).toInt() / 10.0,
            groupedByDate = groupedWorkouts,
            weightLogs = weightLogs,
            weightTimeline = timeline,
            weightStats = stats,
            isLoading = false,
            message = messagePair.first,
            isSuccess = messagePair.second
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HistoryUiState()
    )

    fun saveWeight(date: String, weightKg: Double) {
        viewModelScope.launch {
            try {
                repository.saveWeightLog(date, weightKg)
                _messageState.update { "Weight logged successfully" to true }
            } catch (e: Exception) {
                _messageState.update { "Failed to log weight: ${e.message}" to false }
            }
        }
    }

    fun deleteWeight(date: String) {
        viewModelScope.launch {
            try {
                repository.deleteWeightLogByDate(date)
                _messageState.update { "Weight entry removed" to true }
            } catch (e: Exception) {
                _messageState.update { "Failed to remove weight: ${e.message}" to false }
            }
        }
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

    fun deleteHistory() {
        viewModelScope.launch {
            repository.clearAllHistory()
            _messageState.update { "All workout and weight history cleared" to true }
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

    fun clearMessage() {
        _messageState.update { null to true }
    }
}
