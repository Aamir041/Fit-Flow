package com.fitflow.app.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fitflow.app.data.repository.FitFlowRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class HistoryViewModel(
    private val repository: FitFlowRepository
) : ViewModel() {

    val uiState: StateFlow<HistoryUiState> = repository.getCompletedLogs().map { logs ->
        val grouped = logs.groupBy { it.log.date }
        val distinctDatesCount = grouped.keys.size
        val totalExercises = logs.size
        val totalVolume = logs.filter { !it.exercise.isSprint }.sumOf {
            it.log.actualSets * it.log.actualReps * it.log.actualWeight
        }

        HistoryUiState(
            completedLogs = logs,
            totalWorkoutsCount = distinctDatesCount,
            totalExercisesLogged = totalExercises,
            totalVolumeKg = (totalVolume * 10).toInt() / 10.0,
            groupedByDate = grouped,
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HistoryUiState()
    )
}
