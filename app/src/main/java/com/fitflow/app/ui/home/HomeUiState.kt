package com.fitflow.app.ui.home

import com.fitflow.app.data.local.entity.WorkoutLogEntity
import com.fitflow.app.data.local.relation.TemplateWithExercises
import java.time.DayOfWeek
import java.time.LocalDate

data class WorkoutSetUiModel(
    val setNumber: Int,
    val reps: Int,
    val weight: Double,
    val isCompleted: Boolean = false
)

data class ExerciseLogItem(
    val templateExerciseId: Long,
    val exerciseId: Long,
    val name: String,
    val category: String,
    val targetSets: Int,
    val targetReps: Int,
    val restTimeSeconds: Int,
    val actualSets: Int,
    val actualReps: Int,
    val actualWeight: Double,
    val isCompleted: Boolean,
    val isSprint: Boolean = false,
    val targetDurationSeconds: Int = 30,
    val actualDurationSeconds: Int = 30,
    val sets: List<WorkoutSetUiModel> = emptyList()
) {
    val completedSetsCount: Int
        get() = sets.count { it.isCompleted }

    val totalSetsCount: Int
        get() = if (sets.isNotEmpty()) sets.size else targetSets
}

data class HomeUiState(
    val currentDate: LocalDate = LocalDate.now(),
    val dayOfWeek: DayOfWeek = LocalDate.now().dayOfWeek,
    val assignedTemplate: TemplateWithExercises? = null,
    val exercises: List<ExerciseLogItem> = emptyList(),
    val completedCount: Int = 0,
    val totalCount: Int = 0,
    val progressPercent: Float = 0f,
    val isLoading: Boolean = true,
    val activeRestTimer: ExerciseLogItem? = null,
    val selectedExerciseForLogging: ExerciseLogItem? = null
)

