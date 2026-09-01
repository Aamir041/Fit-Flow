package com.fitflow.app.di

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.fitflow.app.data.local.ThemePreferences
import com.fitflow.app.data.repository.FitFlowRepository
import com.fitflow.app.ui.exercises.ExerciseLibraryViewModel
import com.fitflow.app.ui.food.FoodViewModel
import com.fitflow.app.ui.history.HistoryViewModel
import com.fitflow.app.ui.home.HomeViewModel
import com.fitflow.app.ui.schedule.ScheduleViewModel
import com.fitflow.app.ui.settings.SettingsViewModel
import com.fitflow.app.ui.templates.TemplateEditViewModel
import com.fitflow.app.ui.templates.TemplatesViewModel

class ViewModelFactory(
    private val repository: FitFlowRepository? = null,
    private val templateIdArg: Long? = null,
    private val themePreferences: ThemePreferences? = null
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(SettingsViewModel::class.java) -> {
                val prefs = themePreferences ?: throw IllegalArgumentException("ThemePreferences required for SettingsViewModel")
                SettingsViewModel(prefs) as T
            }
            modelClass.isAssignableFrom(HomeViewModel::class.java) -> {
                val repo = repository ?: throw IllegalArgumentException("Repository required for HomeViewModel")
                HomeViewModel(repo) as T
            }
            modelClass.isAssignableFrom(FoodViewModel::class.java) -> {
                val repo = repository ?: throw IllegalArgumentException("Repository required for FoodViewModel")
                FoodViewModel(repo) as T
            }
            modelClass.isAssignableFrom(TemplatesViewModel::class.java) -> {
                val repo = repository ?: throw IllegalArgumentException("Repository required for TemplatesViewModel")
                TemplatesViewModel(repo) as T
            }
            modelClass.isAssignableFrom(TemplateEditViewModel::class.java) -> {
                val repo = repository ?: throw IllegalArgumentException("Repository required for TemplateEditViewModel")
                TemplateEditViewModel(repo, templateIdArg) as T
            }
            modelClass.isAssignableFrom(ScheduleViewModel::class.java) -> {
                val repo = repository ?: throw IllegalArgumentException("Repository required for ScheduleViewModel")
                ScheduleViewModel(repo) as T
            }
            modelClass.isAssignableFrom(ExerciseLibraryViewModel::class.java) -> {
                val repo = repository ?: throw IllegalArgumentException("Repository required for ExerciseLibraryViewModel")
                ExerciseLibraryViewModel(repo) as T
            }
            modelClass.isAssignableFrom(HistoryViewModel::class.java) -> {
                val repo = repository ?: throw IllegalArgumentException("Repository required for HistoryViewModel")
                HistoryViewModel(repo) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
