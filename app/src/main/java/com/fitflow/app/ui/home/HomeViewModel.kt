package com.fitflow.app.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fitflow.app.data.local.entity.WorkoutLogEntity
import com.fitflow.app.data.local.model.WorkoutSetRecord
import com.fitflow.app.data.repository.FitFlowRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
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

    private val _selectedExerciseForLoggingId = MutableStateFlow<Long?>(null)

    // Map of local in-memory adjustments before or while logging (exerciseId -> LogDraft)
    private val _inMemoryAdjustments = MutableStateFlow<Map<Long, WorkoutLogEntity>>(emptyMap())

    val uiState: StateFlow<HomeUiState> = _selectedDate.flatMapLatest { date ->
        val dateString = date.format(DateTimeFormatter.ISO_LOCAL_DATE)
        val dayOfWeekValue = date.dayOfWeek.value

        val workoutFlow = combine(
            repository.getAssignmentForDay(dayOfWeekValue),
            repository.getLogsForDate(dateString),
            _activeRestTimer,
            _inMemoryAdjustments,
            _selectedExerciseForLoggingId
        ) { dayWithTemplate, logsWithExercise, activeTimer, adjustments, selectedExerciseId ->
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

                val actualSetsCount = existingLog?.actualSets ?: item.templateExercise.targetSets
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

                // Parse or construct sets
                val parsedSets = if (existingLog != null && existingLog.setsDataJson.isNotBlank()) {
                    WorkoutSetRecord.parseSetsFromJson(existingLog.setsDataJson).map { record ->
                        WorkoutSetUiModel(
                            setNumber = record.setNumber,
                            reps = record.reps,
                            weight = record.weight,
                            isCompleted = record.isCompleted
                        )
                    }
                } else {
                    // Generate default sets matching targetSets (or target rounds)
                    val setsCount = if (actualSetsCount > 0) actualSetsCount else item.templateExercise.targetSets
                    val defaultRepsVal = if (item.exercise.isSprint) {
                        if (actualDuration > 0) actualDuration else item.templateExercise.targetDurationSeconds
                    } else {
                        actualReps
                    }
                    (1..setsCount).map { setIndex ->
                        WorkoutSetUiModel(
                            setNumber = setIndex,
                            reps = defaultRepsVal,
                            weight = if (item.exercise.isSprint) 0.0 else actualWeight,
                            isCompleted = isCompleted
                        )
                    }
                }

                ExerciseLogItem(
                    templateExerciseId = item.templateExercise.id,
                    exerciseId = exId,
                    name = item.exercise.name,
                    category = item.exercise.category,
                    targetSets = item.templateExercise.targetSets,
                    targetReps = item.templateExercise.targetReps,
                    restTimeSeconds = item.templateExercise.restTimeSeconds,
                    actualSets = actualSetsCount,
                    actualReps = actualReps,
                    actualWeight = actualWeight,
                    isCompleted = isCompleted,
                    isSprint = item.exercise.isSprint,
                    targetDurationSeconds = item.templateExercise.targetDurationSeconds,
                    actualDurationSeconds = actualDuration,
                    sets = parsedSets
                )
            } ?: emptyList()

            val completedCount = exerciseItems.count { it.isCompleted }
            val totalCount = exerciseItems.size
            val progressPercent = if (totalCount > 0) completedCount.toFloat() / totalCount.toFloat() else 0f
            val selectedExerciseItem = exerciseItems.find { it.exerciseId == selectedExerciseId }

            WorkoutDayState(
                template = template,
                exerciseItems = exerciseItems,
                completedCount = completedCount,
                totalCount = totalCount,
                progressPercent = progressPercent,
                activeTimer = activeTimer,
                selectedExerciseItem = selectedExerciseItem
            )
        }

        val weightFlow = combine(
            repository.getWeightLogForDate(dateString),
            repository.getLatestWeightLog()
        ) { weightLog, latestWeightLog ->
            weightLog?.weightKg to latestWeightLog?.weightKg
        }

        combine(workoutFlow, weightFlow) { workoutState, weightPair ->
            HomeUiState(
                currentDate = date,
                dayOfWeek = date.dayOfWeek,
                assignedTemplate = workoutState.template,
                exercises = workoutState.exerciseItems,
                completedCount = workoutState.completedCount,
                totalCount = workoutState.totalCount,
                progressPercent = workoutState.progressPercent,
                isLoading = false,
                activeRestTimer = workoutState.activeTimer,
                selectedExerciseForLogging = workoutState.selectedExerciseItem,
                todayWeightLog = weightPair.first,
                lastRecordedWeight = weightPair.second
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HomeUiState()
    )

    fun saveTodayWeight(weightKg: Double) {
        viewModelScope.launch {
            val dateString = _selectedDate.value.format(DateTimeFormatter.ISO_LOCAL_DATE)
            repository.saveWeightLog(dateString, weightKg)
        }
    }

    fun deleteTodayWeight() {
        viewModelScope.launch {
            val dateString = _selectedDate.value.format(DateTimeFormatter.ISO_LOCAL_DATE)
            repository.deleteWeightLogByDate(dateString)
        }
    }

    fun openSetLogger(item: ExerciseLogItem) {
        _selectedExerciseForLoggingId.value = item.exerciseId
    }

    fun closeSetLogger() {
        _selectedExerciseForLoggingId.value = null
    }

    fun toggleSetCompletion(exerciseId: Long, setNumber: Int) {
        val currentItem = uiState.value.exercises.find { it.exerciseId == exerciseId } ?: return
        val currentSets = currentItem.sets.toMutableList()
        val setIndex = currentSets.indexOfFirst { it.setNumber == setNumber }
        if (setIndex == -1) return

        val targetSet = currentSets[setIndex]
        val willBeCompleted = !targetSet.isCompleted
        currentSets[setIndex] = targetSet.copy(isCompleted = willBeCompleted)

        val setRecords = currentSets.map {
            WorkoutSetRecord(
                setNumber = it.setNumber,
                reps = it.reps,
                weight = it.weight,
                isCompleted = it.isCompleted
            )
        }

        val dateString = _selectedDate.value.format(DateTimeFormatter.ISO_LOCAL_DATE)
        val templateId = uiState.value.assignedTemplate?.template?.id

        viewModelScope.launch {
            val updatedLog = repository.saveExerciseSets(
                date = dateString,
                templateId = templateId,
                exerciseId = exerciseId,
                sets = setRecords,
                durationSeconds = currentItem.actualDurationSeconds
            )

            _inMemoryAdjustments.update { current ->
                current + (exerciseId to updatedLog)
            }

            // If a set was just marked complete, trigger rest timer suggestion if restTime > 0
            if (willBeCompleted && currentItem.restTimeSeconds > 0) {
                _activeRestTimer.value = currentItem
            }
        }
    }

    fun updateSetValues(exerciseId: Long, setNumber: Int, reps: Int, weight: Double) {
        val currentItem = uiState.value.exercises.find { it.exerciseId == exerciseId } ?: return
        val currentSets = currentItem.sets.toMutableList()
        val setIndex = currentSets.indexOfFirst { it.setNumber == setNumber }
        if (setIndex == -1) return

        currentSets[setIndex] = currentSets[setIndex].copy(
            reps = reps.coerceAtLeast(1),
            weight = weight.coerceAtLeast(0.0)
        )

        val setRecords = currentSets.map {
            WorkoutSetRecord(
                setNumber = it.setNumber,
                reps = it.reps,
                weight = it.weight,
                isCompleted = it.isCompleted
            )
        }

        val dateString = _selectedDate.value.format(DateTimeFormatter.ISO_LOCAL_DATE)
        val templateId = uiState.value.assignedTemplate?.template?.id

        viewModelScope.launch {
            val updatedLog = repository.saveExerciseSets(
                date = dateString,
                templateId = templateId,
                exerciseId = exerciseId,
                sets = setRecords,
                durationSeconds = currentItem.actualDurationSeconds
            )

            _inMemoryAdjustments.update { current ->
                current + (exerciseId to updatedLog)
            }
        }
    }

    fun addSet(exerciseId: Long) {
        val currentItem = uiState.value.exercises.find { it.exerciseId == exerciseId } ?: return
        val currentSets = currentItem.sets.toMutableList()
        val nextNumber = (currentSets.maxOfOrNull { it.setNumber } ?: 0) + 1
        val lastSet = currentSets.lastOrNull()
        val defaultReps = lastSet?.reps ?: currentItem.targetReps
        val defaultWeight = lastSet?.weight ?: currentItem.actualWeight

        currentSets.add(
            WorkoutSetUiModel(
                setNumber = nextNumber,
                reps = defaultReps,
                weight = defaultWeight,
                isCompleted = false
            )
        )

        val setRecords = currentSets.map {
            WorkoutSetRecord(
                setNumber = it.setNumber,
                reps = it.reps,
                weight = it.weight,
                isCompleted = it.isCompleted
            )
        }

        val dateString = _selectedDate.value.format(DateTimeFormatter.ISO_LOCAL_DATE)
        val templateId = uiState.value.assignedTemplate?.template?.id

        viewModelScope.launch {
            val updatedLog = repository.saveExerciseSets(
                date = dateString,
                templateId = templateId,
                exerciseId = exerciseId,
                sets = setRecords,
                durationSeconds = currentItem.actualDurationSeconds
            )

            _inMemoryAdjustments.update { current ->
                current + (exerciseId to updatedLog)
            }
        }
    }

    fun removeSet(exerciseId: Long, setNumber: Int) {
        val currentItem = uiState.value.exercises.find { it.exerciseId == exerciseId } ?: return
        val currentSets = currentItem.sets.filter { it.setNumber != setNumber }
        if (currentSets.isEmpty()) return

        // Re-index remaining sets so numbers are consecutive 1..N
        val reindexed = currentSets.mapIndexed { index, item ->
            item.copy(setNumber = index + 1)
        }

        val setRecords = reindexed.map {
            WorkoutSetRecord(
                setNumber = it.setNumber,
                reps = it.reps,
                weight = it.weight,
                isCompleted = it.isCompleted
            )
        }

        val dateString = _selectedDate.value.format(DateTimeFormatter.ISO_LOCAL_DATE)
        val templateId = uiState.value.assignedTemplate?.template?.id

        viewModelScope.launch {
            val updatedLog = repository.saveExerciseSets(
                date = dateString,
                templateId = templateId,
                exerciseId = exerciseId,
                sets = setRecords,
                durationSeconds = currentItem.actualDurationSeconds
            )

            _inMemoryAdjustments.update { current ->
                current + (exerciseId to updatedLog)
            }
        }
    }

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

            // Mark all sets completed or uncompleted accordingly
            val updatedSets = item.sets.map { it.copy(isCompleted = willBeCompleted) }
            val setRecords = updatedSets.map {
                WorkoutSetRecord(
                    setNumber = it.setNumber,
                    reps = it.reps,
                    weight = it.weight,
                    isCompleted = it.isCompleted
                )
            }

            val savedLog = repository.saveExerciseSets(
                date = dateString,
                templateId = templateId,
                exerciseId = item.exerciseId,
                sets = setRecords,
                durationSeconds = item.actualDurationSeconds
            )

            _inMemoryAdjustments.update { current ->
                current + (item.exerciseId to savedLog)
            }

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

private data class WorkoutDayState(
    val template: com.fitflow.app.data.local.relation.TemplateWithExercises?,
    val exerciseItems: List<ExerciseLogItem>,
    val completedCount: Int,
    val totalCount: Int,
    val progressPercent: Float,
    val activeTimer: ExerciseLogItem?,
    val selectedExerciseItem: ExerciseLogItem?
)

