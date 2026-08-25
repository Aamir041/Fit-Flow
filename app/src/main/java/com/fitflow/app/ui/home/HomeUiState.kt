package com.fitflow.app.ui.home

import com.fitflow.app.data.local.entity.WorkoutLogEntity
import com.fitflow.app.data.local.relation.TemplateWithExercises
import java.time.DayOfWeek
import java.time.LocalDate

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
    val actualDurationSeconds: Int = 30
)

data class HomeUiState(
    val currentDate: LocalDate = LocalDate.now(),
    val dayOfWeek: DayOfWeek = LocalDate.now().dayOfWeek,
    val assignedTemplate: TemplateWithExercises? = null,
    val exercises: List<ExerciseLogItem> = emptyList(),
    val completedCount: Int = 0,
    val totalCount: Int = 0,
    val progressPercent: Float = 0f,
    val isLoading: Boolean = true,
    val activeRestTimer: ExerciseLogItem? = null
)
