package com.fitflow.app.ui.home

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Celebration
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.MonitorWeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.foundation.clickable
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fitflow.app.ui.components.EmptyStateCard
import com.fitflow.app.ui.components.FitFlowTopBar
import com.fitflow.app.ui.components.RestTimerDialog
import com.fitflow.app.ui.theme.FitFlowTheme
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onNavigateToSchedule: () -> Unit,
    onNavigateToSettings: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    HomeScreenContent(
        uiState = uiState,
        onCardClick = { item ->
            viewModel.openSetLogger(item)
        },
        onToggleCompleted = { item ->
            viewModel.toggleExerciseCompletion(item)
        },
        onToggleSet = { exId, setNumber ->
            viewModel.toggleSetCompletion(exId, setNumber)
        },
        onUpdateSetValues = { exId, setNumber, reps, weight ->
            viewModel.updateSetValues(exId, setNumber, reps, weight)
        },
        onAddSet = { exId ->
            viewModel.addSet(exId)
        },
        onRemoveSet = { exId, setNumber ->
            viewModel.removeSet(exId, setNumber)
        },
        onDurationChanged = { exId, duration ->
            viewModel.updateSprintDuration(exId, duration)
        },
        onOpenTimer = { item ->
            viewModel.openRestTimer(item)
        },
        onCloseTimer = {
            viewModel.closeRestTimer()
        },
        onCloseSetLogger = {
            viewModel.closeSetLogger()
        },
        onSaveWeight = { weightKg ->
            viewModel.saveTodayWeight(weightKg)
        },
        onDeleteWeight = {
            viewModel.deleteTodayWeight()
        },
        onNavigateToSchedule = onNavigateToSchedule,
        onNavigateToSettings = onNavigateToSettings,
        modifier = modifier
    )
}

@Composable
fun HomeScreenContent(
    uiState: HomeUiState,
    onCardClick: (ExerciseLogItem) -> Unit,
    onToggleCompleted: (ExerciseLogItem) -> Unit,
    onToggleSet: (exerciseId: Long, setNumber: Int) -> Unit,
    onUpdateSetValues: (exerciseId: Long, setNumber: Int, reps: Int, weight: Double) -> Unit,
    onAddSet: (exerciseId: Long) -> Unit,
    onRemoveSet: (exerciseId: Long, setNumber: Int) -> Unit,
    onDurationChanged: (exerciseId: Long, durationSeconds: Int) -> Unit = { _, _ -> },
    onOpenTimer: (ExerciseLogItem) -> Unit,
    onCloseTimer: () -> Unit,
    onCloseSetLogger: () -> Unit,
    onSaveWeight: (Double) -> Unit = {},
    onDeleteWeight: () -> Unit = {},
    onNavigateToSchedule: () -> Unit,
    onNavigateToSettings: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val dateFormatter = DateTimeFormatter.ofPattern("EEEE, MMMM d")
    val formattedDate = uiState.currentDate.format(dateFormatter)

    val animatedProgress by animateFloatAsState(
        targetValue = uiState.progressPercent,
        label = "WorkoutProgressBar"
    )

    var showWeightDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            FitFlowTopBar(
                title = "Today's Workout",
                subtitle = formattedDate,
                onSettingsClick = onNavigateToSettings
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
        } else if (uiState.assignedTemplate == null || uiState.exercises.isEmpty()) {
            // Rest Day / No template assigned for today
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Today's Weight Card on Rest Day
                item {
                    DailyWeightCard(
                        todayWeight = uiState.todayWeightLog,
                        lastWeight = uiState.lastRecordedWeight,
                        onLogWeightClick = { showWeightDialog = true }
                    )
                }

                item {
                    EmptyStateCard(
                        title = "Rest Day or No Template Assigned",
                        description = "There is no workout template attached to ${uiState.dayOfWeek.name.lowercase().replaceFirstChar { it.uppercase() }}. Tap below to assign a template or customize your weekly routine.",
                        icon = Icons.Default.CalendarMonth,
                        actionButtonText = "Assign Template to Today",
                        onActionClick = onNavigateToSchedule
                    )
                }
            }
        } else {
            // Template Active
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Today's Body Weight Quick Entry Card
                item {
                    DailyWeightCard(
                        todayWeight = uiState.todayWeightLog,
                        lastWeight = uiState.lastRecordedWeight,
                        onLogWeightClick = { showWeightDialog = true }
                    )
                }

                // Workout Overview Header Card
                item {
                    WorkoutOverviewCard(
                        templateName = uiState.assignedTemplate.template.name,
                        completedCount = uiState.completedCount,
                        totalCount = uiState.totalCount,
                        progressPercent = animatedProgress
                    )
                }

                // Exercise Cards
                items(
                    items = uiState.exercises,
                    key = { it.templateExerciseId }
                ) { item ->
                    ExerciseLogCard(
                        item = item,
                        onCardClick = { onCardClick(item) },
                        onToggleCompleted = { onToggleCompleted(item) },
                        onOpenTimer = { onOpenTimer(item) }
                    )
                }

                // Bottom spacer
                item {
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }

        // Daily Weight Logger Dialog
        if (showWeightDialog) {
            com.fitflow.app.ui.components.LogWeightDialog(
                initialDate = uiState.currentDate,
                currentWeight = uiState.lastRecordedWeight,
                existingRecordedWeight = uiState.todayWeightLog,
                onSave = { _, weightKg ->
                    onSaveWeight(weightKg)
                },
                onDelete = {
                    onDeleteWeight()
                },
                onDismiss = { showWeightDialog = false }
            )
        }

        // Set Logger Dialog
        uiState.selectedExerciseForLogging?.let { selectedItem ->
            LogSetsDialog(
                item = selectedItem,
                onToggleSet = { setNumber ->
                    onToggleSet(selectedItem.exerciseId, setNumber)
                },
                onUpdateSetValues = { setNumber, reps, weight ->
                    onUpdateSetValues(selectedItem.exerciseId, setNumber, reps, weight)
                },
                onAddSet = {
                    onAddSet(selectedItem.exerciseId)
                },
                onRemoveSet = { setNumber ->
                    onRemoveSet(selectedItem.exerciseId, setNumber)
                },
                onDurationChanged = { duration ->
                    onDurationChanged(selectedItem.exerciseId, duration)
                },
                onOpenTimer = {
                    onOpenTimer(selectedItem)
                },
                onDismiss = onCloseSetLogger
            )
        }

        // Rest Timer Dialog
        if (uiState.activeRestTimer != null) {
            RestTimerDialog(
                initialSeconds = uiState.activeRestTimer.restTimeSeconds.takeIf { it > 0 } ?: 90,
                exerciseName = uiState.activeRestTimer.name,
                onDismiss = onCloseTimer
            )
        }
    }
}

