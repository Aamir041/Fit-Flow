package com.fitflow.app.ui.templates

import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.automirrored.filled.Input
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
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
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
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
import com.fitflow.app.ui.theme.FitFlowTheme
import kotlinx.coroutines.flow.collectLatest

@Composable
fun TemplatesScreen(
    viewModel: TemplatesViewModel,
    onCreateTemplate: () -> Unit,
    onEditTemplate: (Long) -> Unit,
    onNavigateBack: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    var pendingExportJson by remember { mutableStateOf<String?>(null) }

    // Launcher for importing JSON file from device storage
    val importFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                val jsonString = context.contentResolver.openInputStream(uri)?.use { stream ->
                    stream.bufferedReader().use { it.readText() }
                }
                if (!jsonString.isNullOrBlank()) {
                    viewModel.importTemplateFromJson(jsonString)
                } else {
                    Toast.makeText(context, "Selected file was empty", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Failed to read file: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            }
        }
    }

    // Launcher for exporting all templates JSON to device storage file
    val exportAllFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri: Uri? ->
        if (uri != null && pendingExportJson != null) {
            try {
                context.contentResolver.openOutputStream(uri)?.use { stream ->
                    stream.bufferedWriter().use { it.write(pendingExportJson!!) }
                }
                Toast.makeText(context, "Templates exported successfully!", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(context, "Failed to save file: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            } finally {
                pendingExportJson = null
            }
        }
    }

    LaunchedEffect(uiState.infoMessage) {
        uiState.infoMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessages()
        }
    }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessages()
        }
    }

    LaunchedEffect(Unit) {
        viewModel.exportShareEvent.collectLatest { event ->
            pendingExportJson = event.jsonContent
            val timestamp = System.currentTimeMillis()
            exportAllFileLauncher.launch("${event.templateName}_$timestamp.json")
        }
    }

    TemplatesScreenContent(
        uiState = uiState,
        snackbarHostState = snackbarHostState,
        onCreateTemplate = onCreateTemplate,
        onEditTemplate = onEditTemplate,
        onDeleteTemplate = { viewModel.promptDelete(it) },
        onConfirmDelete = { viewModel.confirmDelete() },
        onDismissDelete = { viewModel.dismissDeletePrompt() },
        onExportAllTemplates = { viewModel.exportAllTemplates() },
        onImportFromFile = {
            importFileLauncher.launch("application/json")
        },
        onImportDirectJson = { json ->
            viewModel.importTemplateFromJson(json)
        },
        onNavigateBack = onNavigateBack,
        modifier = modifier
    )
}

@Composable
fun TemplatesScreenContent(
    uiState: TemplatesUiState,
    snackbarHostState: SnackbarHostState,
    onCreateTemplate: () -> Unit,
    onEditTemplate: (Long) -> Unit,
    onDeleteTemplate: (TemplateEntity) -> Unit,
    onConfirmDelete: () -> Unit,
    onDismissDelete: () -> Unit,
    onExportAllTemplates: () -> Unit,
    onImportFromFile: () -> Unit,
    onImportDirectJson: (String) -> Unit,
    onNavigateBack: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var topMenuExpanded by remember { mutableStateOf(false) }
    var isImportDialogOpen by remember { mutableStateOf(false) }
    var directJsonInput by remember { mutableStateOf("") }
    val clipboardManager = LocalClipboardManager.current

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            FitFlowTopBar(
                title = "Workout Templates",
                subtitle = "${uiState.templates.size} custom routines available",
                onBackClick = onNavigateBack,
                actions = {
                    Box {
                        IconButton(onClick = { topMenuExpanded = true }) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "More Options",
                                tint = MaterialTheme.colorScheme.onBackground
                            )
                        }
                        DropdownMenu(
                            expanded = topMenuExpanded,
                            onDismissRequest = { topMenuExpanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Export All Templates (JSON)") },
                                leadingIcon = {
                                    Icon(Icons.Default.FileDownload, contentDescription = null, modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.primary)
                                },
                                onClick = {
                                    topMenuExpanded = false
                                    onExportAllTemplates()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Import Templates File") },
                                leadingIcon = {
                                    Icon(Icons.Default.FileUpload, contentDescription = null, modifier = Modifier.size(18.dp))
                                },
                                onClick = {
                                    topMenuExpanded = false
                                    onImportFromFile()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Paste Template(s) JSON") },
                                leadingIcon = {
                                    Icon(Icons.AutoMirrored.Filled.Input, contentDescription = null, modifier = Modifier.size(18.dp))
                                },
                                onClick = {
                                    topMenuExpanded = false
                                    directJsonInput = clipboardManager.getText()?.text ?: ""
                                    isImportDialogOpen = true
                                }
                            )
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onCreateTemplate,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
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
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
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
                    description = "Templates let you design custom workout splits like Push Day, Pull Day, or Full Body. Create your first template or import one from JSON!",
                    icon = Icons.Default.Layers,
                    actionButtonText = "+ Create First Template",
                    onActionClick = onCreateTemplate
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedButton(
                    onClick = onImportFromFile,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.FileUpload,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Import Template from JSON")
                }
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

        // Import Paste JSON Dialog
        if (isImportDialogOpen) {
            AlertDialog(
                onDismissRequest = { isImportDialogOpen = false },
                title = {
                    Text(
                        text = "Import Template(s) JSON",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    Column {
                        Text(
                            text = "Paste the exported templates JSON below:",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = directJsonInput,
                            onValueChange = { directJsonInput = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp),
                            placeholder = { Text("{\n  \"templates\": [\n    {\n      \"templateName\": \"...\",\n      \"exercises\": [...]\n    }\n  ]\n}") },
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                            )
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val trimmed = directJsonInput.trim()
                            if (trimmed.isNotBlank()) {
                                isImportDialogOpen = false
                                onImportDirectJson(trimmed)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    ) {
                        Text("Import")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { isImportDialogOpen = false }) {
                        Text("Cancel")
                    }
                }
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
            // Top Row: Title, Exercise Count, and Action Buttons
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
                        color = MaterialTheme.colorScheme.primary,
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
                            tint = MaterialTheme.colorScheme.error,
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
            snackbarHostState = remember { SnackbarHostState() },
            onCreateTemplate = {},
            onEditTemplate = {},
            onDeleteTemplate = {},
            onConfirmDelete = {},
            onDismissDelete = {},
            onExportAllTemplates = {},
            onImportFromFile = {},
            onImportDirectJson = {}
        )
    }
}
