package com.fitflow.app.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.fitflow.app.ui.components.CategoryBadge
import com.fitflow.app.ui.components.DecimalStepper
import com.fitflow.app.ui.components.NumberStepper
import com.fitflow.app.ui.components.SprintBadge
import com.fitflow.app.ui.theme.CyanAccent
import com.fitflow.app.ui.theme.EmeraldLight
import com.fitflow.app.ui.theme.EmeraldPrimary

@Composable
fun LogSetsDialog(
    item: ExerciseLogItem,
    onToggleSet: (setNumber: Int) -> Unit,
    onUpdateSetValues: (setNumber: Int, reps: Int, weight: Double) -> Unit,
    onAddSet: () -> Unit,
    onRemoveSet: (setNumber: Int) -> Unit,
    onDurationChanged: (durationSeconds: Int) -> Unit,
    onOpenTimer: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = modifier
                .fillMaxWidth(0.95f)
                .heightIn(max = 680.dp)
                .clip(RoundedCornerShape(24.dp))
                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(24.dp)),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Top header: Exercise Name + Close button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            CategoryBadge(category = item.category)
                            if (item.isSprint) {
                                SprintBadge(durationSeconds = item.targetDurationSeconds)
                            }
                            if (item.restTimeSeconds > 0) {
                                Row(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(CyanAccent.copy(alpha = 0.12f))
                                        .clickable { onOpenTimer() }
                                        .padding(horizontal = 6.dp, vertical = 3.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Timer,
                                        contentDescription = "Rest timer",
                                        tint = CyanAccent,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(
                                        text = "${item.restTimeSeconds}s",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = CyanAccent
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = item.name,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Text(
                            text = if (item.isSprint) {
                                "Target: ${item.targetSets} rounds × ${item.targetDurationSeconds}s sprint • ${item.completedSetsCount} of ${item.totalSetsCount} completed"
                            } else {
                                "Target: ${item.targetSets} sets × ${item.targetReps} reps • ${item.completedSetsCount} of ${item.totalSetsCount} completed"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                Spacer(modifier = Modifier.height(14.dp))

                // Table Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (item.isSprint) "ROUND" else "SET",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.width(54.dp),
                        textAlign = TextAlign.Center
                    )
                    if (item.isSprint) {
                        Text(
                            text = "DURATION",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.Center
                        )
                    } else {
                        Text(
                            text = "REPETITIONS",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "WEIGHT",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1.1f),
                            textAlign = TextAlign.Center
                        )
                    }
                    Text(
                        text = "DONE",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.width(48.dp),
                        textAlign = TextAlign.Center
                    )
                }

                // Sets / Rounds List
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = false),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(
                        items = item.sets,
                        key = { it.setNumber }
                    ) { setModel ->
                        if (item.isSprint) {
                            SprintRoundRowItem(
                                setModel = setModel,
                                defaultDuration = item.targetDurationSeconds,
                                onToggleDone = { onToggleSet(setModel.setNumber) },
                                onDurationChanged = { newSecs ->
                                    onUpdateSetValues(setModel.setNumber, newSecs, 0.0)
                                },
                                onRemoveRound = if (item.sets.size > 1) {
                                    { onRemoveSet(setModel.setNumber) }
                                } else null
                            )
                        } else {
                            SetRowItem(
                                setModel = setModel,
                                onToggleDone = { onToggleSet(setModel.setNumber) },
                                onRepsChanged = { newReps ->
                                    onUpdateSetValues(setModel.setNumber, newReps, setModel.weight)
                                },
                                onWeightChanged = { newWeight ->
                                    onUpdateSetValues(setModel.setNumber, setModel.reps, newWeight)
                                },
                                onRemoveSet = if (item.sets.size > 1) {
                                    { onRemoveSet(setModel.setNumber) }
                                } else null
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Add Set / Add Round button
                OutlinedButton(
                    onClick = onAddSet,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(40.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = EmeraldPrimary
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = EmeraldPrimary
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (item.isSprint) "Add Round" else "Add Set",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Bottom Done Button
                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = EmeraldPrimary,
                        contentColor = MaterialTheme.colorScheme.background
                    )
                ) {
                    Text(
                        text = "Done Logging",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}

@Composable
fun SetRowItem(
    setModel: WorkoutSetUiModel,
    onToggleDone: () -> Unit,
    onRepsChanged: (Int) -> Unit,
    onWeightChanged: (Double) -> Unit,
    onRemoveSet: (() -> Unit)?,
    modifier: Modifier = Modifier
) {
    val isDone = setModel.isCompleted
    val rowBg = if (isDone) {
        EmeraldPrimary.copy(alpha = 0.12f)
    } else {
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    }
    val rowBorder = if (isDone) {
        EmeraldPrimary.copy(alpha = 0.5f)
    } else {
        MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(rowBg)
            .border(1.dp, rowBorder, RoundedCornerShape(14.dp))
            .padding(horizontal = 8.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Set Number Badge
        Box(
            modifier = Modifier
                .width(36.dp)
                .height(32.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(
                    if (isDone) EmeraldPrimary.copy(alpha = 0.2f)
                    else MaterialTheme.colorScheme.surfaceVariant
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "${setModel.setNumber}",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = if (isDone) EmeraldPrimary else MaterialTheme.colorScheme.onSurface
            )
        }

        // Reps Stepper
        Box(
            modifier = Modifier.weight(1f),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surface)
                        .clickable(enabled = setModel.reps > 1) {
                            onRepsChanged((setModel.reps - 1).coerceAtLeast(1))
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "–",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Text(
                    text = "${setModel.reps}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.width(28.dp),
                    textAlign = TextAlign.Center
                )

                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surface)
                        .clickable(enabled = setModel.reps < 99) {
                            onRepsChanged((setModel.reps + 1).coerceAtMost(99))
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "+",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        // Weight Stepper
        Box(
            modifier = Modifier.weight(1.1f),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surface)
                        .clickable(enabled = setModel.weight > 0.0) {
                            val next = (setModel.weight - 2.5).coerceAtLeast(0.0)
                            onWeightChanged((next * 10).toInt() / 10.0)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "–",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                val weightFormatted = if (setModel.weight % 1.0 == 0.0) {
                    "${setModel.weight.toInt()}"
                } else {
                    "${setModel.weight}"
                }

                Text(
                    text = "$weightFormatted kg",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = CyanAccent,
                    modifier = Modifier.width(52.dp),
                    textAlign = TextAlign.Center,
                    fontSize = 12.sp
                )

                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surface)
                        .clickable(enabled = setModel.weight < 500.0) {
                            val next = (setModel.weight + 2.5).coerceAtMost(500.0)
                            onWeightChanged((next * 10).toInt() / 10.0)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "+",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        // Checkbox button
        Box(
            modifier = Modifier.width(44.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(30.dp)
                    .clip(CircleShape)
                    .background(
                        if (isDone) EmeraldPrimary else MaterialTheme.colorScheme.surface
                    )
                    .border(
                        1.dp,
                        if (isDone) EmeraldPrimary else MaterialTheme.colorScheme.outlineVariant,
                        CircleShape
                    )
                    .clickable { onToggleDone() },
                contentAlignment = Alignment.Center
            ) {
                if (isDone) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Completed Set",
                        tint = MaterialTheme.colorScheme.background,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        // Delete button if applicable
        if (onRemoveSet != null) {
            IconButton(
                onClick = onRemoveSet,
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Remove set",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    }
}

@Composable
fun SprintRoundRowItem(
    setModel: WorkoutSetUiModel,
    defaultDuration: Int,
    onToggleDone: () -> Unit,
    onDurationChanged: (Int) -> Unit,
    onRemoveRound: (() -> Unit)?,
    modifier: Modifier = Modifier
) {
    val isDone = setModel.isCompleted
    val rowBg = if (isDone) {
        EmeraldPrimary.copy(alpha = 0.12f)
    } else {
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    }
    val rowBorder = if (isDone) {
        EmeraldPrimary.copy(alpha = 0.5f)
    } else {
        MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
    }

    // In sprint mode, setModel.reps stores the duration in seconds (falling back to defaultDuration if <= 0)
    val currentDuration = if (setModel.reps > 0) setModel.reps else defaultDuration

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(rowBg)
            .border(1.dp, rowBorder, RoundedCornerShape(12.dp))
            .padding(horizontal = 8.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Round label
        Box(
            modifier = Modifier.width(54.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "R${setModel.setNumber}",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = if (isDone) EmeraldPrimary else MaterialTheme.colorScheme.onSurface
            )
        }

        // Duration Stepper Controls
        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surface)
                    .clickable(enabled = currentDuration > 5) {
                        onDurationChanged((currentDuration - 5).coerceAtLeast(5))
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "−",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = "${currentDuration}s",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = if (isDone) EmeraldPrimary else CyanAccent,
                modifier = Modifier.width(50.dp),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.width(8.dp))

            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surface)
                    .clickable(enabled = currentDuration < 600) {
                        onDurationChanged((currentDuration + 5).coerceAtMost(600))
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "+",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        // Checkbox button
        Box(
            modifier = Modifier.width(48.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(30.dp)
                    .clip(CircleShape)
                    .background(
                        if (isDone) EmeraldPrimary else MaterialTheme.colorScheme.surface
                    )
                    .border(
                        1.dp,
                        if (isDone) EmeraldPrimary else MaterialTheme.colorScheme.outlineVariant,
                        CircleShape
                    )
                    .clickable { onToggleDone() },
                contentAlignment = Alignment.Center
            ) {
                if (isDone) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Completed Round",
                        tint = MaterialTheme.colorScheme.background,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        // Delete button if applicable
        if (onRemoveRound != null) {
            IconButton(
                onClick = onRemoveRound,
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Remove round",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    }
}

