package com.fitflow.app.ui.food

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Scale
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.fitflow.app.data.local.entity.FoodLogEntity

val StandardFoodUnits = listOf(
    Pair("g", "Grams (g)"),
    Pair("mg", "Milligrams (mg)"),
    Pair("kg", "Kilograms (kg)"),
    Pair("ml", "Millilitres (ml)"),
    Pair("l", "Litres (l)"),
    Pair("unit", "Units (unit)"),
    Pair("candy", "Candies (candy)"),
    Pair("piece", "Pieces (piece)"),
    Pair("serving", "Servings (serving)"),
    Pair("cup", "Cups (cup)"),
    Pair("tbsp", "Tablespoons (tbsp)"),
    Pair("tsp", "Teaspoons (tsp)"),
    Pair("custom", "Custom...")
)

val StandardMealTimes = listOf(
    "Breakfast",
    "Morning Snack",
    "Lunch",
    "Afternoon Snack",
    "Pre-Workout",
    "Post-Workout",
    "Dinner",
    "Evening Snack"
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddEditFoodDialog(
    foodToEdit: FoodLogEntity? = null,
    onSave: (foodName: String, quantity: Double, unit: String, calories: Int, mealTime: String) -> Unit,
    onDismiss: () -> Unit
) {
    var foodName by remember { mutableStateOf(foodToEdit?.foodName ?: "") }
    var quantityText by remember {
        mutableStateOf(
            if (foodToEdit != null) {
                if (foodToEdit.quantity % 1.0 == 0.0) foodToEdit.quantity.toInt().toString()
                else foodToEdit.quantity.toString()
            } else "100"
        )
    }
    var selectedUnit by remember {
        mutableStateOf(
            if (foodToEdit != null) {
                if (StandardFoodUnits.any { it.first == foodToEdit.unit }) foodToEdit.unit else "custom"
            } else "g"
        )
    }
    var customUnitText by remember {
        mutableStateOf(
            if (foodToEdit != null && StandardFoodUnits.none { it.first == foodToEdit.unit }) foodToEdit.unit else ""
        )
    }
    var caloriesText by remember { mutableStateOf(foodToEdit?.calories?.toString() ?: "") }
    var selectedMealTime by remember { mutableStateOf(foodToEdit?.mealTime ?: "Breakfast") }

    var isUnitDropdownExpanded by remember { mutableStateOf(false) }

    var nameError by remember { mutableStateOf(false) }
    var quantityError by remember { mutableStateOf(false) }
    var caloriesError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Restaurant,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = if (foodToEdit != null) "Edit Food Entry" else "Log Food Item",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Food Name Field
                OutlinedTextField(
                    value = foodName,
                    onValueChange = {
                        foodName = it
                        nameError = it.trim().isEmpty()
                    },
                    label = { Text("Food Name *") },
                    placeholder = { Text("e.g. Oatmeal, Banana, Candy, Chicken Breast") },
                    isError = nameError,
                    supportingText = if (nameError) {
                        { Text("Food name is required", color = MaterialTheme.colorScheme.error) }
                    } else null,
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        focusedLabelColor = MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                // Quantity and Unit Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    // Quantity input
                    OutlinedTextField(
                        value = quantityText,
                        onValueChange = {
                            quantityText = it
                            quantityError = it.toDoubleOrNull() == null || (it.toDoubleOrNull() ?: 0.0) <= 0.0
                        },
                        label = { Text("Quantity *") },
                        placeholder = { Text("100") },
                        isError = quantityError,
                        supportingText = if (quantityError) {
                            { Text("Invalid", color = MaterialTheme.colorScheme.error) }
                        } else null,
                        leadingIcon = {
                            Icon(Icons.Default.Scale, contentDescription = null, modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.primary)
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            focusedLabelColor = MaterialTheme.colorScheme.primary
                        ),
                        modifier = Modifier.weight(1.1f)
                    )

                    // Unit dropdown selector
                    Box(modifier = Modifier.weight(1.3f)) {
                        val displayUnit = StandardFoodUnits.firstOrNull { it.first == selectedUnit }?.second ?: selectedUnit
                        OutlinedTextField(
                            value = displayUnit,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Unit") },
                            trailingIcon = {
                                Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                focusedLabelColor = MaterialTheme.colorScheme.primary
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { isUnitDropdownExpanded = true }
                        )

                        // Clickable overlay to ensure entire box opens dropdown
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .clickable { isUnitDropdownExpanded = true }
                        )

                        DropdownMenu(
                            expanded = isUnitDropdownExpanded,
                            onDismissRequest = { isUnitDropdownExpanded = false }
                        ) {
                            StandardFoodUnits.forEach { (key, label) ->
                                DropdownMenuItem(
                                    text = { Text(label) },
                                    onClick = {
                                        selectedUnit = key
                                        isUnitDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                // If Custom unit is chosen, show a custom unit text field
                if (selectedUnit == "custom") {
                    OutlinedTextField(
                        value = customUnitText,
                        onValueChange = { customUnitText = it },
                        label = { Text("Custom Unit Name") },
                        placeholder = { Text("e.g. bar, bottle, bowl, slice") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            focusedLabelColor = MaterialTheme.colorScheme.primary
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Calories Field
                OutlinedTextField(
                    value = caloriesText,
                    onValueChange = {
                        caloriesText = it
                        caloriesError = it.toIntOrNull() == null || (it.toIntOrNull() ?: -1) < 0
                    },
                    label = { Text("Calories (kcal) *") },
                    placeholder = { Text("e.g. 250") },
                    isError = caloriesError,
                    supportingText = if (caloriesError) {
                        { Text("Please enter calories (>= 0)", color = MaterialTheme.colorScheme.error) }
                    } else null,
                    leadingIcon = {
                        Icon(Icons.Default.LocalFireDepartment, contentDescription = null, modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.primary)
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        focusedLabelColor = MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                // Meal / Time of Day Selector
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Time of Day / Meal",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        StandardMealTimes.forEach { meal ->
                            val isSelected = selectedMealTime == meal
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(
                                        if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                                    )
                                    .border(
                                        width = 1.dp,
                                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                                        shape = RoundedCornerShape(20.dp)
                                    )
                                    .clickable { selectedMealTime = meal }
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = meal,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val nameValid = foodName.trim().isNotEmpty()
                    val qty = quantityText.toDoubleOrNull()
                    val qtyValid = qty != null && qty > 0.0
                    val cal = caloriesText.toIntOrNull()
                    val calValid = cal != null && cal >= 0

                    nameError = !nameValid
                    quantityError = !qtyValid
                    caloriesError = !calValid

                    if (nameValid && qtyValid && calValid) {
                        val finalUnit = if (selectedUnit == "custom") {
                            customUnitText.trim().ifEmpty { "unit" }
                        } else {
                            selectedUnit
                        }
                        onSave(foodName.trim(), qty!!, finalUnit, cal!!, selectedMealTime)
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Text(if (foodToEdit != null) "Update" else "Log Food", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        },
        shape = RoundedCornerShape(24.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 6.dp
    )
}
