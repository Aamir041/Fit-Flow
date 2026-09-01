package com.fitflow.app.ui.templates

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.FitnessCenter
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
import com.fitflow.app.ui.components.FitFlowTopBar
import com.fitflow.app.ui.components.NumberStepper
import com.fitflow.app.ui.components.SprintBadge
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
        uiState.generalError?.let { error ->
            snackbarHostState.showSnackbar(error)
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
        onUpdateExerciseValues = { index, sets, reps, rest ->
            viewModel.updateExerciseValues(index, sets, reps, rest)
        },
        onSprintValuesChanged = { index, rounds, duration, rest ->
            viewModel.updateSprintValues(index, rounds, duration, rest)
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
    onCreateCustomExercise: (name: String, category: String, sets: Int, reps: Int, isSprint: Boolean, durationSeconds: Int) -> Unit,
    onRemoveExercise: (Int) -> Unit,
    onMoveUp: (Int) -> Unit,
    onMoveDown: (Int) -> Unit,
    onUpdateExerciseValues: (index: Int, sets: Int, reps: Int, restSeconds: Int) -> Unit,
    onSprintValuesChanged: (index: Int, rounds: Int, durationSeconds: Int, restSeconds: Int) -> Unit,
    onSaveTemplate: () -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isEditMode = uiState.templateId > 0L
    val screenTitle = if (isEditMode) "Edit Routine Split" else "New Workout Routine"

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            FitFlowTopBar(
                title = screenTitle,
                subtitle = if (isEditMode) "Configure rounds, sets, reps & movements" else "Design a custom routine",
                onBackClick = onNavigateBack,
                actions = {
                    Button(
                        onClick = onSaveTemplate,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Save",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
        modifier = modifier.fillMaxSize()
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
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Template Name Input
                item {
                    OutlinedTextField(
                        value = uiState.templateName,
                        onValueChange = onNameChanged,
                        label = { Text("Template Name") },
                        placeholder = { Text("e.g., Push Day, HIIT Sprint, Leg Annihilation") },
                        singleLine = true,
                        isError = uiState.nameError != null,
                        supportingText = if (uiState.nameError != null) {
                            { Text(uiState.nameError, color = MaterialTheme.colorScheme.error) }
                        } else null,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Section Header: Exercises + Add Button
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "EXERCISES (${uiState.exercises.size})",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            letterSpacing = 1.sp
                        )

                        OutlinedButton(
                            onClick = onOpenPicker,
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Add Exercise",
                                color = MaterialTheme.colorScheme.primary,
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
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(56.dp)
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(MaterialTheme.colorScheme.surfaceVariant),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.FitnessCenter,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(28.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "No Exercises Added Yet",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Tap '+ Add Exercise' to pick movements from library",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                } else {
                    itemsIndexed(
                        items = uiState.exercises,
                        key = { index, item -> "${item.exerciseId}_$index" }
                    ) { index, item ->
                        EditableExerciseCard(
                            item = item,
                            index = index,
                            totalCount = uiState.exercises.size,
                            onValuesChanged = { sets, reps, rest ->
                                onUpdateExerciseValues(index, sets, reps, rest)
                            },
                            onSprintValuesChanged = { rounds, duration, rest ->
                                onSprintValuesChanged(index, rounds, duration, rest)
                            },
                            onMoveUp = { onMoveUp(index) },
                            onMoveDown = { onMoveDown(index) },
                            onRemove = { onRemoveExercise(index) }
                        )
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }

        // Exercise Picker Bottom Sheet
        if (uiState.isPickerOpen) {
            val alreadySelectedIds = uiState.exercises.map { it.exerciseId }.toSet()
            ExercisePickerBottomSheet(
                availableExercises = uiState.availableExercises,
                searchQuery = uiState.searchQuery,
                selectedCategory = uiState.selectedCategory,
                alreadySelectedExerciseIds = alreadySelectedIds,
                onSearchQueryChanged = onSearchQueryChanged,
                onCategorySelected = onCategoryFilterChanged,
                onExerciseSelected = onAddExercise,
                onCreateCustomExercise = onCreateCustomExercise,
                onDismiss = onClosePicker
            )
        }
    }
}

@Composable
fun EditableExerciseCard(
    item: EditableExerciseItem,
    index: Int,
    totalCount: Int,
    onValuesChanged: (sets: Int, reps: Int, restSeconds: Int) -> Unit,
    onSprintValuesChanged: (rounds: Int, durationSeconds: Int, restSeconds: Int) -> Unit,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(
                1.dp,
                if (item.isSprint) MaterialTheme.colorScheme.primary.copy(alpha = 0.4f) else MaterialTheme.colorScheme.outlineVariant,
                RoundedCornerShape(16.dp)
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Row 1: Exercise Index + Name + Category + Reorder & Remove Actions
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
                            .size(28.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${index + 1}",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Text(
                            text = item.name,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            CategoryBadge(category = item.category)
                            if (item.isSprint) {
                                SprintBadge(durationSeconds = item.targetDurationSeconds)
                            }
                        }
                    }
                }

                // Reorder and Delete Buttons
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
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(10.dp))

            // Row 2: Steppers — Sprint shows Rounds / Duration / Rest, Standard shows Sets / Reps / Rest
            if (item.isSprint) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    NumberStepper(
                        label = "Rounds",
                        value = item.targetSets,
                        onValueChange = { newRounds ->
                            onSprintValuesChanged(newRounds, item.targetDurationSeconds, item.restTimeSeconds)
                        },
                        minValue = 1,
                        maxValue = 30,
                        modifier = Modifier.weight(1f)
                    )

                    NumberStepper(
                        label = "Duration",
                        value = item.targetDurationSeconds,
                        onValueChange = { newDuration ->
                            onSprintValuesChanged(item.targetSets, newDuration, item.restTimeSeconds)
                        },
                        minValue = 5,
                        maxValue = 600,
                        step = 5,
                        suffix = "s",
                        modifier = Modifier.weight(1.1f)
                    )

                    NumberStepper(
                        label = "Rest",
                        value = item.restTimeSeconds,
                        onValueChange = { newRest ->
                            onSprintValuesChanged(item.targetSets, item.targetDurationSeconds, newRest)
                        },
                        minValue = 0,
                        maxValue = 600,
                        step = 15,
                        suffix = "s",
                        modifier = Modifier.weight(1.1f)
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
        val snackbarHostState = remember { SnackbarHostState() }
        TemplateDetailEditScreenContent(
            uiState = TemplateEditUiState(
                templateName = "Chest & Back Super Split",
                exercises = listOf(
                    EditableExerciseItem(exerciseId = 1, name = "Barbell Bench Press", category = "Chest", targetSets = 4, targetReps = 8, restTimeSeconds = 120),
                    EditableExerciseItem(exerciseId = 2, name = "Pull-ups", category = "Back", targetSets = 3, targetReps = 10, restTimeSeconds = 90)
                )
            ),
            snackbarHostState = snackbarHostState,
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
            onSprintValuesChanged = { _, _, _, _ -> },
            onSaveTemplate = {},
            onNavigateBack = {}
        )
    }
}
