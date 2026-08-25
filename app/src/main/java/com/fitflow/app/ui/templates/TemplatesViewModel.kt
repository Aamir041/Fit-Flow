package com.fitflow.app.ui.templates

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fitflow.app.data.local.entity.TemplateEntity
import com.fitflow.app.data.repository.FitFlowRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class TemplatesViewModel(
    private val repository: FitFlowRepository
) : ViewModel() {

    private val _templateToDelete = MutableStateFlow<TemplateEntity?>(null)

    val uiState: StateFlow<TemplatesUiState> = combine(
        repository.getAllTemplatesWithExercises(),
        _templateToDelete
    ) { templates, templateToDelete ->
        TemplatesUiState(
            templates = templates,
            isLoading = false,
            templateToDelete = templateToDelete
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = TemplatesUiState()
    )

    fun promptDelete(template: TemplateEntity) {
        _templateToDelete.value = template
    }

    fun dismissDeletePrompt() {
        _templateToDelete.value = null
    }

    fun confirmDelete() {
        val template = _templateToDelete.value ?: return
        viewModelScope.launch {
            repository.deleteTemplate(template)
            _templateToDelete.value = null
        }
    }
}
