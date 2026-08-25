package com.fitflow.app.ui.templates

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fitflow.app.data.local.entity.ExerciseEntity
import com.fitflow.app.data.local.entity.TemplateEntity
import com.fitflow.app.data.local.entity.TemplateExerciseEntity
import com.fitflow.app.data.repository.FitFlowRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class EditableExerciseItem(
    val exerciseId: Long,
    val name: String,
    val category: String,
    val targetSets: Int = 3,
    val targetReps: Int = 10,
    val restTimeSeconds: Int = 90,
    val isSprint: Boolean = false,
    val targetDurationSeconds: Int = 30
)

data class TemplateEditUiState(
    val templateId: Long = 0L,
    val templateName: String = "",
    val exercises: List<EditableExerciseItem> = emptyList(),
    val availableExercises: List<ExerciseEntity> = emptyList(),
    val selectedCategory: String = "All",
    val searchQuery: String = "",
    val isPickerOpen: Boolean = false,
    val isLoading: Boolean = false,
    val nameError: String? = null,
    val generalError: String? = null
)

private data class FormState(
    val name: String = "",
    val exercises: List<EditableExerciseItem> = emptyList(),
    val isLoading: Boolean = false,
    val nameError: String? = null,
    val generalError: String? = null
)

private data class PickerFilterState(
    val isPickerOpen: Boolean = false,
    val selectedCategory: String = "All",
    val searchQuery: String = ""
)

