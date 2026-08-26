package com.fitflow.app.ui.home

import androidx.compose.animation.animateColorAsState
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fitflow.app.ui.components.CategoryBadge
import com.fitflow.app.ui.components.NumberStepper
import com.fitflow.app.ui.components.SprintBadge
import com.fitflow.app.ui.theme.CyanAccent
import com.fitflow.app.ui.theme.EmeraldLight
import com.fitflow.app.ui.theme.EmeraldPrimary

@Composable
fun ExerciseLogCard(
    item: ExerciseLogItem,
    onValuesChanged: (sets: Int, reps: Int, weight: Double) -> Unit,
    onDurationChanged: (Int) -> Unit = {},
    onToggleCompleted: () -> Unit,
    onOpenTimer: () -> Unit,
    modifier: Modifier = Modifier
) {
    val borderColor by animateColorAsState(
        targetValue = if (item.isCompleted) EmeraldPrimary else MaterialTheme.colorScheme.outlineVariant,
        label = "BorderColor"
    )
    val cardBackground by animateColorAsState(
        targetValue = if (item.isCompleted) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f) else MaterialTheme.colorScheme.surface,
        label = "CardBackground"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .border(
                width = if (item.isCompleted) 1.5.dp else 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(18.dp)
            ),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = cardBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = if (item.isCompleted) 1.dp else 3.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Row: Category Badge + Rest Timer + Checkmark status
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
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

                // Check circle indicator
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(
                            if (item.isCompleted) EmeraldPrimary else MaterialTheme.colorScheme.surfaceVariant
                        )
                        .clickable { onToggleCompleted() },
                    contentAlignment = Alignment.Center
                ) {
                    if (item.isCompleted) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Completed",
                            tint = MaterialTheme.colorScheme.background,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Exercise Title & Target
            Text(
                text = item.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = if (item.isSprint) {
                    "Target: ${item.targetDurationSeconds}s sprint"
                } else {
                    "Target: ${item.targetSets} sets × ${item.targetReps} reps"
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(14.dp))

            // Inputs: Duration (for sprint) OR Sets | Reps | Weight (for standard)
            if (item.isSprint) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    NumberStepper(
                        label = "Duration",
                        value = item.actualDurationSeconds,
                        onValueChange = { newDuration ->
                            onDurationChanged(newDuration)
                        },
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
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    NumberStepper(
                        label = "Sets",
                        value = item.actualSets,
                        onValueChange = { newSets ->
                            onValuesChanged(newSets, item.actualReps, item.actualWeight)
                        },
                        minValue = 1,
                        maxValue = 20,
                        modifier = Modifier.weight(1f)
                    )

                    NumberStepper(
                        label = "Reps",
                        value = item.actualReps,
                        onValueChange = { newReps ->
                            onValuesChanged(item.actualSets, newReps, item.actualWeight)
                        },
                        minValue = 1,
                        maxValue = 100,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Complete Button
            Button(
                onClick = onToggleCompleted,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(42.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (item.isCompleted) EmeraldPrimary.copy(alpha = 0.18f) else EmeraldPrimary,
                    contentColor = if (item.isCompleted) EmeraldLight else MaterialTheme.colorScheme.background
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (item.isCompleted) "Completed (Tap to undo)" else "Log & Mark Complete",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }
        }
    }
}
