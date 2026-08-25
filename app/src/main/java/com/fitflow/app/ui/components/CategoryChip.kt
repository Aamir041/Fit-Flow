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
import com.fitflow.app.ui.theme.AmberAccent
import com.fitflow.app.ui.theme.CategoryArms
import com.fitflow.app.ui.theme.CategoryBack
import com.fitflow.app.ui.theme.CategoryCalves
import com.fitflow.app.ui.theme.CategoryCardio
import com.fitflow.app.ui.theme.CategoryChest
import com.fitflow.app.ui.theme.CategoryCore
import com.fitflow.app.ui.theme.CategoryForearms
import com.fitflow.app.ui.theme.CategoryFullBody
import com.fitflow.app.ui.theme.CategoryGlutes
import com.fitflow.app.ui.theme.CategoryLegs
import com.fitflow.app.ui.theme.CategoryShoulders
import com.fitflow.app.ui.theme.CategorySprint
import com.fitflow.app.ui.theme.CategoryTraps
import com.fitflow.app.ui.theme.CyanAccent
import com.fitflow.app.ui.theme.EmeraldPrimary

private val DynamicColors = listOf(
    Color(0xFFFF4081), // Pink
    Color(0xFF7C4DFF), // Deep Purple
    Color(0xFF536DFE), // Indigo
    Color(0xFF00E5FF), // Cyan
    Color(0xFFFFAB40), // Orange
    Color(0xFF69F0AE), // Mint
    Color(0xFFFFD740), // Amber
    Color(0xFFE040FB), // Fuchsia
    Color(0xFFFF6E40)  // Coral
)

fun getCategoryColor(category: String): Color {
    return when (category.trim().lowercase()) {
        "chest" -> CategoryChest
        "back" -> CategoryBack
        "legs" -> CategoryLegs
        "shoulders" -> CategoryShoulders
        "arms" -> CategoryArms
        "core", "abs" -> CategoryCore
        "cardio" -> CategoryCardio
        "glutes", "glute" -> CategoryGlutes
        "forearms", "forearm" -> CategoryForearms
        "traps", "trap" -> CategoryTraps
        "calves", "calf" -> CategoryCalves
        "full body", "fullbody" -> CategoryFullBody
        "sprint", "sprints", "hiit" -> CategorySprint
        else -> {
            // Deterministic dynamic color selection based on muscle group name hash
            val hash = kotlin.math.abs(category.hashCode())
            DynamicColors[hash % DynamicColors.size]
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
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(CategorySprint.copy(alpha = 0.18f))
            .border(1.dp, CategorySprint.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
            .padding(horizontal = 7.dp, vertical = 3.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.ElectricBolt,
                contentDescription = null,
                tint = CategorySprint,
                modifier = Modifier.size(12.dp)
            )
            Spacer(modifier = Modifier.width(3.dp))
            Text(
                text = if (durationSeconds != null && durationSeconds > 0) "SPRINT ${durationSeconds}s" else "SPRINT",
                style = MaterialTheme.typography.labelSmall,
                color = CategorySprint,
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
    val activeColor = if (category == "All") EmeraldPrimary else getCategoryColor(category)
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