class TemplateEditViewModel(
    private val repository: FitFlowRepository,
    private val initialTemplateId: Long?
) : ViewModel() {

    private val _formState = MutableStateFlow(FormState(isLoading = initialTemplateId != null && initialTemplateId > 0L))
    private val _pickerFilterState = MutableStateFlow(PickerFilterState())

    private val _saveSuccessEvent = MutableSharedFlow<Boolean>()
    val saveSuccessEvent: SharedFlow<Boolean> = _saveSuccessEvent.asSharedFlow()

    init {
        if (initialTemplateId != null && initialTemplateId > 0L) {
            loadTemplate(initialTemplateId)
        }
    }

    private fun loadTemplate(id: Long) {
        viewModelScope.launch {
            _formState.update { it.copy(isLoading = true) }
            val template = repository.getTemplateWithExercisesOnce(id)
            if (template != null) {
                val mapped = template.exercises
                    .sortedBy { it.templateExercise.orderIndex }
                    .map {
                        EditableExerciseItem(
                            exerciseId = it.exercise.id,
                            name = it.exercise.name,
                            category = it.exercise.category,
                            targetSets = it.templateExercise.targetSets,
                            targetReps = it.templateExercise.targetReps,
                            restTimeSeconds = it.templateExercise.restTimeSeconds,
                            isSprint = it.exercise.isSprint,
                            targetDurationSeconds = it.templateExercise.targetDurationSeconds
                        )
                    }
                _formState.update {
                    it.copy(
                        name = template.template.name,
                        exercises = mapped,
                        isLoading = false
                    )
                }
            } else {
                _formState.update { it.copy(isLoading = false) }
            }
        }
    }

    private val _filteredExercises = combine(
        repository.getAllExercises(),
        _pickerFilterState
    ) { allExercises, picker ->
        allExercises.filter { ex ->
            val matchesCategory = (picker.selectedCategory == "All" || ex.category.equals(picker.selectedCategory, ignoreCase = true))
            val matchesSearch = picker.searchQuery.isBlank() || ex.name.contains(picker.searchQuery, ignoreCase = true)
            matchesCategory && matchesSearch
        }
    }

    val uiState: StateFlow<TemplateEditUiState> = combine(
        _formState,
        _pickerFilterState,
        _filteredExercises
    ) { form, picker, filteredAvailable ->
        TemplateEditUiState(
            templateId = initialTemplateId ?: 0L,
            templateName = form.name,
            exercises = form.exercises,
            availableExercises = filteredAvailable,
            selectedCategory = picker.selectedCategory,
            searchQuery = picker.searchQuery,
            isPickerOpen = picker.isPickerOpen,
            isLoading = form.isLoading,
            nameError = form.nameError,
            generalError = form.generalError
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = TemplateEditUiState(isLoading = initialTemplateId != null && initialTemplateId > 0L)
    )

    fun onNameChange(name: String) {
        _formState.update {
            it.copy(
                name = name,
                nameError = if (name.isNotBlank()) null else it.nameError
            )
        }
    }

    fun openExercisePicker() {
        _pickerFilterState.update { it.copy(isPickerOpen = true) }
    }

    fun closeExercisePicker() {
        _pickerFilterState.update { it.copy(isPickerOpen = false) }
    }

    fun setCategoryFilter(category: String) {
        _pickerFilterState.update { it.copy(selectedCategory = category) }
    }

    fun setSearchQuery(query: String) {
        _pickerFilterState.update { it.copy(searchQuery = query) }
    }

    fun addExercise(exercise: ExerciseEntity) {
        _formState.update { current ->
            val exists = current.exercises.any { it.exerciseId == exercise.id }
            if (!exists) {
                val updatedList = current.exercises + EditableExerciseItem(
                    exerciseId = exercise.id,
                    name = exercise.name,
                    category = exercise.category,
                    targetSets = exercise.defaultSets,
                    targetReps = exercise.defaultReps,
                    restTimeSeconds = if (exercise.isSprint) 0 else 90,
                    isSprint = exercise.isSprint,
                    targetDurationSeconds = exercise.defaultDurationSeconds
                )
                current.copy(exercises = updatedList, generalError = null)
            } else {
                current
            }
        }
        _pickerFilterState.update { it.copy(isPickerOpen = false) }
    }

    fun createAndAddCustomExercise(
        name: String,
        category: String,
        defaultSets: Int,
        defaultReps: Int,
        isSprint: Boolean = false,
        defaultDurationSeconds: Int = 30
    ) {
        viewModelScope.launch {
            val newExerciseId = repository.insertExercise(
                ExerciseEntity(
                    name = name.trim(),
                    category = category,
                    defaultSets = defaultSets,
                    defaultReps = defaultReps,
                    isCustom = true,
                    isSprint = isSprint,
                    defaultDurationSeconds = defaultDurationSeconds
                )
            )

            _formState.update { current ->
                val updatedList = current.exercises + EditableExerciseItem(
                    exerciseId = newExerciseId,
                    name = name.trim(),
                    category = category,
                    targetSets = defaultSets,
                    targetReps = defaultReps,
                    restTimeSeconds = if (isSprint) 0 else 90,
                    isSprint = isSprint,
                    targetDurationSeconds = defaultDurationSeconds
                )
                current.copy(exercises = updatedList, generalError = null)
            }
            _pickerFilterState.update { it.copy(isPickerOpen = false) }
        }
    }

    fun removeExercise(index: Int) {
        _formState.update { current ->
            val list = current.exercises.toMutableList()
            if (index in list.indices) {
                list.removeAt(index)
                current.copy(exercises = list)
            } else {
                current
            }
        }
    }

    fun moveExerciseUp(index: Int) {
        _formState.update { current ->
            if (index > 0 && index in current.exercises.indices) {
                val list = current.exercises.toMutableList()
                val item = list.removeAt(index)
                list.add(index - 1, item)
                current.copy(exercises = list)
            } else {
                current
            }
        }
    }

    fun moveExerciseDown(index: Int) {
        _formState.update { current ->
            if (index in current.exercises.indices && index < current.exercises.lastIndex) {
                val list = current.exercises.toMutableList()
                val item = list.removeAt(index)
                list.add(index + 1, item)
                current.copy(exercises = list)
            } else {
                current
            }
        }
    }

    fun updateExerciseValues(index: Int, sets: Int, reps: Int, restSeconds: Int) {
        _formState.update { current ->
            val list = current.exercises.toMutableList()
            if (index in list.indices) {
                list[index] = list[index].copy(
                    targetSets = sets,
                    targetReps = reps,
                    restTimeSeconds = restSeconds
                )
                current.copy(exercises = list)
            } else {
                current
            }
        }
    }

    fun updateSprintDuration(index: Int, durationSeconds: Int) {
        _formState.update { current ->
            val list = current.exercises.toMutableList()
            if (index in list.indices) {
                list[index] = list[index].copy(targetDurationSeconds = durationSeconds)
                current.copy(exercises = list)
            } else {
                current
            }
        }
    }

    fun saveTemplate() {
        val currentForm = _formState.value
        val name = currentForm.name.trim()
        if (name.isBlank()) {
            _formState.update { it.copy(nameError = "Template name cannot be empty") }
            return
        }

        val items = currentForm.exercises
        if (items.isEmpty()) {
            _formState.update { it.copy(generalError = "Please add at least one exercise to the template") }
            return
        }

        viewModelScope.launch {
            _formState.update { it.copy(isLoading = true) }

            val isUnique = repository.isTemplateNameUnique(name, initialTemplateId ?: 0L)
            if (!isUnique) {
                _formState.update {
                    it.copy(
                        isLoading = false,
                        nameError = "A template with this name already exists"
                    )
                }
                return@launch
            }

            val templateEntity = TemplateEntity(
                id = initialTemplateId ?: 0L,
                name = name
            )

            val exerciseEntities = items.mapIndexed { idx, item ->
                TemplateExerciseEntity(
                    templateId = initialTemplateId ?: 0L,
                    exerciseId = item.exerciseId,
                    targetSets = item.targetSets,
                    targetReps = item.targetReps,
                    targetDurationSeconds = item.targetDurationSeconds,
                    restTimeSeconds = item.restTimeSeconds,
                    orderIndex = idx
                )
            }

            repository.saveTemplateWithExercises(templateEntity, exerciseEntities)
            _formState.update { it.copy(isLoading = false) }
            _saveSuccessEvent.emit(true)
        }
    }
}
