package com.fitflow.app.ui.schedule

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fitflow.app.ui.components.FitFlowTopBar
import com.fitflow.app.ui.theme.FitFlowTheme
import java.time.DayOfWeek

@Composable
fun ScheduleScreen(
    viewModel: ScheduleViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    ScheduleScreenContent(
        uiState = uiState,
        onDayClick = { viewModel.openTemplatePickerForDay(it) },
        onSelectTemplate = { dayOfWeek, templateId ->
            viewModel.assignTemplateToDay(dayOfWeek, templateId)
        },
        onDismissSheet = { viewModel.closeTemplatePicker() },
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleScreenContent(
    uiState: ScheduleUiState,
    onDayClick: (DayScheduleItem) -> Unit,
    onSelectTemplate: (dayOfWeek: Int, templateId: Long?) -> Unit,
    onDismissSheet: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            FitFlowTopBar(
                title = "Weekly Routine",
                subtitle = "Set templates for each day of the week"
            )
        },
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(
                    items = uiState.days,
                    key = { it.dayOfWeek }
                ) { dayItem ->
                    DayScheduleCard(
                        day = dayItem,
                        onClick = { onDayClick(dayItem) }
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }

        // Template Assignment Bottom Sheet
        if (uiState.selectedDay != null) {
            AssignTemplateBottomSheet(
                day = uiState.selectedDay,
                availableTemplates = uiState.availableTemplates,
                onSelectTemplate = { templateId ->
                    onSelectTemplate(uiState.selectedDay.dayOfWeek, templateId)
                },
                onDismiss = onDismissSheet
            )
        }
    }
}

@Composable
fun DayScheduleCard(
    day: DayScheduleItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val borderColor = if (day.isToday) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
    val cardBg = if (day.isToday) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f) else MaterialTheme.colorScheme.surface

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(
                width = if (day.isToday) 1.5.dp else 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(16.dp)
            )
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        elevation = CardDefaults.cardElevation(defaultElevation = if (day.isToday) 3.dp else 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Day Label + Icon
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                // Day Icon Indicator
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (day.isToday) MaterialTheme.colorScheme.primary.copy(alpha = 0.18f)
                            else if (day.templateName != null) MaterialTheme.colorScheme.surfaceVariant
                            else MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (day.templateName != null) {
                        Icon(
                            imageVector = Icons.Default.FitnessCenter,
                            contentDescription = null,
                            tint = if (day.isToday) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(22.dp)
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Bedtime,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = day.dayName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (day.isToday) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(MaterialTheme.colorScheme.primary)
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "TODAY",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 9.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    if (day.templateName != null) {
                        Text(
                            text = "${day.templateName} • ${day.exerciseCount} exercises",
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (day.isToday) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        Text(
                            text = "Rest Day (No Workout)",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                }
            }

            // Right Arrow
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "Edit schedule",
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ScheduleScreenPreview() {
    FitFlowTheme {
        val sampleDays = listOf(
            DayScheduleItem(
                dayOfWeek = DayOfWeek.MONDAY.value,
                dayOfWeekEnum = DayOfWeek.MONDAY,
                dayName = "Monday",
                shortDayName = "Mon",
                isToday = false,
                templateId = 1L,
                templateName = "Push Day (Chest/Triceps)",
                exerciseCount = 4
            ),
            DayScheduleItem(
                dayOfWeek = DayOfWeek.TUESDAY.value,
                dayOfWeekEnum = DayOfWeek.TUESDAY,
                dayName = "Tuesday",
                shortDayName = "Tue",
                isToday = true,
                templateId = 2L,
                templateName = "Pull Day (Back/Biceps)",
                exerciseCount = 5
            ),
            DayScheduleItem(
                dayOfWeek = DayOfWeek.WEDNESDAY.value,
                dayOfWeekEnum = DayOfWeek.WEDNESDAY,
                dayName = "Wednesday",
                shortDayName = "Wed",
                isToday = false,
                templateId = null,
                templateName = null,
                exerciseCount = 0
            )
        )

        ScheduleScreenContent(
            uiState = ScheduleUiState(
                days = sampleDays,
                availableTemplates = emptyList(),
                selectedDay = null,
                isLoading = false
            ),
            onDayClick = {},
            onSelectTemplate = { _, _ -> },
            onDismissSheet = {}
        )
    }
}
