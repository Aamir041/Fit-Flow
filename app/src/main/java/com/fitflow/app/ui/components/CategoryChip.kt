package com.fitflow.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val CategoryHuePalette = listOf(
    Color(0xFFFF5252), // Red
    Color(0xFF448AFF), // Blue
    Color(0xFF69F0AE), // Mint
    Color(0xFFFFAB40), // Orange
    Color(0xFFE040FB), // Purple
    Color(0xFFFFD740), // Gold
    Color(0xFF18FFFF), // Cyan
    Color(0xFFFF4081), // Pink
    Color(0xFF7C4DFF), // Indigo
    Color(0xFF00E5FF)  // Aqua
)

fun getCategoryColor(category: String): Color {
    return when (category.trim().lowercase()) {
        "chest" -> Color(0xFFFF5252)
        "back" -> Color(0xFF448AFF)
        "legs" -> Color(0xFF69F0AE)
        "shoulders" -> Color(0xFFFFAB40)
        "arms" -> Color(0xFFE040FB)
        "core", "abs" -> Color(0xFFFFD740)
        "cardio" -> Color(0xFF18FFFF)
        "glutes", "glute" -> Color(0xFFFF4081)
        "forearms", "forearm" -> Color(0xFF7C4DFF)
        "traps", "trap" -> Color(0xFF536DFE)
        "calves", "calf" -> Color(0xFF00E5FF)
        "full body", "fullbody" -> Color(0xFFFF6E40)
        "sprint", "sprints", "hiit" -> Color(0xFFFFD600)
        else -> {
            val hash = kotlin.math.abs(category.hashCode())
            CategoryHuePalette[hash % CategoryHuePalette.size]
        }
    }
}

@Composable
fun CategoryBadge(
    category: String,
    modifier: Modifier = Modifier
) {
    val catColor = getCategoryColor(category)
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(catColor.copy(alpha = 0.15f))
            .border(1.dp, catColor.copy(alpha = 0.35f), RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(
            text = category.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = catColor,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
fun SprintBadge(
    durationSeconds: Int? = null,
    modifier: Modifier = Modifier
) {
    val sprintColor = Color(0xFFFFD600)
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(sprintColor.copy(alpha = 0.18f))
            .border(1.dp, sprintColor.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
            .padding(horizontal = 7.dp, vertical = 3.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.ElectricBolt,
                contentDescription = null,
                tint = sprintColor,
                modifier = Modifier.size(12.dp)
            )
            Spacer(modifier = Modifier.width(3.dp))
            Text(
                text = if (durationSeconds != null && durationSeconds > 0) "SPRINT ${durationSeconds}s" else "SPRINT",
                style = MaterialTheme.typography.labelSmall,
                color = sprintColor,
                fontWeight = FontWeight.Bold,
                fontSize = 9.sp
            )
        }
    }
}

@Composable
fun CategoryFilterChip(
    category: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val activeColor = if (category == "All") MaterialTheme.colorScheme.primary else getCategoryColor(category)
    val background = if (isSelected) activeColor.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant
    val borderColor = if (isSelected) activeColor else MaterialTheme.colorScheme.outlineVariant

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(background)
            .border(1.dp, borderColor, RoundedCornerShape(20.dp))
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 6.dp)
    ) {
        Text(
            text = category,
            style = MaterialTheme.typography.labelMedium,
            color = if (isSelected) activeColor else MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )
    }
}
