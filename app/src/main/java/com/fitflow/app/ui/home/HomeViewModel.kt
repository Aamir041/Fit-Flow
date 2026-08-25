package com.fitflow.app.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fitflow.app.data.local.entity.WorkoutLogEntity
import com.fitflow.app.data.repository.FitFlowRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModel(
    private val repository: FitFlowRepository
) : ViewModel() {

    private val _selectedDate = MutableStateFlow(LocalDate.now())
    val selectedDate: StateFlow<LocalDate> = _selectedDate.asStateFlow()

    private val _activeRestTimer = MutableStateFlow<ExerciseLogItem?>(null)
    val activeRestTimer: StateFlow<ExerciseLogItem?> = _activeRestTimer.asStateFlow()

    // Map of local in-memory adjustments before or while logging (exerciseId -> LogDraft)
    private val _inMemoryAdjustments = MutableStateFlow<Map<Long, WorkoutLogEntity>>(emptyMap())

    val uiState: StateFlow<HomeUiState> = _selectedDate.flatMapLatest { date ->
        val dateString = date.format(DateTimeFormatter.ISO_LOCAL_DATE)
        val dayOfWeekValue = date.dayOfWeek.value

        combine(
            repository.getAssignmentForDay(dayOfWeekValue),
            repository.getLogsForDate(dateString),
            _activeRestTimer,
            _inMemoryAdjustments
        ) { dayWithTemplate, logsWithExercise, activeTimer, adjustments ->
            val templateId = dayWithTemplate?.dayAssignment?.templateId
            val template = if (templateId != null) {
                repository.getTemplateWithExercisesOnce(templateId)
            } else {
                null
            }

            val logsMap = logsWithExercise.associateBy { it.log.exerciseId }

            val exerciseItems = template?.exercises?.sortedBy { it.templateExercise.orderIndex }?.map { item ->
                val exId = item.exercise.id
                val existingLog = logsMap[exId]?.log ?: adjustments[exId]

                val actualSets = existingLog?.actualSets ?: item.templateExercise.targetSets
                val actualReps = existingLog?.actualReps ?: item.templateExercise.targetReps
                val actualWeight = existingLog?.actualWeight ?: 0.0
                val actualDuration = if (existingLog != null && existingLog.actualDurationSeconds > 0) {
                    existingLog.actualDurationSeconds
                } else if (item.templateExercise.targetDurationSeconds > 0) {
                    item.templateExercise.targetDurationSeconds
                } else {
                    item.exercise.defaultDurationSeconds
                }
                val isCompleted = existingLog?.isCompleted ?: false

                ExerciseLogItem(
                    templateExerciseId = item.templateExercise.id,
                    exerciseId = exId,
                    name = item.exercise.name,
                    category = item.exercise.category,
                    targetSets = item.templateExercise.targetSets,
                    targetReps = item.templateExercise.targetReps,
                    restTimeSeconds = item.templateExercise.restTimeSeconds,
                    actualSets = actualSets,
                    actualReps = actualReps,
                    actualWeight = actualWeight,
                    isCompleted = isCompleted,
                    isSprint = item.exercise.isSprint,
                    targetDurationSeconds = item.templateExercise.targetDurationSeconds,
                    actualDurationSeconds = actualDuration
                )
            } ?: emptyList()

            val completedCount = exerciseItems.count { it.isCompleted }
            val totalCount = exerciseItems.size
            val progressPercent = if (totalCount > 0) completedCount.toFloat() / totalCount.toFloat() else 0f

            HomeUiState(
                currentDate = date,
                dayOfWeek = date.dayOfWeek,
                assignedTemplate = template,
                exercises = exerciseItems,
                completedCount = completedCount,
                totalCount = totalCount,
                progressPercent = progressPercent,
                isLoading = false,
                activeRestTimer = activeTimer
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HomeUiState()
    )

    fun updateExerciseValues(exerciseId: Long, sets: Int, reps: Int, weight: Double) {
        val dateString = _selectedDate.value.format(DateTimeFormatter.ISO_LOCAL_DATE)
        viewModelScope.launch {
            val templateId = uiState.value.assignedTemplate?.template?.id
            val existing = repository.getLogForExercise(dateString, exerciseId)
            val updated = (existing ?: WorkoutLogEntity(
                date = dateString,
                templateId = templateId,
                exerciseId = exerciseId,
                actualSets = sets,
                actualReps = reps,
                actualWeight = weight,
                isCompleted = false
            )).copy(
                actualSets = sets,
                actualReps = reps,
                actualWeight = weight
            )
            repository.saveWorkoutLog(updated)

            _inMemoryAdjustments.update { current ->
                current + (exerciseId to updated)
            }
        }
    }

    fun updateSprintDuration(exerciseId: Long, durationSeconds: Int) {
        val dateString = _selectedDate.value.format(DateTimeFormatter.ISO_LOCAL_DATE)
        viewModelScope.launch {
            val templateId = uiState.value.assignedTemplate?.template?.id
            val existing = repository.getLogForExercise(dateString, exerciseId)
            val updated = (existing ?: WorkoutLogEntity(
                date = dateString,
                templateId = templateId,
                exerciseId = exerciseId,
                actualDurationSeconds = durationSeconds,
                isCompleted = false
            )).copy(
                actualDurationSeconds = durationSeconds
            )
            repository.saveWorkoutLog(updated)

            _inMemoryAdjustments.update { current ->
                current + (exerciseId to updated)
            }
        }
    }

    fun toggleExerciseCompletion(item: ExerciseLogItem) {
        val dateString = _selectedDate.value.format(DateTimeFormatter.ISO_LOCAL_DATE)
        viewModelScope.launch {
            val templateId = uiState.value.assignedTemplate?.template?.id
            val wasCompleted = item.isCompleted
            val willBeCompleted = !wasCompleted

            repository.toggleExerciseCompletion(
                date = dateString,
                templateId = templateId,
                exerciseId = item.exerciseId,
                sets = item.actualSets,
                reps = item.actualReps,
                weight = item.actualWeight,
                durationSeconds = item.actualDurationSeconds
            )

            // If user just marked completed, trigger rest timer suggestion if rest time > 0
            if (willBeCompleted && item.restTimeSeconds > 0) {
                _activeRestTimer.value = item
            }
        }
    }

    fun openRestTimer(item: ExerciseLogItem) {
        _activeRestTimer.value = item
    }

    fun closeRestTimer() {
        _activeRestTimer.value = null
    }

    fun changeDate(date: LocalDate) {
        _selectedDate.value = date
    }
}
