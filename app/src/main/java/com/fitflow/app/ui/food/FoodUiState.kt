package com.fitflow.app.ui.food

import com.fitflow.app.data.local.entity.FoodLogEntity
import java.time.LocalDate

data class FoodUiState(
    val selectedDate: LocalDate = LocalDate.now(),
    val foodLogs: List<FoodLogEntity> = emptyList(),
    val totalCalories: Int = 0,
    val isLoading: Boolean = false,
    val isAddEditDialogOpen: Boolean = false,
    val foodToEdit: FoodLogEntity? = null,
    val foodToDelete: FoodLogEntity? = null,
    val snackbarMessage: String? = null
)
