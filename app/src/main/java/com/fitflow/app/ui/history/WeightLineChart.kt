package com.fitflow.app.ui.history

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MonitorWeight
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.SyncAlt
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingFlat
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fitflow.app.data.local.model.WeightChartStyle
import com.fitflow.app.data.local.model.WeightDataPoint
import com.fitflow.app.data.local.model.WeightSummaryStats
import com.fitflow.app.data.local.model.WeightTimeRange
import com.fitflow.app.data.local.model.WeightUnit
import com.fitflow.app.data.local.model.calculateMovingAverage
import com.fitflow.app.data.local.model.calculateWeightSummaryStats
import com.fitflow.app.data.local.model.filterWeightTimeline
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

@Composable
fun WeightTrackingSection(
    timeline: List<WeightDataPoint>,
    stats: WeightSummaryStats,
    onLogWeightClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedRange by remember { mutableStateOf(WeightTimeRange.ALL) }
    var selectedUnit by remember { mutableStateOf(WeightUnit.KG) }
    var chartStyle by remember { mutableStateOf(WeightChartStyle.SMOOTH) }
    var showMovingAverage by remember { mutableStateOf(false) }
    var showOnlyLoggedDays by remember { mutableStateOf(false) }

    // Filter timeline based on chosen time range and continuous/isolated mode
    val filteredTimeline = remember(timeline, selectedRange, showOnlyLoggedDays) {
        val rangeFiltered = filterWeightTimeline(timeline, selectedRange)
        if (showOnlyLoggedDays) {
            val onlyLogged = rangeFiltered.filter { !it.isCarriedForward }
            if (onlyLogged.isNotEmpty()) onlyLogged else rangeFiltered
        } else {
            rangeFiltered
        }
    }

    // Dynamic stats recalculated for the currently selected timeframe
    val dynamicStats = remember(filteredTimeline, selectedUnit) {
        if (filteredTimeline.isEmpty()) {
            WeightSummaryStats()
        } else {
            val startKg = filteredTimeline.first().weightKg
            val latestKg = filteredTimeline.last().weightKg
            val minKg = filteredTimeline.minOf { it.weightKg }
            val maxKg = filteredTimeline.maxOf { it.weightKg }
            val totalLogged = filteredTimeline.count { !it.isCarriedForward }

            val startConverted = selectedUnit.fromKg(startKg)
            val latestConverted = selectedUnit.fromKg(latestKg)
            val changeConverted = ((latestConverted - startConverted) * 10.0).roundToInt() / 10.0
            val minConverted = selectedUnit.fromKg(minKg)
            val maxConverted = selectedUnit.fromKg(maxKg)

            WeightSummaryStats(
                latestWeight = latestConverted,
                startingWeight = startConverted,
                weightChange = changeConverted,
                minWeight = minConverted,
                maxWeight = maxConverted,
                totalLoggedDays = totalLogged
            )
        }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Section Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "WEIGHT PROGRESSION",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 1.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Body Weight Over Time",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Button(
                    onClick = onLogWeightClick,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Log Weight",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            if (timeline.isEmpty()) {
                // Empty state
                EmptyWeightState(onLogWeightClick = onLogWeightClick)
            } else {
                // Time Range Filters & Unit Toggle Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Time Range Selector
                    TimeRangeSegmentedBar(
                        selectedRange = selectedRange,
                        onRangeSelected = { selectedRange = it },
                        modifier = Modifier.weight(1f)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    // Unit Toggle (KG / LBS)
                    UnitTogglePill(
                        selectedUnit = selectedUnit,
                        onUnitSelected = { selectedUnit = it }
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Secondary Options Row: Curve/Line, 7D Moving Avg, Only Logged
                GraphOptionsRow(
                    chartStyle = chartStyle,
                    onStyleToggled = {
                        chartStyle = if (chartStyle == WeightChartStyle.SMOOTH) WeightChartStyle.LINEAR else WeightChartStyle.SMOOTH
                    },
                    showMovingAverage = showMovingAverage,
                    onMovingAverageToggled = { showMovingAverage = !showMovingAverage },
                    showOnlyLoggedDays = showOnlyLoggedDays,
                    onOnlyLoggedToggled = { showOnlyLoggedDays = !showOnlyLoggedDays }
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Stats summary row
                WeightStatsSummaryRow(
                    stats = dynamicStats,
                    unit = selectedUnit
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Interactive Line Chart
                WeightLineChart(
                    timeline = filteredTimeline,
                    unit = selectedUnit,
                    chartStyle = chartStyle,
                    showMovingAverage = showMovingAverage,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(230.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Chart Legend
                WeightChartLegend(
                    showMovingAverage = showMovingAverage,
                    showOnlyLoggedDays = showOnlyLoggedDays
                )
            }
        }
    }
}

@Composable
private fun TimeRangeSegmentedBar(
    selectedRange: WeightTimeRange,
    onRangeSelected: (WeightTimeRange) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), RoundedCornerShape(10.dp)),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(2.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            WeightTimeRange.entries.forEach { range ->
                val isSelected = selectedRange == range
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent
                        )
                        .clickable { onRangeSelected(range) }
                        .padding(vertical = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = range.label,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun UnitTogglePill(
    selectedUnit: WeightUnit,
    onUnitSelected: (WeightUnit) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), RoundedCornerShape(10.dp)),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
    ) {
        Row(
            modifier = Modifier.padding(2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            WeightUnit.entries.forEach { unit ->
                val isSelected = selectedUnit == unit
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent
                        )
                        .clickable { onUnitSelected(unit) }
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = unit.label.uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 10.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun GraphOptionsRow(
    chartStyle: WeightChartStyle,
    onStyleToggled: () -> Unit,
    showMovingAverage: Boolean,
    onMovingAverageToggled: () -> Unit,
    showOnlyLoggedDays: Boolean,
    onOnlyLoggedToggled: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Curve vs Straight line toggle
        OptionChip(
            label = if (chartStyle == WeightChartStyle.SMOOTH) "Curve" else "Line",
            isSelected = chartStyle == WeightChartStyle.SMOOTH,
            icon = Icons.Default.Timeline,
            onClick = onStyleToggled
        )

        // 7-day moving average overlay toggle
        OptionChip(
            label = "7D Trend",
            isSelected = showMovingAverage,
            icon = Icons.Default.ShowChart,
            onClick = onMovingAverageToggled
        )

        // Only logged days toggle
        OptionChip(
            label = if (showOnlyLoggedDays) "Logged Only" else "Continuous",
            isSelected = showOnlyLoggedDays,
            icon = Icons.Default.CheckCircle,
            onClick = onOnlyLoggedToggled
        )
    }
}

@Composable
private fun OptionChip(
    label: String,
    isSelected: Boolean,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .border(
                1.dp,
                if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.6f) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f),
                RoundedCornerShape(8.dp)
            )
            .clickable { onClick() },
        color = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(12.dp)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                fontSize = 10.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun WeightStatsSummaryRow(
    stats: WeightSummaryStats,
    unit: WeightUnit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        val latest = stats.latestWeight
        val start = stats.startingWeight
        val change = stats.weightChange

        val changeColor = when {
            change == null || change == 0.0 -> MaterialTheme.colorScheme.onSurfaceVariant
            change < 0 -> MaterialTheme.colorScheme.primary
            else -> MaterialTheme.colorScheme.tertiary
        }

        val changeIcon = when {
            change == null || change == 0.0 -> Icons.Default.TrendingFlat
            change < 0 -> Icons.Default.TrendingDown
            else -> Icons.Default.TrendingUp
        }

        StatItem(
            label = "CURRENT",
            value = if (latest != null) "$latest ${unit.label}" else "--",
            modifier = Modifier.weight(1f)
        )

        StatItem(
            label = "STARTING",
            value = if (start != null) "$start ${unit.label}" else "--",
            modifier = Modifier.weight(1f)
        )

        Surface(
            modifier = Modifier
                .weight(1.1f)
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f), RoundedCornerShape(12.dp)),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = "TOTAL CHANGE",
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = changeIcon,
                        contentDescription = null,
                        tint = changeColor,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = if (change != null) {
                            val sign = if (change > 0) "+" else ""
                            "$sign$change ${unit.label}"
                        } else "--",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = changeColor,
                        fontSize = 13.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun StatItem(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f), RoundedCornerShape(12.dp)),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 13.sp
            )
        }
    }
}

