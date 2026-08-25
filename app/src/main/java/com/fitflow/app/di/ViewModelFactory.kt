package com.fitflow.app.di

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.fitflow.app.data.repository.FitFlowRepository
import com.fitflow.app.ui.exercises.ExerciseLibraryViewModel
import com.fitflow.app.ui.history.HistoryViewModel
import com.fitflow.app.ui.home.HomeViewModel
import com.fitflow.app.ui.schedule.ScheduleViewModel
import com.fitflow.app.ui.templates.TemplateEditViewModel
import com.fitflow.app.ui.templates.TemplatesViewModel

class ViewModelFactory(
    private val repository: FitFlowRepository,
    private val templateIdArg: Long? = null
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(HomeViewModel::class.java) -> {
                HomeViewModel(repository) as T
            }
            modelClass.isAssignableFrom(TemplatesViewModel::class.java) -> {
                TemplatesViewModel(repository) as T
            }
            modelClass.isAssignableFrom(TemplateEditViewModel::class.java) -> {
                TemplateEditViewModel(repository, templateIdArg) as T
            }
            modelClass.isAssignableFrom(ScheduleViewModel::class.java) -> {
                ScheduleViewModel(repository) as T
            }
            modelClass.isAssignableFrom(ExerciseLibraryViewModel::class.java) -> {
                ExerciseLibraryViewModel(repository) as T
            }
            modelClass.isAssignableFrom(HistoryViewModel::class.java) -> {
                HistoryViewModel(repository) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
