package com.fitflow.app.ui.templates

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import com.fitflow.app.data.local.entity.ExerciseEntity
import com.fitflow.app.data.local.entity.TemplateEntity
import com.fitflow.app.data.local.entity.TemplateExerciseEntity
import com.fitflow.app.data.local.relation.TemplateExerciseWithDetail
import com.fitflow.app.data.local.relation.TemplateWithExercises
import com.fitflow.app.ui.components.CategoryBadge
import com.fitflow.app.ui.components.ConfirmDeleteDialog
import com.fitflow.app.ui.components.EmptyStateCard
import com.fitflow.app.ui.components.FitFlowTopBar
import com.fitflow.app.ui.theme.CrimsonAlert
import com.fitflow.app.ui.theme.EmeraldPrimary
import com.fitflow.app.ui.theme.FitFlowTheme

@Composable
fun TemplatesScreen(
    viewModel: TemplatesViewModel,
    onCreateTemplate: () -> Unit,
    onEditTemplate: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    TemplatesScreenContent(
        uiState = uiState,
        onCreateTemplate = onCreateTemplate,
        onEditTemplate = onEditTemplate,
        onDeleteTemplate = { viewModel.promptDelete(it) },
        onConfirmDelete = { viewModel.confirmDelete() },
        onDismissDelete = { viewModel.dismissDeletePrompt() },
        modifier = modifier
    )
}

@Composable
fun TemplatesScreenContent(
    uiState: TemplatesUiState,
    onCreateTemplate: () -> Unit,
    onEditTemplate: (Long) -> Unit,
    onDeleteTemplate: (TemplateEntity) -> Unit,
    onConfirmDelete: () -> Unit,
    onDismissDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            FitFlowTopBar(
                title = "Workout Templates",
                subtitle = "${uiState.templates.size} saved templates"
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onCreateTemplate,
                containerColor = EmeraldPrimary,
                contentColor = MaterialTheme.colorScheme.background,
                shape = CircleShape
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Create Template")
            }
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
        } else if (uiState.templates.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(20.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                EmptyStateCard(
                    title = "No Templates Created Yet",
                    description = "Templates let you design custom workout splits like Push Day, Pull Day, or Full Body. Create your first template now!",
                    icon = Icons.Default.Layers,
                    actionButtonText = "+ Create First Template",
                    onActionClick = onCreateTemplate
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
                items(
                    items = uiState.templates,
                    key = { it.template.id }
                ) { item ->
                    TemplateItemCard(
                        templateWithExercises = item,
                        onClick = { onEditTemplate(item.template.id) },
                        onDeleteClick = { onDeleteTemplate(item.template) }
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(72.dp)) // Clearance for FAB
                }
            }
        }

        // Delete Dialog
        if (uiState.templateToDelete != null) {
            ConfirmDeleteDialog(
                title = "Delete Template?",
                message = "Are you sure you want to delete '${uiState.templateToDelete.name}'? This will remove its exercise configuration.",
                onConfirm = onConfirmDelete,
                onDismiss = onDismissDelete
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TemplateItemCard(
    templateWithExercises: TemplateWithExercises,
    onClick: () -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val template = templateWithExercises.template
    val exercises = templateWithExercises.exercises.sortedBy { it.templateExercise.orderIndex }
    val categories = exercises.map { it.exercise.category }.distinct()

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(18.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Top Row: Title, Exercise Count, and Delete Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = template.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${exercises.size} exercises",
                        style = MaterialTheme.typography.bodySmall,
                        color = EmeraldPrimary,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onClick) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit template",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    IconButton(onClick = onDeleteClick) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = "Delete template",
                            tint = CrimsonAlert,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            // Categories flow
            if (categories.isNotEmpty()) {
                Spacer(modifier = Modifier.height(10.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    categories.forEach { cat ->
                        CategoryBadge(category = cat)
                    }
                }
            }

            // Exercises Summary preview
            if (exercises.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                Spacer(modifier = Modifier.height(10.dp))

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    exercises.take(3).forEachIndexed { index, item ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${index + 1}. ${item.exercise.name}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1
                            )
                            Text(
                                text = "${item.templateExercise.targetSets} × ${item.templateExercise.targetReps}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    if (exercises.size > 3) {
                        Text(
                            text = "+ ${exercises.size - 3} more exercises",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun TemplatesScreenPreview() {
    FitFlowTheme {
        val sampleTemplate = TemplateWithExercises(
            template = TemplateEntity(id = 1, name = "Push Day"),
            exercises = listOf(
                TemplateExerciseWithDetail(
                    templateExercise = TemplateExerciseEntity(
                        id = 1, templateId = 1, exerciseId = 1, targetSets = 4, targetReps = 8, restTimeSeconds = 90, orderIndex = 0
                    ),
                    exercise = ExerciseEntity(id = 1, name = "Barbell Bench Press", category = "Chest")
                ),
                TemplateExerciseWithDetail(
                    templateExercise = TemplateExerciseEntity(
                        id = 2, templateId = 1, exerciseId = 2, targetSets = 3, targetReps = 10, restTimeSeconds = 60, orderIndex = 1
                    ),
                    exercise = ExerciseEntity(id = 2, name = "Overhead Barbell Press", category = "Shoulders")
                )
            )
        )

        TemplatesScreenContent(
            uiState = TemplatesUiState(
                templates = listOf(sampleTemplate),
                isLoading = false
            ),
            onCreateTemplate = {},
            onEditTemplate = {},
            onDeleteTemplate = {},
            onConfirmDelete = {},
            onDismissDelete = {}
        )
    }
}
