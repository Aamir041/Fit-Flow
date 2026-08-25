package com.fitflow.app.ui.schedule

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fitflow.app.data.repository.FitFlowRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

class ScheduleViewModel(
    private val repository: FitFlowRepository
) : ViewModel() {

    private val _selectedDay = MutableStateFlow<DayScheduleItem?>(null)

    val uiState: StateFlow<ScheduleUiState> = combine(
        repository.getAllDayAssignments(),
        repository.getAllTemplatesWithExercises(),
        _selectedDay
    ) { assignments, templates, selectedDay ->
        val templatesMap = templates.associateBy { it.template.id }
        val assignmentsMap = assignments.associateBy { it.dayAssignment.dayOfWeek }
        val currentDayOfWeekValue = LocalDate.now().dayOfWeek.value

        val days = (1..7).map { dayIndex ->
            val dayOfWeekEnum = DayOfWeek.of(dayIndex)
            val fullDayName = dayOfWeekEnum.getDisplayName(TextStyle.FULL, Locale.getDefault())
            val shortDayName = dayOfWeekEnum.getDisplayName(TextStyle.SHORT, Locale.getDefault()).uppercase()
            val isToday = dayIndex == currentDayOfWeekValue

            val assignment = assignmentsMap[dayIndex]
            val templateId = assignment?.dayAssignment?.templateId
            val template = if (templateId != null) templatesMap[templateId] else null

            DayScheduleItem(
                dayOfWeek = dayIndex,
                dayOfWeekEnum = dayOfWeekEnum,
                dayName = fullDayName,
                shortDayName = shortDayName,
                isToday = isToday,
                templateId = templateId,
                templateName = template?.template?.name,
                exerciseCount = template?.exercises?.size ?: 0
            )
        }

        ScheduleUiState(
            days = days,
            availableTemplates = templates,
            selectedDay = selectedDay,
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ScheduleUiState()
    )

    fun openTemplatePickerForDay(day: DayScheduleItem) {
        _selectedDay.value = day
    }

    fun closeTemplatePicker() {
        _selectedDay.value = null
    }

    fun assignTemplateToDay(dayOfWeek: Int, templateId: Long?) {
        viewModelScope.launch {
            repository.assignTemplateToDay(dayOfWeek, templateId)
            _selectedDay.value = null
        }
    }
}
