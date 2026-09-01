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
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.MoreVert
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
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.ui.text.style.TextAlign
import com.fitflow.app.data.local.entity.ExerciseEntity
import com.fitflow.app.data.local.entity.FoodLogEntity
import com.fitflow.app.data.local.entity.WorkoutLogEntity
import com.fitflow.app.data.local.relation.WorkoutLogWithExercise
import com.fitflow.app.ui.components.CategoryBadge
import com.fitflow.app.ui.components.EmptyStateCard
import com.fitflow.app.ui.components.FitFlowTopBar
import com.fitflow.app.ui.components.SprintBadge
import com.fitflow.app.ui.theme.CyanAccent
import com.fitflow.app.ui.theme.EmeraldDark
import com.fitflow.app.ui.theme.EmeraldLight
import com.fitflow.app.ui.theme.EmeraldPrimary
import com.fitflow.app.ui.theme.FitFlowTheme
import java.time.LocalDate
import java.time.YearMonth
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
    var selectedMonth by remember { mutableStateOf(YearMonth.now()) }
    var selectedDateForDetails by remember { mutableStateOf<String?>(null) }

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
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Monthly GitHub-style Activity Contribution Heatmap
                item {
                    MonthContributionHeatmap(
                        yearMonth = selectedMonth,
                        groupedByDate = uiState.groupedByDate,
                        foodLogsByDate = uiState.foodLogsByDate,
                        onPreviousMonth = { selectedMonth = selectedMonth.minusMonths(1) },
                        onNextMonth = { selectedMonth = selectedMonth.plusMonths(1) },
                        onDateSelected = { dateKey ->
                            selectedDateForDetails = dateKey
                        }
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }

        // Dialog showing history of exercises & foods on selected date
        selectedDateForDetails?.let { dateKey ->
            val logsForDay = uiState.groupedByDate[dateKey] ?: emptyList()
            val foodLogsForDay = uiState.foodLogsByDate[dateKey] ?: emptyList()
            DayWorkoutDetailsDialog(
                dateKey = dateKey,
                logs = logsForDay,
                foodLogs = foodLogsForDay,
                onDismiss = { selectedDateForDetails = null }
            )
        }
    }
}

@Composable
fun MonthContributionHeatmap(
    yearMonth: YearMonth,
    groupedByDate: Map<String, List<WorkoutLogWithExercise>>,
    foodLogsByDate: Map<String, List<FoodLogEntity>> = emptyMap(),
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onDateSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val monthTitle = yearMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy"))
    val daysInMonth = yearMonth.lengthOfMonth()
    val today = LocalDate.now()
    val isCurrentMonth = yearMonth == YearMonth.from(today)

    // Calculate active workout days count in this month
    val monthActiveDaysCount = (1..daysInMonth).count { day ->
        val dateKey = String.format("%04d-%02d-%02d", yearMonth.year, yearMonth.monthValue, day)
        groupedByDate.containsKey(dateKey) && groupedByDate[dateKey]?.isNotEmpty() == true
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Month Header with Prev/Next controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "CONSISTENCY HEATMAP",
                        style = MaterialTheme.typography.labelSmall,
                        color = EmeraldPrimary,
                        letterSpacing = 1.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = monthTitle,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onPreviousMonth,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ChevronLeft,
                            contentDescription = "Previous Month",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(
                        onClick = onNextMonth,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = "Next Month",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "$monthActiveDaysCount active ${if (monthActiveDaysCount == 1) "day" else "days"} • Tap any date to view details",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(14.dp))

            // GitHub style grid of squares (equal to days in month, 7 columns for days of week)
            val columns = 7
            val totalRows = (daysInMonth + columns - 1) / columns

            Column(
                verticalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                for (row in 0 until totalRows) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        for (col in 0 until columns) {
                            val dayNumber = row * columns + col + 1
                            if (dayNumber <= daysInMonth) {
                                val dateKey = String.format(
                                    "%04d-%02d-%02d",
                                    yearMonth.year,
                                    yearMonth.monthValue,
                                    dayNumber
                                )
                                val logsForDay = groupedByDate[dateKey]
                                val workoutCount = logsForDay?.size ?: 0

                                val isLit = workoutCount > 0
                                val isToday = isCurrentMonth && dayNumber == today.dayOfMonth

                                val squareBgColor = when {
                                    workoutCount >= 4 -> EmeraldPrimary
                                    workoutCount in 2..3 -> EmeraldLight
                                    workoutCount == 1 -> EmeraldLight.copy(alpha = 0.65f)
                                    else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                                }

                                val squareBorderColor = when {
                                    isToday -> CyanAccent
                                    isLit -> EmeraldPrimary.copy(alpha = 0.8f)
                                    else -> MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
                                }

                                val textColor = when {
                                    workoutCount >= 2 -> MaterialTheme.colorScheme.background
                                    isLit -> MaterialTheme.colorScheme.onSurface
                                    else -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                }

                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(38.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(squareBgColor)
                                        .border(
                                            width = if (isToday) 1.5.dp else 1.dp,
                                            color = squareBorderColor,
                                            shape = RoundedCornerShape(8.dp)
                                        )
                                        .clickable {
                                            onDateSelected(dateKey)
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = dayNumber.toString(),
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = if (isLit || isToday) FontWeight.Bold else FontWeight.Normal,
                                        color = textColor,
                                        fontSize = 11.sp,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            } else {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Heatmap Legend
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End
            ) {
                Text(
                    text = "Less",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 10.sp
                )
                Spacer(modifier = Modifier.width(4.dp))
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                        .border(0.5.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(2.dp))
                )
                Spacer(modifier = Modifier.width(3.dp))
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(EmeraldLight.copy(alpha = 0.65f))
                )
                Spacer(modifier = Modifier.width(3.dp))
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(EmeraldLight)
                )
                Spacer(modifier = Modifier.width(3.dp))
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(EmeraldPrimary)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "More",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 10.sp
                )
            }
        }
    }
}

