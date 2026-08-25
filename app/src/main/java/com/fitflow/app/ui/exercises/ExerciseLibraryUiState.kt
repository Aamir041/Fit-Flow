package com.fitflow.app.ui.exercises

import com.fitflow.app.data.local.entity.ExerciseEntity

data class ExerciseLibraryUiState(
    val exercises: List<ExerciseEntity> = emptyList(),
    val totalCount: Int = 0,
    val searchQuery: String = "",
    val selectedCategory: String = "All",
    val availableCategories: List<String> = listOf("All", "Chest", "Back", "Legs", "Shoulders", "Arms", "Core", "Cardio"),
    val isAddDialogOpen: Boolean = false,
    val exerciseToEdit: ExerciseEntity? = null,
    val exerciseToDelete: ExerciseEntity? = null,
    val isLoading: Boolean = true
)

