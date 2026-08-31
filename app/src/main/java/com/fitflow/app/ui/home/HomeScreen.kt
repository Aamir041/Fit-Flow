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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Celebration
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import com.fitflow.app.ui.theme.CyanAccent
import com.fitflow.app.ui.theme.EmeraldLight
import com.fitflow.app.ui.theme.EmeraldPrimary
import com.fitflow.app.ui.theme.FitFlowTheme
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onNavigateToSchedule: () -> Unit,
    onOpenThemeDialog: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    HomeScreenContent(
        uiState = uiState,
        onValuesChanged = { exId, sets, reps, weight ->
            viewModel.updateExerciseValues(exId, sets, reps, weight)
        },
        onDurationChanged = { exId, duration ->
            viewModel.updateSprintDuration(exId, duration)
        },
        onToggleCompleted = { item ->
            viewModel.toggleExerciseCompletion(item)
        },
        onOpenTimer = { item ->
            viewModel.openRestTimer(item)
        },
        onCloseTimer = {
            viewModel.closeRestTimer()
        },
        onNavigateToSchedule = onNavigateToSchedule,
        onOpenThemeDialog = onOpenThemeDialog,
        modifier = modifier
    )
}

@Composable
fun HomeScreenContent(
    uiState: HomeUiState,
    onValuesChanged: (exerciseId: Long, sets: Int, reps: Int, weight: Double) -> Unit,
    onDurationChanged: (exerciseId: Long, durationSeconds: Int) -> Unit = { _, _ -> },
    onToggleCompleted: (ExerciseLogItem) -> Unit,
    onOpenTimer: (ExerciseLogItem) -> Unit,
    onCloseTimer: () -> Unit,
    onNavigateToSchedule: () -> Unit,
    onOpenThemeDialog: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val dateFormatter = DateTimeFormatter.ofPattern("EEEE, MMMM d")
    val formattedDate = uiState.currentDate.format(dateFormatter)

    val animatedProgress by animateFloatAsState(
        targetValue = uiState.progressPercent,
        label = "WorkoutProgressBar"
    )

    Scaffold(
        topBar = {
            FitFlowTopBar(
                title = "Today's Workout",
                subtitle = formattedDate,
                actions = {
                    IconButton(onClick = onOpenThemeDialog) {
                        Icon(
                            imageVector = Icons.Default.Palette,
                            contentDescription = "Change Theme",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
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
                CircularProgressIndicator(color = EmeraldPrimary)
            }
        } else if (uiState.assignedTemplate == null || uiState.exercises.isEmpty()) {
            // Empty state when no template is assigned for today
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(20.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                EmptyStateCard(
                    title = "Rest Day or No Template Assigned",
                    description = "There is no workout template attached to ${uiState.dayOfWeek.name.lowercase().replaceFirstChar { it.uppercase() }}. Tap below to assign a template or customize your weekly routine.",
                    icon = Icons.Default.CalendarMonth,
                    actionButtonText = "Assign Template to Today",
                    onActionClick = onNavigateToSchedule
                )
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
                        onValuesChanged = { sets, reps, weight ->
                            onValuesChanged(item.exerciseId, sets, reps, weight)
                        },
                        onDurationChanged = { duration ->
                            onDurationChanged(item.exerciseId, duration)
                        },
                        onToggleCompleted = {
                            onToggleCompleted(item)
                        },
                        onOpenTimer = {
                            onOpenTimer(item)
                        }
                    )
                }

                // Bottom spacer
                item {
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
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
                if (isAllDone) EmeraldPrimary.copy(alpha = 0.6f) else MaterialTheme.colorScheme.outlineVariant,
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
                        color = EmeraldPrimary,
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
                            .background(EmeraldPrimary.copy(alpha = 0.15f))
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Celebration,
                                contentDescription = null,
                                tint = EmeraldPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.padding(2.dp))
                            Text(
                                text = "Crushed It!",
                                style = MaterialTheme.typography.labelSmall,
                                color = EmeraldPrimary,
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
                    color = if (isAllDone) EmeraldPrimary else MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            LinearProgressIndicator(
                progress = { progressPercent },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = EmeraldPrimary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
                strokeCap = StrokeCap.Round
            )
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
            onValuesChanged = { _, _, _, _ -> },
            onToggleCompleted = {},
            onOpenTimer = {},
            onCloseTimer = {},
            onNavigateToSchedule = {}
        )
    }
}
