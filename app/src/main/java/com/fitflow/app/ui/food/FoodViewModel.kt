package com.fitflow.app.ui.food

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fitflow.app.data.local.entity.FoodLogEntity
import com.fitflow.app.data.repository.FitFlowRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalCoroutinesApi::class)
class FoodViewModel(
    private val repository: FitFlowRepository
) : ViewModel() {

    private val _isAddEditDialogOpen = MutableStateFlow(false)
    private val _foodToEdit = MutableStateFlow<FoodLogEntity?>(null)
    private val _foodToDelete = MutableStateFlow<FoodLogEntity?>(null)
    private val _snackbarMessage = MutableStateFlow<String?>(null)

    private val todayString: String
        get() = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)

    val uiState: StateFlow<FoodUiState> = combine(
        repository.getFoodLogsForDate(todayString),
        _isAddEditDialogOpen,
        _foodToEdit,
        _foodToDelete,
        _snackbarMessage
    ) { logs, isAddEditOpen, foodToEdit, foodToDelete, message ->
        val totalCalories = logs.sumOf { it.calories }
        FoodUiState(
            selectedDate = LocalDate.now(),
            foodLogs = logs,
            totalCalories = totalCalories,
            isLoading = false,
            isAddEditDialogOpen = isAddEditOpen,
            foodToEdit = foodToEdit,
            foodToDelete = foodToDelete,
            snackbarMessage = message
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = FoodUiState()
    )

    fun openAddFoodDialog() {
        _foodToEdit.value = null
        _isAddEditDialogOpen.value = true
    }

    fun openEditFoodDialog(food: FoodLogEntity) {
        _foodToEdit.value = food
        _isAddEditDialogOpen.value = true
    }

    fun closeAddEditDialog() {
        _isAddEditDialogOpen.value = false
        _foodToEdit.value = null
    }

    fun confirmDeleteFood(food: FoodLogEntity) {
        _foodToDelete.value = food
    }

    fun dismissDeleteDialog() {
        _foodToDelete.value = null
    }

    fun deleteFoodConfirmed() {
        val food = _foodToDelete.value ?: return
        viewModelScope.launch {
            repository.deleteFoodLog(food)
            _foodToDelete.value = null
            _snackbarMessage.value = "Removed \"${food.foodName}\""
        }
    }

    fun saveFoodLog(
        foodName: String,
        quantity: Double,
        unit: String,
        calories: Int,
        mealTime: String
    ) {
        val dateString = todayString
        val currentFood = _foodToEdit.value

        viewModelScope.launch {
            if (currentFood != null) {
                val updated = currentFood.copy(
                    foodName = foodName.trim(),
                    quantity = quantity,
                    unit = unit.trim(),
                    calories = calories,
                    mealTime = mealTime.trim()
                )
                repository.updateFoodLog(updated)
                _snackbarMessage.value = "Updated \"${updated.foodName}\""
            } else {
                val newLog = FoodLogEntity(
                    date = dateString,
                    foodName = foodName.trim(),
                    quantity = quantity,
                    unit = unit.trim(),
                    calories = calories,
                    mealTime = mealTime.trim()
                )
                repository.insertFoodLog(newLog)
                _snackbarMessage.value = "Logged \"${newLog.foodName}\""
            }
            closeAddEditDialog()
        }
    }

    fun clearSnackbar() {
        _snackbarMessage.value = null
    }
}