@Composable
fun WeightLineChart(
    timeline: List<WeightDataPoint>,
    unit: WeightUnit = WeightUnit.KG,
    chartStyle: WeightChartStyle = WeightChartStyle.SMOOTH,
    showMovingAverage: Boolean = false,
    modifier: Modifier = Modifier
) {
    if (timeline.isEmpty()) return

    // Convert weights to selected unit
    val convertedWeights = remember(timeline, unit) {
        timeline.map { unit.fromKg(it.weightKg) }
    }

    val movingAverages = remember(timeline, unit, showMovingAverage) {
        if (showMovingAverage) {
            val maKg = calculateMovingAverage(timeline, windowSize = 7)
            maKg.map { unit.fromKg(it) }
        } else {
            emptyList()
        }
    }

    var selectedIndex by remember { mutableStateOf<Int?>(null) }

    val rawMinWeight = convertedWeights.minOrNull() ?: 0.0
    val rawMaxWeight = convertedWeights.maxOrNull() ?: 100.0

    // Add padding to Y-axis min/max
    val minWeight = ((rawMinWeight - 1.0) * 2.0).toInt() / 2.0
    val maxWeight = ((rawMaxWeight + 1.0) * 2.0).toInt() / 2.0
    val weightRange = max(1.0, maxWeight - minWeight)

    val primaryColor = MaterialTheme.colorScheme.primary
    val tertiaryColor = MaterialTheme.colorScheme.tertiary
    val surfaceElevated = MaterialTheme.colorScheme.surface
    val outlineColor = MaterialTheme.colorScheme.outlineVariant
    val textColor = MaterialTheme.colorScheme.onSurfaceVariant

    Column(modifier = modifier) {
        // Floating Scrubber Value Pill
        AnimatedVisibility(
            visible = selectedIndex != null && selectedIndex!! in timeline.indices,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .fillMaxWidth()
                .height(42.dp)
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                selectedIndex?.let { index ->
                    val point = timeline[index]
                    val weightVal = convertedWeights[index]
                    val maVal = movingAverages.getOrNull(index)

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        border = androidx.compose.foundation.BorderStroke(1.dp, primaryColor.copy(alpha = 0.5f)),
                        shadowElevation = 4.dp
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Column {
                                Text(
                                    text = point.date.format(DateTimeFormatter.ofPattern("EEE, MMM d, yyyy")),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                if (point.isCarriedForward) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Info,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(10.dp)
                                        )
                                        Spacer(modifier = Modifier.width(2.dp))
                                        Text(
                                            text = "Carried from ${point.sourceDateStr}",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontSize = 9.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                } else {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(primaryColor.copy(alpha = 0.2f))
                                            .padding(horizontal = 4.dp, vertical = 1.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = null,
                                            tint = primaryColor,
                                            modifier = Modifier.size(10.dp)
                                        )
                                        Spacer(modifier = Modifier.width(2.dp))
                                        Text(
                                            text = "Logged entry",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontSize = 9.sp,
                                            color = primaryColor,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "$weightVal ${unit.label}",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = primaryColor
                                )
                                if (maVal != null) {
                                    Text(
                                        text = "7D Avg: $maVal ${unit.label}",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontSize = 9.sp,
                                        color = tertiaryColor,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .pointerInput(timeline) {
                    detectTapGestures(
                        onPress = { offset ->
                            val width = size.width
                            val step = if (timeline.size > 1) (width - 80f) / (timeline.size - 1) else 1f
                            val rawIndex = ((offset.x - 40f) / step).roundToInt()
                            val clamped = rawIndex.coerceIn(0, timeline.lastIndex)
                            selectedIndex = clamped
                        },
                        onTap = { offset ->
                            val width = size.width
                            val step = if (timeline.size > 1) (width - 80f) / (timeline.size - 1) else 1f
                            val rawIndex = ((offset.x - 40f) / step).roundToInt()
                            val clamped = rawIndex.coerceIn(0, timeline.lastIndex)
                            selectedIndex = if (selectedIndex == clamped) null else clamped
                        }
                    )
                }
                .pointerInput(timeline) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            val width = size.width
                            val step = if (timeline.size > 1) (width - 80f) / (timeline.size - 1) else 1f
                            val rawIndex = ((offset.x - 40f) / step).roundToInt()
                            selectedIndex = rawIndex.coerceIn(0, timeline.lastIndex)
                        },
                        onDrag = { change, _ ->
                            val width = size.width
                            val step = if (timeline.size > 1) (width - 80f) / (timeline.size - 1) else 1f
                            val rawIndex = ((change.position.x - 40f) / step).roundToInt()
                            selectedIndex = rawIndex.coerceIn(0, timeline.lastIndex)
                        },
                        onDragEnd = {
                            // Keep selection visible
                        }
                    )
                }
        ) {
            Canvas(modifier = Modifier.matchParentSize()) {
                val canvasWidth = size.width
                val canvasHeight = size.height

                val leftMargin = 38.dp.toPx()
                val rightMargin = 16.dp.toPx()
                val topMargin = 16.dp.toPx()
                val bottomMargin = 26.dp.toPx()

                val chartWidth = canvasWidth - leftMargin - rightMargin
                val chartHeight = canvasHeight - topMargin - bottomMargin

                // 1. Draw horizontal gridlines & Y-axis labels
                val gridSteps = 4
                val paint = android.graphics.Paint().apply {
                    color = textColor.hashCode()
                    textSize = 9.sp.toPx()
                    isAntiAlias = true
                    textAlign = android.graphics.Paint.Align.RIGHT
                }

                for (i in 0..gridSteps) {
                    val ratio = i.toFloat() / gridSteps.toFloat()
                    val y = topMargin + chartHeight * (1f - ratio)
                    val labelValue = minWeight + weightRange * ratio
                    val labelText = String.format("%.1f", labelValue)

                    // Grid line
                    drawLine(
                        color = outlineColor.copy(alpha = 0.35f),
                        start = Offset(leftMargin, y),
                        end = Offset(canvasWidth - rightMargin, y),
                        strokeWidth = 1.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f), 0f)
                    )

                    // Y Label
                    drawContext.canvas.nativeCanvas.drawText(
                        labelText,
                        leftMargin - 6.dp.toPx(),
                        y + 3.dp.toPx(),
                        paint
                    )
                }

                // 2. Compute points on canvas
                val pointOffsets = convertedWeights.mapIndexed { index, weightVal ->
                    val x = if (timeline.size > 1) {
                        leftMargin + (index.toFloat() / (timeline.size - 1).toFloat()) * chartWidth
                    } else {
                        leftMargin + chartWidth / 2f
                    }

                    val normalizedY = ((weightVal - minWeight) / weightRange).coerceIn(0.0, 1.0)
                    val y = topMargin + chartHeight * (1f - normalizedY.toFloat())
                    Offset(x, y)
                }

                if (pointOffsets.isNotEmpty()) {
                    // 3. Draw gradient fill under curve
                    val fillPath = Path()
                    fillPath.moveTo(pointOffsets.first().x, topMargin + chartHeight)
                    fillPath.lineTo(pointOffsets.first().x, pointOffsets.first().y)

                    if (pointOffsets.size == 1) {
                        fillPath.lineTo(pointOffsets.first().x + 20f, pointOffsets.first().y)
                        fillPath.lineTo(pointOffsets.first().x + 20f, topMargin + chartHeight)
                    } else {
                        if (chartStyle == WeightChartStyle.SMOOTH) {
                            for (i in 0 until pointOffsets.size - 1) {
                                val p0 = pointOffsets[i]
                                val p1 = pointOffsets[i + 1]
                                val controlX = (p0.x + p1.x) / 2f
                                fillPath.cubicTo(controlX, p0.y, controlX, p1.y, p1.x, p1.y)
                            }
                        } else {
                            for (i in 1 until pointOffsets.size) {
                                fillPath.lineTo(pointOffsets[i].x, pointOffsets[i].y)
                            }
                        }
                        fillPath.lineTo(pointOffsets.last().x, topMargin + chartHeight)
                    }
                    fillPath.close()

                    drawPath(
                        path = fillPath,
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                primaryColor.copy(alpha = 0.35f),
                                primaryColor.copy(alpha = 0.08f),
                                Color.Transparent
                            ),
                            startY = topMargin,
                            endY = topMargin + chartHeight
                        )
                    )

                    // 4. Draw stroke line (Smooth or Linear)
                    val strokePath = Path()
                    strokePath.moveTo(pointOffsets.first().x, pointOffsets.first().y)

                    if (pointOffsets.size > 1) {
                        if (chartStyle == WeightChartStyle.SMOOTH) {
                            for (i in 0 until pointOffsets.size - 1) {
                                val p0 = pointOffsets[i]
                                val p1 = pointOffsets[i + 1]
                                val controlX = (p0.x + p1.x) / 2f
                                strokePath.cubicTo(controlX, p0.y, controlX, p1.y, p1.x, p1.y)
                            }
                        } else {
                            for (i in 1 until pointOffsets.size) {
                                strokePath.lineTo(pointOffsets[i].x, pointOffsets[i].y)
                            }
                        }
                    }

                    drawPath(
                        path = strokePath,
                        color = primaryColor,
                        style = Stroke(
                            width = 2.5.dp.toPx(),
                            cap = StrokeCap.Round,
                            join = StrokeJoin.Round
                        )
                    )

                    // 4b. Draw 7-Day Moving Average overlay if enabled
                    if (showMovingAverage && movingAverages.size == pointOffsets.size) {
                        val maPath = Path()
                        val maOffsets = movingAverages.mapIndexed { index, maVal ->
                            val x = pointOffsets[index].x
                            val normalizedY = ((maVal - minWeight) / weightRange).coerceIn(0.0, 1.0)
                            val y = topMargin + chartHeight * (1f - normalizedY.toFloat())
                            Offset(x, y)
                        }

                        maPath.moveTo(maOffsets.first().x, maOffsets.first().y)
                        for (i in 0 until maOffsets.size - 1) {
                            val p0 = maOffsets[i]
                            val p1 = maOffsets[i + 1]
                            val controlX = (p0.x + p1.x) / 2f
                            maPath.cubicTo(controlX, p0.y, controlX, p1.y, p1.x, p1.y)
                        }

                        drawPath(
                            path = maPath,
                            color = tertiaryColor,
                            style = Stroke(
                                width = 2.dp.toPx(),
                                cap = StrokeCap.Round,
                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 6f), 0f)
                            )
                        )
                    }

                    // 5. Draw data points
                    pointOffsets.forEachIndexed { index, offset ->
                        val item = timeline[index]
                        val isSelected = selectedIndex == index

                        if (!item.isCarriedForward || isSelected) {
                            // Actual recorded entry: Glowing outer ring + solid center
                            drawCircle(
                                color = primaryColor.copy(alpha = if (isSelected) 0.5f else 0.25f),
                                radius = if (isSelected) 7.dp.toPx() else 5.dp.toPx(),
                                center = offset
                            )
                            drawCircle(
                                color = surfaceElevated,
                                radius = if (isSelected) 4.5.dp.toPx() else 3.5.dp.toPx(),
                                center = offset
                            )
                            drawCircle(
                                color = primaryColor,
                                radius = if (isSelected) 3.5.dp.toPx() else 2.5.dp.toPx(),
                                center = offset
                            )
                        } else {
                            // Carried-forward point: subtle dot only if few points, or clean line
                            if (timeline.size <= 20) {
                                drawCircle(
                                    color = primaryColor.copy(alpha = 0.4f),
                                    radius = 1.5.dp.toPx(),
                                    center = offset
                                )
                            }
                        }
                    }

                    // 6. Draw vertical scrubber indicator if active
                    selectedIndex?.let { selIdx ->
                        if (selIdx in pointOffsets.indices) {
                            val activeOffset = pointOffsets[selIdx]
                            drawLine(
                                color = primaryColor.copy(alpha = 0.8f),
                                start = Offset(activeOffset.x, topMargin),
                                end = Offset(activeOffset.x, topMargin + chartHeight),
                                strokeWidth = 1.5.dp.toPx(),
                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)
                            )
                        }
                    }

                    // 7. Draw X-axis date labels (Oldest, middle intervals, and Current Day)
                    val datePaint = android.graphics.Paint().apply {
                        color = textColor.hashCode()
                        textSize = 9.sp.toPx()
                        isAntiAlias = true
                        textAlign = android.graphics.Paint.Align.CENTER
                    }

                    val firstDateStr = formatShortDate(timeline.first().date)
                    val lastDateStr = formatShortDate(timeline.last().date)

                    // Draw first date
                    drawContext.canvas.nativeCanvas.drawText(
                        firstDateStr,
                        pointOffsets.first().x,
                        canvasHeight - 4.dp.toPx(),
                        datePaint
                    )

                    // Draw middle dates if space permits
                    if (timeline.size >= 7) {
                        val midIndex = timeline.size / 2
                        val midDateStr = formatShortDate(timeline[midIndex].date)
                        drawContext.canvas.nativeCanvas.drawText(
                            midDateStr,
                            pointOffsets[midIndex].x,
                            canvasHeight - 4.dp.toPx(),
                            datePaint
                        )
                    }

                    // Draw last date (Today)
                    if (timeline.size > 1) {
                        drawContext.canvas.nativeCanvas.drawText(
                            lastDateStr,
                            pointOffsets.last().x,
                            canvasHeight - 4.dp.toPx(),
                            datePaint
                        )
                    }
                }
            }
        }
    }
}

private fun formatShortDate(date: LocalDate): String {
    return try {
        date.format(DateTimeFormatter.ofPattern("MMM d"))
    } catch (e: Exception) {
        date.toString()
    }
}

@Composable
private fun WeightChartLegend(
    showMovingAverage: Boolean = false,
    showOnlyLoggedDays: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "Tap & drag across chart to scrub daily history",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 10.sp
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Logged",
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (!showOnlyLoggedDays) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .width(12.dp)
                            .height(2.dp)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Carried",
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (showMovingAverage) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .width(12.dp)
                            .height(2.dp)
                            .background(MaterialTheme.colorScheme.tertiary)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "7D Trend",
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.tertiary
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyWeightState(onLogWeightClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f), RoundedCornerShape(16.dp)),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.MonitorWeight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp)
                )
            }

            Text(
                text = "No Weight Entries Yet",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = "Log your weight daily to visualize your progress over time. Missed days will automatically carry forward to keep your chart uninterrupted!",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(4.dp))

            Button(
                onClick = onLogWeightClick,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 10.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Log Today's Weight",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
