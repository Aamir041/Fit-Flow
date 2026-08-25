package com.fitflow.app.ui.templates

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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fitflow.app.data.local.entity.ExerciseEntity
import com.fitflow.app.ui.components.CategoryBadge
import com.fitflow.app.ui.components.SprintBadge
import com.fitflow.app.ui.components.FitFlowTopBar
import com.fitflow.app.ui.components.NumberStepper
import com.fitflow.app.ui.theme.CrimsonAlert
import com.fitflow.app.ui.theme.EmeraldPrimary
import com.fitflow.app.ui.theme.FitFlowTheme
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TemplateDetailEditScreen(
    viewModel: TemplateEditViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.saveSuccessEvent.collectLatest { success ->
            if (success) {
                onNavigateBack()
            }
        }
    }

    LaunchedEffect(uiState.generalError) {
        uiState.generalError?.let { msg ->
            snackbarHostState.showSnackbar(msg)
        }
    }

    TemplateDetailEditScreenContent(
        uiState = uiState,
        snackbarHostState = snackbarHostState,
        onNameChanged = { viewModel.onNameChange(it) },
        onOpenPicker = { viewModel.openExercisePicker() },
        onClosePicker = { viewModel.closeExercisePicker() },
        onCategoryFilterChanged = { viewModel.setCategoryFilter(it) },
        onSearchQueryChanged = { viewModel.setSearchQuery(it) },
        onAddExercise = { viewModel.addExercise(it) },
        onCreateCustomExercise = { name, cat, sets, reps, isSprint, duration ->
            viewModel.createAndAddCustomExercise(name, cat, sets, reps, isSprint, duration)
        },
        onRemoveExercise = { viewModel.removeExercise(it) },
        onMoveUp = { viewModel.moveExerciseUp(it) },
        onMoveDown = { viewModel.moveExerciseDown(it) },
        onUpdateExerciseValues = { idx, sets, reps, rest ->
            viewModel.updateExerciseValues(idx, sets, reps, rest)
        },
        onUpdateSprintDuration = { idx, duration ->
            viewModel.updateSprintDuration(idx, duration)
        },
        onSaveTemplate = { viewModel.saveTemplate() },
        onNavigateBack = onNavigateBack,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TemplateDetailEditScreenContent(
    uiState: TemplateEditUiState,
    snackbarHostState: SnackbarHostState,
    onNameChanged: (String) -> Unit,
    onOpenPicker: () -> Unit,
    onClosePicker: () -> Unit,
    onCategoryFilterChanged: (String) -> Unit,
    onSearchQueryChanged: (String) -> Unit,
    onAddExercise: (ExerciseEntity) -> Unit,
    onCreateCustomExercise: (String, String, Int, Int, Boolean, Int) -> Unit,
    onRemoveExercise: (Int) -> Unit,
    onMoveUp: (Int) -> Unit,
    onMoveDown: (Int) -> Unit,
    onUpdateExerciseValues: (Int, Int, Int, Int) -> Unit,
    onUpdateSprintDuration: (Int, Int) -> Unit = { _, _ -> },
    onSaveTemplate: () -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isEditMode = uiState.templateId > 0L
    val screenTitle = if (isEditMode) "Edit Template" else "New Template"

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            FitFlowTopBar(
                title = screenTitle,
                onBackClick = onNavigateBack,
                actions = {
                    Button(
                        onClick = onSaveTemplate,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = EmeraldPrimary,
                            contentColor = MaterialTheme.colorScheme.background
                        ),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Save",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "Save", fontWeight = FontWeight.Bold)
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
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Template Name Input
                item {
                    Column {
                        Text(
                            text = "TEMPLATE NAME",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 6.dp)
                        )
                        OutlinedTextField(
                            value = uiState.templateName,
                            onValueChange = onNameChanged,
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("e.g., Push Day, Leg Power, Upper Body") },
                            singleLine = true,
                            isError = uiState.nameError != null,
                            supportingText = if (uiState.nameError != null) {
                                { Text(uiState.nameError, color = CrimsonAlert) }
                            } else null,
                            shape = RoundedCornerShape(14.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = EmeraldPrimary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                                focusedContainerColor = MaterialTheme.colorScheme.surface,
                                unfocusedContainerColor = MaterialTheme.colorScheme.surface
                            )
                        )
                    }
                }

                // Exercises Header + Add Button
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "EXERCISES (${uiState.exercises.size})",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Customize sets, reps, rest & order",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                        }

                        OutlinedButton(
                            onClick = onOpenPicker,
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = null,
                                tint = EmeraldPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Add Exercise",
                                color = EmeraldPrimary,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.labelMedium
                            )
                        }
                    }
                }

                // Exercise List
                if (uiState.exercises.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(MaterialTheme.colorScheme.surface)
                                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(16.dp))
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "No exercises added yet",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Tap '+ Add Exercise' to pick from the library",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                } else {
                    itemsIndexed(
                        items = uiState.exercises,
                        key = { _, item -> item.exerciseId }
                    ) { index, item ->
                        ConfigurableExerciseCard(
                            item = item,
                            index = index,
                            totalCount = uiState.exercises.size,
                            onMoveUp = { onMoveUp(index) },
                            onMoveDown = { onMoveDown(index) },
                            onRemove = { onRemoveExercise(index) },
                            onValuesChanged = { sets, reps, rest ->
                                onUpdateExerciseValues(index, sets, reps, rest)
                            },
                            onDurationChanged = { duration ->
                                onUpdateSprintDuration(index, duration)
                            }
                        )
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }

        // Bottom Sheet Picker
        if (uiState.isPickerOpen) {
            val selectedIds = remember(uiState.exercises) {
                uiState.exercises.map { it.exerciseId }.toSet()
            }

            ExercisePickerBottomSheet(
                availableExercises = uiState.availableExercises,
                alreadySelectedExerciseIds = selectedIds,
                selectedCategory = uiState.selectedCategory,
                searchQuery = uiState.searchQuery,
                onCategorySelected = onCategoryFilterChanged,
                onSearchQueryChanged = onSearchQueryChanged,
                onExerciseSelected = onAddExercise,
                onCreateCustomExercise = { name, cat, sets, reps, isSprint, duration ->
                    onCreateCustomExercise(name, cat, sets, reps, isSprint, duration)
                },
                onDismiss = onClosePicker
            )
        }
    }
}