@Composable
fun DayWorkoutDetailsDialog(
    dateKey: String,
    logs: List<WorkoutLogWithExercise>,
    foodLogs: List<FoodLogEntity> = emptyList(),
    onDismiss: () -> Unit
) {
    val formattedDate = try {
        val parsed = LocalDate.parse(dateKey, DateTimeFormatter.ISO_LOCAL_DATE)
        parsed.format(DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy"))
    } catch (e: Exception) {
        dateKey
    }

    val totalCalories = foodLogs.sumOf { it.calories }
    val totalWorkouts = logs.size
    val totalFoods = foodLogs.size
    val isEmpty = totalWorkouts == 0 && totalFoods == 0

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(
                    text = formattedDate,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = when {
                        isEmpty -> "Rest day / No logs recorded"
                        totalWorkouts > 0 && totalFoods > 0 -> "$totalWorkouts movements • $totalCalories kcal"
                        totalWorkouts > 0 -> "$totalWorkouts movements completed"
                        else -> "$totalFoods food items • $totalCalories kcal"
                    },
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isEmpty) MaterialTheme.colorScheme.onSurfaceVariant else EmeraldPrimary,
                    fontWeight = FontWeight.SemiBold
                )
            }
        },
        text = {
            if (isEmpty) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No workouts or food logs recorded for this day.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // 1. Workouts Section
                    if (logs.isNotEmpty()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "WORKOUTS (${logs.size})",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = EmeraldPrimary,
                                letterSpacing = 1.sp
                            )
                        }

                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            logs.forEach { item ->
                                val setsList = if (item.log.setsDataJson.isNotBlank()) {
                                    com.fitflow.app.data.local.model.WorkoutSetRecord.parseSetsFromJson(item.log.setsDataJson)
                                } else {
                                    emptyList()
                                }

                                val weightFormatted = if (item.log.actualWeight % 1.0 == 0.0) {
                                    "${item.log.actualWeight.toInt()}"
                                } else {
                                    "${item.log.actualWeight}"
                                }

                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                                        .padding(12.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = item.exercise.name,
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                CategoryBadge(category = item.exercise.category)
                                                if (item.exercise.isSprint) {
                                                    SprintBadge(durationSeconds = item.log.actualDurationSeconds)
                                                }
                                            }
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
                                                    text = "${item.log.actualSets} sets total",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )
                                                if (item.log.actualWeight > 0 && setsList.isEmpty()) {
                                                    Text(
                                                        text = "$weightFormatted kg",
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = CyanAccent
                                                    )
                                                }
                                            }
                                        }
                                    }

                                    // Display detailed sets or sprint rounds if available
                                    if (setsList.isNotEmpty()) {
                                        Spacer(modifier = Modifier.height(8.dp))
                                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                                        Spacer(modifier = Modifier.height(6.dp))

                                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                            setsList.forEach { s ->
                                                val isSprintItem = item.exercise.isSprint
                                                val setWeightFormatted = if (s.weight % 1.0 == 0.0) {
                                                    "${s.weight.toInt()}"
                                                } else {
                                                    "${s.weight}"
                                                }
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.SpaceBetween
                                                ) {
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        val prefix = if (isSprintItem) "Round" else "Set"
                                                        Text(
                                                            text = "$prefix ${s.setNumber}:",
                                                            style = MaterialTheme.typography.labelSmall,
                                                            fontWeight = FontWeight.Bold,
                                                            color = if (s.isCompleted) EmeraldLight else MaterialTheme.colorScheme.onSurfaceVariant
                                                        )
                                                        Spacer(modifier = Modifier.width(6.dp))
                                                        Text(
                                                            text = if (isSprintItem) "${s.reps}s sprint" else "${s.reps} reps",
                                                            style = MaterialTheme.typography.bodySmall,
                                                            color = MaterialTheme.colorScheme.onSurface
                                                        )
                                                    }
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        if (!isSprintItem && s.weight > 0) {
                                                            Text(
                                                                text = "$setWeightFormatted kg",
                                                                style = MaterialTheme.typography.labelSmall,
                                                                fontWeight = FontWeight.SemiBold,
                                                                color = CyanAccent
                                                            )
                                                            Spacer(modifier = Modifier.width(6.dp))
                                                        }
                                                        if (s.isCompleted) {
                                                            Text(
                                                                text = "✓",
                                                                style = MaterialTheme.typography.labelSmall,
                                                                color = EmeraldPrimary,
                                                                fontWeight = FontWeight.Bold
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
                    }

                    if (foodLogs.isNotEmpty()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "FOOD & NUTRITION (${foodLogs.size})",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = CyanAccent,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = "$totalCalories kcal",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.ExtraBold,
                                color = CyanAccent
                            )
                        }

                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            foodLogs.forEach { food ->
                                val qtyFormatted = if (food.quantity % 1.0 == 0.0) {
                                    food.quantity.toInt().toString()
                                } else {
                                    food.quantity.toString()
                                }

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = food.foodName,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = "$qtyFormatted ${food.unit} • ${food.mealTime}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }

                                    Text(
                                        text = "${food.calories} kcal",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = CyanAccent
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", color = EmeraldPrimary, fontWeight = FontWeight.Bold)
            }
        },
        shape = RoundedCornerShape(20.dp),
        containerColor = MaterialTheme.colorScheme.surface
    )
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
