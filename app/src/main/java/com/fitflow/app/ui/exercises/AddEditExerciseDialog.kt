package com.fitflow.app.ui.exercises

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.fitflow.app.data.local.entity.ExerciseEntity
import com.fitflow.app.ui.components.CategoryFilterChip
import com.fitflow.app.ui.components.NumberStepper
import com.fitflow.app.ui.theme.CategorySprint
import com.fitflow.app.ui.theme.CrimsonAlert
import com.fitflow.app.ui.theme.CyanAccent
import com.fitflow.app.ui.theme.EmeraldPrimary

val DefaultCategories = listOf("Chest", "Back", "Legs", "Shoulders", "Arms", "Core", "Cardio", "Glutes", "Forearms")

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddEditExerciseDialog(
    exerciseToEdit: ExerciseEntity?,
    existingCategories: List<String> = emptyList(),
    onSave: (name: String, category: String, sets: Int, reps: Int, isSprint: Boolean, durationSeconds: Int) -> Unit,
    onDelete: (() -> Unit)? = null,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf(exerciseToEdit?.name ?: "") }
    var category by remember { mutableStateOf(exerciseToEdit?.category ?: "Chest") }
    var isSprint by remember { mutableStateOf(exerciseToEdit?.isSprint ?: false) }
    var defaultSets by remember { mutableIntStateOf(exerciseToEdit?.defaultSets ?: 3) }
    var defaultReps by remember { mutableIntStateOf(exerciseToEdit?.defaultReps ?: 10) }
    val initialDuration = exerciseToEdit?.defaultDurationSeconds ?: 30
    val initialIsMinutes = initialDuration >= 60 && initialDuration % 60 == 0
    var durationInputText by remember {
        mutableStateOf(
            if (initialIsMinutes) (initialDuration / 60).toString() else initialDuration.toString()
        )
    }
    var durationUnit by remember {
        mutableStateOf(if (initialIsMinutes) "Minutes" else "Seconds")
    }
    var durationUnitDropdownExpanded by remember { mutableStateOf(false) }
    var nameError by remember { mutableStateOf<String?>(null) }

    // Dynamic custom muscle group adding
    var isAddingCustomCategory by remember { mutableStateOf(false) }
    var customCategoryInput by remember { mutableStateOf("") }

    val allCategories = remember(existingCategories) {
        val set = linkedSetOf<String>()
        set.addAll(DefaultCategories)
        set.addAll(existingCategories.filter { it.isNotBlank() && it != "All" })
        if (exerciseToEdit != null && exerciseToEdit.category.isNotBlank()) {
            set.add(exerciseToEdit.category)
        }
        set.toList()
    }

    val dynamicCategoriesList = remember { mutableStateListOf(*allCategories.toTypedArray()) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = if (exerciseToEdit != null) "Edit Movement" else "New Movement",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Exercise Name
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        if (it.isNotBlank()) nameError = null
                    },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Exercise Name") },
                    placeholder = { Text(if (isSprint) "e.g., 100m Hill Sprint" else "e.g., Incline Dumbbell Press") },
                    singleLine = true,
                    isError = nameError != null,
                    supportingText = if (nameError != null) {
                        { Text(nameError!!, color = CrimsonAlert) }
                    } else null,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = if (isSprint) CategorySprint else EmeraldPrimary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Exercise Type Toggle: Standard (Sets/Reps) vs Sprint (Duration-Only)
                Text(
                    text = "EXERCISE TRACKING TYPE",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Standard Resistance / Sets & Reps
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (!isSprint) EmeraldPrimary.copy(alpha = 0.15f)
                                else MaterialTheme.colorScheme.surfaceVariant
                            )
                            .border(
                                width = 1.dp,
                                color = if (!isSprint) EmeraldPrimary else MaterialTheme.colorScheme.outlineVariant,
                                shape = RoundedCornerShape(12.dp)
                            )
                            .clickable { isSprint = false }
                            .padding(vertical = 10.dp, horizontal = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.FitnessCenter,
                                contentDescription = null,
                                tint = if (!isSprint) EmeraldPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Sets & Reps",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = if (!isSprint) FontWeight.Bold else FontWeight.Normal,
                                color = if (!isSprint) EmeraldPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Sprint / Timed
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (isSprint) CategorySprint.copy(alpha = 0.2f)
                                else MaterialTheme.colorScheme.surfaceVariant
                            )
                            .border(
                                width = 1.dp,
                                color = if (isSprint) CategorySprint else MaterialTheme.colorScheme.outlineVariant,
                                shape = RoundedCornerShape(12.dp)
                            )
                            .clickable {
                                isSprint = true
                                if (category == "Chest" || category == "Back" || category == "Arms") {
                                    category = "Cardio"
                                }
                            }
                            .padding(vertical = 10.dp, horizontal = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.ElectricBolt,
                                contentDescription = null,
                                tint = if (isSprint) CategorySprint else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Sprint (Time)",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = if (isSprint) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSprint) CategorySprint else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Target Muscle Group / Category Header + Add Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "MUSCLE GROUP / CATEGORY",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Bold
                    )

                    if (!isAddingCustomCategory) {
                        TextButton(
                            onClick = { isAddingCustomCategory = true },
                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 0.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = null,
                                tint = CyanAccent,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = "Add Muscle Group",
                                style = MaterialTheme.typography.labelSmall,
                                color = CyanAccent,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                // Inline custom muscle group input field
                AnimatedVisibility(visible = isAddingCustomCategory) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        OutlinedTextField(
                            value = customCategoryInput,
                            onValueChange = { customCategoryInput = it },
                            modifier = Modifier.weight(1f),
                            placeholder = { Text("e.g. Glutes, Traps, Calves...", fontSize = 12.sp) },
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = CyanAccent,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                            )
                        )

                        Button(
                            onClick = {
                                val trimmed = customCategoryInput.trim()
                                if (trimmed.isNotBlank()) {
                                    val capitalized = trimmed.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
                                    if (!dynamicCategoriesList.contains(capitalized)) {
                                        dynamicCategoriesList.add(capitalized)
                                    }
                                    category = capitalized
                                    customCategoryInput = ""
                                    isAddingCustomCategory = false
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = CyanAccent, contentColor = MaterialTheme.colorScheme.background),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Add", fontWeight = FontWeight.Bold)
                        }

                        IconButton(onClick = { isAddingCustomCategory = false }) {
                            Text("✕", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    dynamicCategoriesList.forEach { cat ->
                        CategoryFilterChip(
                            category = cat,
                            isSelected = category.equals(cat, ignoreCase = true),
                            onClick = { category = cat }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Conditional Inputs based on isSprint
                if (isSprint) {
                    // Sprint Duration Configuration
                    Text(
                        text = "DEFAULT SPRINT DURATION",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    // Rounds stepper for sprint exercises
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        NumberStepper(
                            label = "Default Rounds",
                            value = defaultSets,
                            onValueChange = { defaultSets = it },
                            minValue = 1,
                            maxValue = 30
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Duration Text Input
                        OutlinedTextField(
                            value = durationInputText,
                            onValueChange = { input ->
                                if (input.all { it.isDigit() }) {
                                    durationInputText = input
                                }
                            },
                            modifier = Modifier.weight(1f),
                            label = { Text("Duration per Round") },
                            placeholder = { Text("e.g., 30") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = CategorySprint,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        )

                        // Unit Dropdown Menu (Seconds / Minutes)
                        Box {
                            OutlinedButton(
                                onClick = { durationUnitDropdownExpanded = true },
                                shape = RoundedCornerShape(12.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 14.dp),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = MaterialTheme.colorScheme.onSurface
                                )
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        text = durationUnit,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Icon(
                                        imageVector = Icons.Default.ArrowDropDown,
                                        contentDescription = "Select duration unit",
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }

                            DropdownMenu(
                                expanded = durationUnitDropdownExpanded,
                                onDismissRequest = { durationUnitDropdownExpanded = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Seconds (s)") },
                                    onClick = {
                                        durationUnit = "Seconds"
                                        durationUnitDropdownExpanded = false
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Minutes (min)") },
                                    onClick = {
                                        durationUnit = "Minutes"
                                        durationUnitDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Preset chips for quick duration picking
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        listOf(15 to "Seconds", 30 to "Seconds", 45 to "Seconds", 60 to "Seconds", 90 to "Seconds", 2 to "Minutes").forEach { (presetVal, presetUnit) ->
                            val isSelected = durationUnit == presetUnit && durationInputText == presetVal.toString()
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        if (isSelected) CategorySprint.copy(alpha = 0.25f)
                                        else MaterialTheme.colorScheme.surfaceVariant
                                    )
                                    .border(
                                        width = 1.dp,
                                        color = if (isSelected) CategorySprint else MaterialTheme.colorScheme.outlineVariant,
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .clickable {
                                        durationInputText = presetVal.toString()
                                        durationUnit = presetUnit
                                    }
                                    .padding(horizontal = 10.dp, vertical = 5.dp)
                            ) {
                                Text(
                                    text = if (presetUnit == "Minutes") "${presetVal}m" else "${presetVal}s",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (isSelected) CategorySprint else MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }
                } else {
                    // Standard Sets & Reps
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        NumberStepper(
                            label = "Default Sets",
                            value = defaultSets,
                            onValueChange = { defaultSets = it },
                            minValue = 1,
                            maxValue = 20
                        )
                        NumberStepper(
                            label = "Default Reps",
                            value = defaultReps,
                            onValueChange = { defaultReps = it },
                            minValue = 1,
                            maxValue = 100
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    if (exerciseToEdit?.isCustom == true && onDelete != null) {
                        TextButton(
                            onClick = onDelete,
                            colors = ButtonDefaults.textButtonColors(contentColor = CrimsonAlert)
                        ) {
                            Icon(
                                imageVector = Icons.Default.DeleteOutline,
                                contentDescription = "Delete",
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Delete")
                        }
                    } else {
                        Spacer(modifier = Modifier.width(1.dp))
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = onDismiss) {
                            Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                if (name.trim().isBlank()) {
                                    nameError = "Name cannot be empty"
                                } else {
                                    val parsedDurationValue = durationInputText.toIntOrNull() ?: 30
                                    val calculatedDuration = if (durationUnit == "Minutes") {
                                        parsedDurationValue * 60
                                    } else {
                                        parsedDurationValue
                                    }.coerceAtLeast(1)

                                    onSave(
                                        name.trim(),
                                        category,
                                        if (isSprint) 1 else defaultSets,
                                        if (isSprint) 1 else defaultReps,
                                        isSprint,
                                        calculatedDuration
                                    )
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isSprint) CategorySprint else EmeraldPrimary,
                                contentColor = MaterialTheme.colorScheme.background
                            ),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text(
                                text = if (exerciseToEdit != null) "Update" else "Save Exercise",
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}
