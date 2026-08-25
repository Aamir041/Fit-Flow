package com.fitflow.app.ui.history

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.EventNote
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fitflow.app.data.local.entity.ExerciseEntity
import com.fitflow.app.data.local.entity.WorkoutLogEntity
import com.fitflow.app.data.local.relation.WorkoutLogWithExercise
import com.fitflow.app.ui.components.CategoryBadge
import com.fitflow.app.ui.components.EmptyStateCard
import com.fitflow.app.ui.components.FitFlowTopBar
import com.fitflow.app.ui.components.SprintBadge
import com.fitflow.app.ui.theme.CyanAccent
import com.fitflow.app.ui.theme.EmeraldPrimary
import com.fitflow.app.ui.theme.FitFlowTheme
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

@Composable
fun HistoryScreen(
    viewModel: HistoryViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    // Launchers
    val exportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        uri?.let { viewModel.exportHistory(context, it) }
    }

    val importLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let { viewModel.importHistory(context, it) }
    }

    LaunchedEffect(uiState.message) {
        uiState.message?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessage()
        }
    }

    var showDeleteDialog by remember { mutableStateOf(false) }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Clear History?") },
            text = { Text("This will permanently delete all your logged workouts. This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteHistory()
                        showDeleteDialog = false
                    }
                ) {
                    Text("Clear All", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    HistoryScreenContent(
        uiState = uiState,
        snackbarHostState = snackbarHostState,
        onExport = { exportLauncher.launch("fitflow_history_${System.currentTimeMillis()}.json") },
        onImport = { importLauncher.launch(arrayOf("application/json")) },
        onClear = { showDeleteDialog = true },
        modifier = modifier
    )
}

@Composable
fun HistoryScreenContent(
    uiState: HistoryUiState,
    snackbarHostState: SnackbarHostState,
    onExport: () -> Unit,
    onImport: () -> Unit,
    onClear: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showMenu by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            FitFlowTopBar(
                title = "Workout History",
                subtitle = "Track your consistency and personal records",
                actions = {
                    Box {
                        IconButton(onClick = { showMenu = true }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "History Options")
                        }
                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Export History") },
                                onClick = {
                                    showMenu = false
                                    onExport()
                                },
                                leadingIcon = { Icon(Icons.Default.Upload, contentDescription = null) }
                            )
                            DropdownMenuItem(
                                text = { Text("Import History") },
                                onClick = {
                                    showMenu = false
                                    onImport()
                                },
                                leadingIcon = { Icon(Icons.Default.Download, contentDescription = null) }
                            )
                            HorizontalDivider()
                            DropdownMenuItem(
                                text = { Text("Clear All History", color = MaterialTheme.colorScheme.error) },
                                onClick = {
                                    showMenu = false
                                    onClear()
                                },
                                leadingIcon = { 
                                    Icon(
                                        Icons.Default.DeleteSweep, 
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.error
                                    ) 
                                }
                            )
                        }
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
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
        } else if (uiState.groupedByDate.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(20.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                EmptyStateCard(
                    title = "No Logged Workouts Yet",
                    description = "When you complete exercises on the Today screen, your logged sets, reps, and weights will show up here.",
                    icon = Icons.Default.History
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Aggregate Stats Cards Row
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        StatMetricCard(
                            label = "Sessions",
                            value = "${uiState.totalWorkoutsCount}",
                            icon = Icons.Default.EventNote,
                            modifier = Modifier.weight(1f)
                        )
                        StatMetricCard(
                            label = "Completed",
                            value = "${uiState.totalExercisesLogged}",
                            icon = Icons.Default.CheckCircle,
                            modifier = Modifier.weight(1f)
                        )
                        StatMetricCard(
                            label = "Volume",
                            value = "${uiState.totalVolumeKg.toInt()} kg",
                            icon = Icons.Default.Speed,
                            modifier = Modifier.weight(1.2f)
                        )
                    }
                }

                // Grouped Log Days
                items(
                    items = uiState.groupedByDate.entries.toList(),
                    key = { it.key }
                ) { entry ->
                    val dateString = entry.key
                    val logsForDay = entry.value

                    val formattedDate = try {
                        val parsed = LocalDate.parse(dateString, DateTimeFormatter.ISO_LOCAL_DATE)
                        parsed.format(DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy"))
                    } catch (e: Exception) {
                        dateString
                    }

                    WorkoutDateSessionCard(
                        formattedDate = formattedDate,
                        logs = logsForDay
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}

@Composable
fun StatMetricCard(
    label: String,
    value: String,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = EmeraldPrimary,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = label.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 10.sp
            )
        }
    }
}

@Composable
fun WorkoutDateSessionCard(
    formattedDate: String,
    logs: List<WorkoutLogWithExercise>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(18.dp)),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Date Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(EmeraldPrimary)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = formattedDate,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Text(
                    text = "${logs.size} movements",
                    style = MaterialTheme.typography.labelSmall,
                    color = EmeraldPrimary,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(10.dp))

            // Log details
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                logs.forEach { item ->
                    val weightFormatted = if (item.log.actualWeight % 1.0 == 0.0) {
                        "${item.log.actualWeight.toInt()}"
                    } else {
                        "${item.log.actualWeight}"
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = item.exercise.name,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                if (item.exercise.isSprint) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    SprintBadge(durationSeconds = item.log.actualDurationSeconds)
                                }
                            }
                            CategoryBadge(category = item.exercise.category)
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            if (item.exercise.isSprint) {
                                Text(
                                    text = "${item.log.actualDurationSeconds}s sprint",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            } else {
                                Text(
                                    text = "${item.log.actualSets} sets × ${item.log.actualReps} reps",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                if (item.log.actualWeight > 0) {
                                    Text(
                                        text = "$weightFormatted kg",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = CyanAccent
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HistoryScreenPreview() {
    FitFlowTheme {
        val sampleLogs = listOf(
            WorkoutLogWithExercise(
                log = WorkoutLogEntity(id = 1, date = "2026-08-22", exerciseId = 1, actualSets = 4, actualReps = 8, actualWeight = 80.0, isCompleted = true),
                exercise = ExerciseEntity(id = 1, name = "Barbell Bench Press", category = "Chest")
            ),
            WorkoutLogWithExercise(
                log = WorkoutLogEntity(id = 2, date = "2026-08-22", exerciseId = 2, actualSets = 3, actualReps = 10, actualWeight = 30.0, isCompleted = true),
                exercise = ExerciseEntity(id = 2, name = "Incline Dumbbell Press", category = "Chest")
            )
        )

        HistoryScreenContent(
            uiState = HistoryUiState(
                completedLogs = sampleLogs,
                totalWorkoutsCount = 1,
                totalExercisesLogged = 2,
                totalVolumeKg = 3460.0,
                groupedByDate = mapOf("2026-08-22" to sampleLogs),
                isLoading = false
            ),
            snackbarHostState = SnackbarHostState(),
            onExport = {},
            onImport = {},
            onClear = {}
        )
    }
}