@Composable
fun ConfigurableExerciseCard(
    item: EditableExerciseItem,
    index: Int,
    totalCount: Int,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    onRemove: () -> Unit,
    onValuesChanged: (sets: Int, reps: Int, restSeconds: Int) -> Unit,
    onDurationChanged: (Int) -> Unit = {},
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Row 1: Exercise Index + Name + Category + Up/Down/Delete Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${index + 1}",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = item.name,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            if (item.isSprint) {
                                Spacer(modifier = Modifier.width(6.dp))
                                SprintBadge()
                            }
                        }
                        CategoryBadge(category = item.category)
                    }
                }

                // Reorder controls + Delete
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onMoveUp,
                        enabled = index > 0,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowUpward,
                            contentDescription = "Move up",
                            tint = if (index > 0) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.outlineVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    IconButton(
                        onClick = onMoveDown,
                        enabled = index < totalCount - 1,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowDownward,
                            contentDescription = "Move down",
                            tint = if (index < totalCount - 1) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.outlineVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    IconButton(
                        onClick = onRemove,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = "Remove",
                            tint = CrimsonAlert,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(10.dp))

            // Row 2: Steppers — Sprint shows only Duration, Standard shows Sets/Reps/Rest
            if (item.isSprint) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    NumberStepper(
                        label = "Duration",
                        value = item.targetDurationSeconds,
                        onValueChange = { onDurationChanged(it) },
                        minValue = 5,
                        maxValue = 600,
                        step = 5,
                        suffix = "s",
                        modifier = Modifier.weight(1f)
                    )
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    NumberStepper(
                        label = "Sets",
                        value = item.targetSets,
                        onValueChange = { newSets ->
                            onValuesChanged(newSets, item.targetReps, item.restTimeSeconds)
                        },
                        minValue = 1,
                        maxValue = 20,
                        modifier = Modifier.weight(1f)
                    )

                    NumberStepper(
                        label = "Reps",
                        value = item.targetReps,
                        onValueChange = { newReps ->
                            onValuesChanged(item.targetSets, newReps, item.restTimeSeconds)
                        },
                        minValue = 1,
                        maxValue = 100,
                        modifier = Modifier.weight(1f)
                    )

                    NumberStepper(
                        label = "Rest",
                        value = item.restTimeSeconds,
                        onValueChange = { newRest ->
                            onValuesChanged(item.targetSets, item.targetReps, newRest)
                        },
                        minValue = 0,
                        maxValue = 600,
                        step = 15,
                        suffix = "s",
                        modifier = Modifier.weight(1.2f)
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun TemplateDetailEditPreview() {
    FitFlowTheme {
        TemplateDetailEditScreenContent(
            uiState = TemplateEditUiState(
                templateName = "Chest & Back Super Split",
                exercises = listOf(
                    EditableExerciseItem(exerciseId = 1, name = "Barbell Bench Press", category = "Chest", targetSets = 4, targetReps = 8, restTimeSeconds = 120),
                    EditableExerciseItem(exerciseId = 2, name = "Pull-ups", category = "Back", targetSets = 3, targetReps = 10, restTimeSeconds = 90)
                )
            ),
            snackbarHostState = remember { SnackbarHostState() },
            onNameChanged = {},
            onOpenPicker = {},
            onClosePicker = {},
            onCategoryFilterChanged = {},
            onSearchQueryChanged = {},
            onAddExercise = {},
            onCreateCustomExercise = { _, _, _, _, _, _ -> },
            onRemoveExercise = {},
            onMoveUp = {},
            onMoveDown = {},
            onUpdateExerciseValues = { _, _, _, _ -> },
            onSaveTemplate = {},
            onNavigateBack = {}
        )
    }
}
