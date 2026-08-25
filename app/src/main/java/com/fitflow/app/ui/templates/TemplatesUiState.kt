package com.fitflow.app.ui.templates

import com.fitflow.app.data.local.entity.TemplateEntity
import com.fitflow.app.data.local.relation.TemplateWithExercises

data class TemplatesUiState(
    val templates: List<TemplateWithExercises> = emptyList(),
    val isLoading: Boolean = true,
    val templateToDelete: TemplateEntity? = null,
    val infoMessage: String? = null,
    val errorMessage: String? = null
)
