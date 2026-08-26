package com.fitflow.app.ui.exercises

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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ListAlt
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import com.fitflow.app.data.local.entity.ExerciseEntity
import com.fitflow.app.ui.components.CategoryBadge
import com.fitflow.app.ui.components.CategoryFilterChip
import com.fitflow.app.ui.components.ConfirmDeleteDialog
import com.fitflow.app.ui.components.EmptyStateCard
import com.fitflow.app.ui.components.FitFlowTopBar
import com.fitflow.app.ui.components.SprintBadge
import com.fitflow.app.ui.theme.CategorySprint
import com.fitflow.app.ui.theme.CrimsonAlert
import com.fitflow.app.ui.theme.CyanAccent
import com.fitflow.app.ui.theme.EmeraldPrimary
import com.fitflow.app.ui.theme.FitFlowTheme

@Composable
fun ExerciseLibraryScreen(
    viewModel: ExerciseLibraryViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    ExerciseLibraryScreenContent(
        uiState = uiState,
        onSearchQueryChanged = { viewModel.setSearchQuery(it) },
        onCategorySelected = { viewModel.setSelectedCategory(it) },
        onOpenAddDialog = { viewModel.openAddExerciseDialog() },
        onOpenEditDialog = { viewModel.openEditExerciseDialog(it) },
        onCloseDialog = { viewModel.closeDialog() },
        onSaveExercise = { name, cat, sets, reps, isSprint, duration ->
            viewModel.saveExercise(name, cat, sets, reps, isSprint, duration)
        },
        onPromptDelete = { viewModel.promptDelete(it) },
        onConfirmDelete = { viewModel.confirmDelete() },
        onDismissDelete = { viewModel.dismissDeletePrompt() },
        modifier = modifier
    )
}

@Composable
fun ExerciseLibraryScreenContent(
    uiState: ExerciseLibraryUiState,
    onSearchQueryChanged: (String) -> Unit,
    onCategorySelected: (String) -> Unit,
    onOpenAddDialog: () -> Unit,
    onOpenEditDialog: (ExerciseEntity) -> Unit,
    onCloseDialog: () -> Unit,
    onSaveExercise: (name: String, category: String, sets: Int, reps: Int, isSprint: Boolean, durationSeconds: Int) -> Unit,
    onPromptDelete: (ExerciseEntity) -> Unit,
    onConfirmDelete: () -> Unit,
    onDismissDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            FitFlowTopBar(
                title = "Exercise Library",
                subtitle = "${uiState.totalCount} total movements"
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onOpenAddDialog,
                containerColor = EmeraldPrimary,
                contentColor = MaterialTheme.colorScheme.background,
                shape = CircleShape
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add Custom Movement")
            }
        },
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Search Input
            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = onSearchQueryChanged,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                placeholder = { Text("Search exercises (e.g. Squat, Sprint)...") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                trailingIcon = {
                    if (uiState.searchQuery.isNotEmpty()) {
                        IconButton(onClick = { onSearchQueryChanged("") }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Clear search",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = EmeraldPrimary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface
                )
            )

            // Category Chips Row (Dynamic Categories from DB + Default Categories)
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(uiState.availableCategories) { category ->
                    CategoryFilterChip(
                        category = category,
                        isSelected = category.equals(uiState.selectedCategory, ignoreCase = true),
                        onClick = { onCategorySelected(category) }
                    )
                }
            }

            // Exercise List
            if (uiState.isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = EmeraldPrimary)
                }
            } else if (uiState.exercises.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    EmptyStateCard(
                        title = "No Movements Found",
                        description = "No movements matched '${uiState.searchQuery}'. Tap below to create a custom exercise or sprint.",
                        icon = Icons.Default.ListAlt,
                        actionButtonText = "+ Add Movement / Sprint",
                        onActionClick = onOpenAddDialog
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(
                        items = uiState.exercises,
                        key = { it.id }
                    ) { exercise ->
                        ExerciseCatalogItemCard(
                            exercise = exercise,
                            onEdit = { onOpenEditDialog(exercise) }
                        )
                    }

                    item {
                        Spacer(modifier = Modifier.height(72.dp)) // Clearance for FAB
                    }
                }
            }
        }

        // Add / Edit Dialog
        if (uiState.isAddDialogOpen) {
            AddEditExerciseDialog(
                exerciseToEdit = uiState.exerciseToEdit,
                existingCategories = uiState.availableCategories,
                onSave = onSaveExercise,
                onDelete = if (uiState.exerciseToEdit != null && uiState.exerciseToEdit.isCustom) {
                    { onPromptDelete(uiState.exerciseToEdit) }
                } else null,
                onDismiss = onCloseDialog
            )
        }

        // Delete Dialog
        if (uiState.exerciseToDelete != null) {
            ConfirmDeleteDialog(
                title = "Delete Exercise?",
                message = "Are you sure you want to delete '${uiState.exerciseToDelete.name}'?",
                onConfirm = onConfirmDelete,
                onDismiss = onDismissDelete
            )
        }
    }
}

@Composable
fun ExerciseCatalogItemCard(
    exercise: ExerciseEntity,
    onEdit: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(
                1.dp,
                if (exercise.isSprint) CategorySprint.copy(alpha = 0.4f) else MaterialTheme.colorScheme.outlineVariant,
                RoundedCornerShape(16.dp)
            )
            .clickable(onClick = onEdit),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = exercise.name,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (exercise.isSprint) {
                        Spacer(modifier = Modifier.width(6.dp))
                        SprintBadge(durationSeconds = exercise.defaultDurationSeconds)
                    }
                    if (exercise.isCustom) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(CyanAccent.copy(alpha = 0.15f))
                                .padding(horizontal = 5.dp, vertical = 1.dp)
                        ) {
                            Text(
                                text = "CUSTOM",
                                style = MaterialTheme.typography.labelSmall,
                                color = CyanAccent,
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    CategoryBadge(category = exercise.category)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (exercise.isSprint) {
                            "Duration: ${exercise.defaultDurationSeconds}s"
                        } else {
                            "Defaults: ${exercise.defaultSets} sets × ${exercise.defaultReps} reps"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            IconButton(onClick = onEdit) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Edit",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ExerciseLibraryScreenPreview() {
    FitFlowTheme {
        val sampleExercises = listOf(
            ExerciseEntity(1, "Barbell Bench Press", "Chest", 4, 8, false),
            ExerciseEntity(2, "100m Track Sprint", "Cardio", 1, 1, false, isSprint = true, defaultDurationSeconds = 30),
            ExerciseEntity(3, "Barbell Back Squat", "Legs", 4, 8, false),
            ExerciseEntity(4, "Glute Kickbacks", "Glutes", 3, 15, true)
        )

        ExerciseLibraryScreenContent(
            uiState = ExerciseLibraryUiState(
                exercises = sampleExercises,
                totalCount = 4,
                isLoading = false
            ),
            onSearchQueryChanged = {},
            onCategorySelected = {},
            onOpenAddDialog = {},
            onOpenEditDialog = {},
            onCloseDialog = {},
            onSaveExercise = { _, _, _, _, _, _ -> },
            onPromptDelete = {},
            onConfirmDelete = {},
            onDismissDelete = {}
        )
    }
}
