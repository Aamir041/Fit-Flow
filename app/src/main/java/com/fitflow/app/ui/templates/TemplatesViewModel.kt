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

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

data class ExportShareEvent(
    val templateName: String,
    val jsonContent: String
)

class TemplatesViewModel(
    private val repository: FitFlowRepository
) : ViewModel() {

    private val _templateToDelete = MutableStateFlow<TemplateEntity?>(null)
    private val _infoMessage = MutableStateFlow<String?>(null)
    private val _errorMessage = MutableStateFlow<String?>(null)

    private val _exportShareEvent = MutableSharedFlow<ExportShareEvent>()
    val exportShareEvent: SharedFlow<ExportShareEvent> = _exportShareEvent.asSharedFlow()

    val uiState: StateFlow<TemplatesUiState> = combine(
        repository.getAllTemplatesWithExercises(),
        _templateToDelete,
        _infoMessage,
        _errorMessage
    ) { templates, templateToDelete, info, error ->
        TemplatesUiState(
            templates = templates,
            isLoading = false,
            templateToDelete = templateToDelete,
            infoMessage = info,
            errorMessage = error
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
            _infoMessage.value = "Template '${template.name}' deleted"
        }
    }

    fun exportAllTemplates() {
        viewModelScope.launch {
            try {
                val json = repository.exportAllTemplatesToJson()
                _exportShareEvent.emit(ExportShareEvent("fitflow_templates_backup", json))
                _infoMessage.value = "All templates ready to save/share as JSON"
            } catch (e: Exception) {
                _errorMessage.value = "Export error: ${e.localizedMessage ?: "Unknown error"}"
            }
        }
    }

    fun importTemplateFromJson(jsonString: String) {
        viewModelScope.launch {
            try {
                val result = repository.importTemplateBundleFromJson(jsonString)
                result.fold(
                    onSuccess = { count ->
                        _infoMessage.value = "Successfully imported $count template(s)!"
                    },
                    onFailure = { error ->
                        _errorMessage.value = error.localizedMessage ?: "Failed to import templates from JSON"
                    }
                )
            } catch (e: Exception) {
                _errorMessage.value = "Invalid template JSON format"
            }
        }
    }

    fun clearMessages() {
        _infoMessage.value = null
        _errorMessage.value = null
    }
}
