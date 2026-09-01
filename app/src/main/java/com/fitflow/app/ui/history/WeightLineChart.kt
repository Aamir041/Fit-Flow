package com.fitflow.app.ui.history

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MonitorWeight
import androidx.compose.material.icons.filled.SyncAlt
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingFlat
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import com.fitflow.app.data.local.model.WeightDataPoint
import com.fitflow.app.data.local.model.WeightSummaryStats
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
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 6.dp)
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
                // Stats summary row
                WeightStatsSummaryRow(stats = stats)

                Spacer(modifier = Modifier.height(16.dp))

                // Interactive Line Chart
                WeightLineChart(
                    timeline = timeline,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(230.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Chart Legend
                WeightChartLegend()
            }
        }
    }
}

@Composable
private fun WeightStatsSummaryRow(stats: WeightSummaryStats) {
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
            value = if (latest != null) "$latest kg" else "--",
            modifier = Modifier.weight(1f)
        )

        StatItem(
            label = "STARTING",
            value = if (start != null) "$start kg" else "--",
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
                            "$sign$change kg"
                        } else "--",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = changeColor
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
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun WeightLineChart(
    timeline: List<WeightDataPoint>,
    modifier: Modifier = Modifier
) {
    if (timeline.isEmpty()) return

    var selectedIndex by remember(timeline) { mutableStateOf<Int?>(null) }

    val primaryColor = MaterialTheme.colorScheme.primary
    val outlineColor = MaterialTheme.colorScheme.outlineVariant
    val textColor = MaterialTheme.colorScheme.onSurfaceVariant
    val surfaceElevated = MaterialTheme.colorScheme.surface
    val density = LocalDensity.current

    // Extract bounds with sensible padding
    val rawMin = timeline.minOf { it.weightKg }
    val rawMax = timeline.maxOf { it.weightKg }
    val padding = if (rawMax == rawMin) 2.0 else max(1.0, (rawMax - rawMin) * 0.15)
    val minWeight = ((rawMin - padding) * 10.0).roundToInt() / 10.0
    val maxWeight = ((rawMax + padding) * 10.0).roundToInt() / 10.0
    val weightRange = if (maxWeight - minWeight <= 0.0) 1.0 else maxWeight - minWeight

    val selectedPoint = selectedIndex?.let { if (it in timeline.indices) timeline[it] else null }

    Column(modifier = modifier) {
        // Scrubber Info Pill
        AnimatedVisibility(
            visible = selectedPoint != null,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            selectedPoint?.let { point ->
                val formattedScrubDate = try {
                    point.date.format(DateTimeFormatter.ofPattern("MMM d, yyyy"))
                } catch (e: Exception) {
                    point.dateStr
                }

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 6.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .border(1.dp, primaryColor.copy(alpha = 0.5f), RoundedCornerShape(10.dp)),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.9f)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = formattedScrubDate,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            if (point.isCarriedForward) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                                        .padding(horizontal = 4.dp, vertical = 1.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.SyncAlt,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(10.dp)
                                    )
                                    Spacer(modifier = Modifier.width(2.dp))
                                    Text(
                                        text = "Carried forward",
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

                        Text(
                            text = "${point.weightKg} kg",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = primaryColor
                        )
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
                            // Keep selection visible or dismiss after tap
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
                val pointOffsets = timeline.mapIndexed { index, dataPoint ->
                    val x = if (timeline.size > 1) {
                        leftMargin + (index.toFloat() / (timeline.size - 1).toFloat()) * chartWidth
                    } else {
                        leftMargin + chartWidth / 2f
                    }

                    val normalizedY = ((dataPoint.weightKg - minWeight) / weightRange).coerceIn(0.0, 1.0)
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
                        for (i in 0 until pointOffsets.size - 1) {
                            val p0 = pointOffsets[i]
                            val p1 = pointOffsets[i + 1]
                            val controlX = (p0.x + p1.x) / 2f
                            fillPath.cubicTo(controlX, p0.y, controlX, p1.y, p1.x, p1.y)
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

                    // 4. Draw smooth stroke line
                    val strokePath = Path()
                    strokePath.moveTo(pointOffsets.first().x, pointOffsets.first().y)

                    if (pointOffsets.size > 1) {
                        for (i in 0 until pointOffsets.size - 1) {
                            val p0 = pointOffsets[i]
                            val p1 = pointOffsets[i + 1]
                            val controlX = (p0.x + p1.x) / 2f
                            strokePath.cubicTo(controlX, p0.y, controlX, p1.y, p1.x, p1.y)
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
private fun WeightChartLegend() {
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

            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .width(12.dp)
                        .height(2.dp)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Carried forward",
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
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
                )
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Log First Entry", fontWeight = FontWeight.Bold)
            }
        }
    }
}
