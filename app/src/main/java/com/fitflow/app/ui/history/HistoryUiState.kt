package com.fitflow.app.ui.history

import com.fitflow.app.data.local.relation.WorkoutLogWithExercise

data class HistoryUiState(
    val completedLogs: List<WorkoutLogWithExercise> = emptyList(),
    val totalWorkoutsCount: Int = 0,
    val totalExercisesLogged: Int = 0,
    val totalVolumeKg: Double = 0.0,
    val groupedByDate: Map<String, List<WorkoutLogWithExercise>> = emptyMap(),
    val isLoading: Boolean = true,
    val message: String? = null,
    val isSuccess: Boolean = true
)