@Composable
fun WorkoutOverviewCard(
    templateName: String,
    completedCount: Int,
    totalCount: Int,
    progressPercent: Float,
    modifier: Modifier = Modifier
) {
    val isAllDone = totalCount > 0 && completedCount == totalCount

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(
                1.dp,
                if (isAllDone) MaterialTheme.colorScheme.primary.copy(alpha = 0.6f) else MaterialTheme.colorScheme.outlineVariant,
                RoundedCornerShape(20.dp)
            )
            .padding(18.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "ACTIVE SPLIT",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = templateName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                if (isAllDone) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Celebration,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.padding(2.dp))
                            Text(
                                text = "Crushed It!",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Progress text and bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "$completedCount of $totalCount exercises completed",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "${(progressPercent * 100).toInt()}%",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (isAllDone) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            LinearProgressIndicator(
                progress = { progressPercent },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
                strokeCap = StrokeCap.Round
            )
        }
    }
}

@Composable
fun DailyWeightCard(
    todayWeight: Double?,
    lastWeight: Double?,
    onLogWeightClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isLoggedToday = todayWeight != null

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(
                width = 1.dp,
                color = if (isLoggedToday) MaterialTheme.colorScheme.primary.copy(alpha = 0.5f) else MaterialTheme.colorScheme.outlineVariant,
                shape = RoundedCornerShape(16.dp)
            )
            .clickable { onLogWeightClick() },
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(
                            if (isLoggedToday) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                            else MaterialTheme.colorScheme.surfaceVariant
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.MonitorWeight,
                        contentDescription = null,
                        tint = if (isLoggedToday) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = "BODY WEIGHT",
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 1.sp
                    )

                    if (isLoggedToday) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "$todayWeight kg",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "• Today",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    } else {
                        Text(
                            text = if (lastWeight != null) "Last: $lastWeight kg" else "Not logged today",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            if (isLoggedToday) {
                OutlinedButton(
                    onClick = onLogWeightClick,
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "Edit", style = MaterialTheme.typography.labelSmall)
                }
            } else {
                Button(
                    onClick = onLogWeightClick,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Text(text = "Log Weight", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    FitFlowTheme {
        val sampleItems = listOf(
            ExerciseLogItem(
                templateExerciseId = 1,
                exerciseId = 1,
                name = "Barbell Bench Press",
                category = "Chest",
                targetSets = 4,
                targetReps = 8,
                restTimeSeconds = 120,
                actualSets = 4,
                actualReps = 8,
                actualWeight = 85.0,
                isCompleted = true
            ),
            ExerciseLogItem(
                templateExerciseId = 2,
                exerciseId = 2,
                name = "Incline Dumbbell Press",
                category = "Chest",
                targetSets = 3,
                targetReps = 10,
                restTimeSeconds = 90,
                actualSets = 3,
                actualReps = 10,
                actualWeight = 30.0,
                isCompleted = false
            ),
            ExerciseLogItem(
                templateExerciseId = 3,
                exerciseId = 3,
                name = "Overhead Barbell Press",
                category = "Shoulders",
                targetSets = 3,
                targetReps = 8,
                restTimeSeconds = 90,
                actualSets = 3,
                actualReps = 8,
                actualWeight = 50.0,
                isCompleted = false
            )
        )

        HomeScreenContent(
            uiState = HomeUiState(
                currentDate = LocalDate.now(),
                exercises = sampleItems,
                completedCount = 1,
                totalCount = 3,
                progressPercent = 0.33f,
                isLoading = false
            ),
            onCardClick = {},
            onToggleCompleted = {},
            onToggleSet = { _, _ -> },
            onUpdateSetValues = { _, _, _, _ -> },
            onAddSet = {},
            onRemoveSet = { _, _ -> },
            onDurationChanged = { _, _ -> },
            onOpenTimer = {},
            onCloseTimer = {},
            onCloseSetLogger = {},
            onNavigateToSchedule = {}
        )
    }
}
