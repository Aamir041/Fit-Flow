package com.fitflow.app.ui.schedule

import com.fitflow.app.data.local.relation.TemplateWithExercises
import java.time.DayOfWeek

data class DayScheduleItem(
    val dayOfWeek: Int, // 1 to 7
    val dayOfWeekEnum: DayOfWeek,
    val dayName: String,
    val shortDayName: String,
    val isToday: Boolean,
    val templateId: Long?,
    val templateName: String?,
    val exerciseCount: Int
)

data class ScheduleUiState(
    val days: List<DayScheduleItem> = emptyList(),
    val availableTemplates: List<TemplateWithExercises> = emptyList(),
    val selectedDay: DayScheduleItem? = null,
    val isLoading: Boolean = true
)
