package com.fitflow.app.ui.exercises

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fitflow.app.data.local.entity.ExerciseEntity
import com.fitflow.app.data.repository.FitFlowRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

private data class DialogState(
    val isAddOpen: Boolean = false,
    val editExercise: ExerciseEntity? = null,
    val deleteExercise: ExerciseEntity? = null
)

class ExerciseLibraryViewModel(
    private val repository: FitFlowRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    private val _selectedCategory = MutableStateFlow("All")
    private val _dialogState = MutableStateFlow(DialogState())

    private val _exerciseListState = combine(
        repository.getAllExercises(),
        _searchQuery,
        _selectedCategory
    ) { allExercises, query, category ->
        val dynamicCategories = (listOf("All") + DefaultCategories + allExercises.map { it.category }).distinct()

        val filtered = allExercises.filter { ex ->
            val matchesCategory = category == "All" || ex.category.equals(category, ignoreCase = true)
            val matchesSearch = query.isBlank() || ex.name.contains(query, ignoreCase = true)
            matchesCategory && matchesSearch
        }
        Triple(allExercises.size, dynamicCategories, filtered)
    }

    val uiState: StateFlow<ExerciseLibraryUiState> = combine(
        _exerciseListState,
        _searchQuery,
        _selectedCategory,
        _dialogState
    ) { (totalCount, categories, filteredList), query, category, dialogs ->
        ExerciseLibraryUiState(
            exercises = filteredList,
            totalCount = totalCount,
            searchQuery = query,
            selectedCategory = category,
            availableCategories = categories,
            isAddDialogOpen = dialogs.isAddOpen,
            exerciseToEdit = dialogs.editExercise,
            exerciseToDelete = dialogs.deleteExercise,
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ExerciseLibraryUiState()
    )

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setSelectedCategory(category: String) {
        _selectedCategory.value = category
    }

    fun openAddExerciseDialog() {
        _dialogState.value = DialogState(isAddOpen = true, editExercise = null)
    }

    fun openEditExerciseDialog(exercise: ExerciseEntity) {
        _dialogState.value = DialogState(isAddOpen = true, editExercise = exercise)
    }

    fun closeDialog() {
        _dialogState.value = DialogState(isAddOpen = false, editExercise = null)
    }

    fun promptDelete(exercise: ExerciseEntity) {
        _dialogState.value = _dialogState.value.copy(deleteExercise = exercise)
    }

    fun dismissDeletePrompt() {
        _dialogState.value = _dialogState.value.copy(deleteExercise = null)
    }

    fun confirmDelete() {
        val exercise = _dialogState.value.deleteExercise ?: return
        viewModelScope.launch {
            repository.deleteExercise(exercise)
            _dialogState.value = _dialogState.value.copy(deleteExercise = null)
        }
    }

    fun saveExercise(
        name: String,
        category: String,
        defaultSets: Int,
        defaultReps: Int,
        isSprint: Boolean = false,
        defaultDurationSeconds: Int = 30
    ) {
        val editing = _dialogState.value.editExercise
        viewModelScope.launch {
            if (editing != null) {
                repository.updateExercise(
                    editing.copy(
                        name = name.trim(),
                        category = category,
                        defaultSets = defaultSets,
                        defaultReps = defaultReps,
                        isSprint = isSprint,
                        defaultDurationSeconds = defaultDurationSeconds
                    )
                )
            } else {
                repository.insertExercise(
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
            }
            closeDialog()
        }
    }
}
