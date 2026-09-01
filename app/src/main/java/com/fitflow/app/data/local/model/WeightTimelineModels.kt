package com.fitflow.app.data.local.model

import com.fitflow.app.data.local.entity.WeightLogEntity
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.math.roundToInt

/**
 * Represents a single point in the continuous weight tracking timeline.
 * If [isCarriedForward] is true, this point represents an auto-interpolated day
 * carrying forward the value from [sourceDateStr].
 */
data class WeightDataPoint(
    val date: LocalDate,
    val dateStr: String,
    val weightKg: Double,
    val isCarriedForward: Boolean,
    val sourceDateStr: String
)

data class WeightSummaryStats(
    val latestWeight: Double? = null,
    val startingWeight: Double? = null,
    val weightChange: Double? = null,
    val minWeight: Double? = null,
    val maxWeight: Double? = null,
    val totalLoggedDays: Int = 0
)

/**
 * Transforms discrete weight logs into a continuous daily timeline starting from
 * the oldest recorded log entry and extending to [today].
 * Missed days automatically carry forward the most recent recorded weight.
 */
fun calculateWeightTimeline(
    logs: List<WeightLogEntity>,
    today: LocalDate = LocalDate.now()
): List<WeightDataPoint> {
    if (logs.isEmpty()) return emptyList()

    val validLogs = logs.mapNotNull { log ->
        try {
            val parsedDate = LocalDate.parse(log.date.trim(), DateTimeFormatter.ISO_LOCAL_DATE)
            parsedDate to log
        } catch (e: Exception) {
            null
        }
    }.sortedBy { it.first }

    if (validLogs.isEmpty()) return emptyList()

    val logsByDate = validLogs.associate { it.first to it.second }
    val oldestDate = validLogs.first().first
    val latestLogDate = validLogs.last().first

    // X-axis starts from the oldest recorded entry and extends to current day (or latest log date if logged in advance)
    val endDate = when {
        today.isBefore(oldestDate) -> oldestDate
        latestLogDate.isAfter(today) -> latestLogDate
        else -> today
    }

    val result = mutableListOf<WeightDataPoint>()
    var currentDate = oldestDate
    var lastKnownWeight = validLogs.first().second.weightKg
    var lastSourceDateStr = validLogs.first().second.date

    while (!currentDate.isAfter(endDate)) {
        val dateStr = currentDate.format(DateTimeFormatter.ISO_LOCAL_DATE)
        val logForDay = logsByDate[currentDate]

        if (logForDay != null) {
            lastKnownWeight = logForDay.weightKg
            lastSourceDateStr = logForDay.date
            result.add(
                WeightDataPoint(
                    date = currentDate,
                    dateStr = dateStr,
                    weightKg = lastKnownWeight,
                    isCarriedForward = false,
                    sourceDateStr = lastSourceDateStr
                )
            )
        } else {
            result.add(
                WeightDataPoint(
                    date = currentDate,
                    dateStr = dateStr,
                    weightKg = lastKnownWeight,
                    isCarriedForward = true,
                    sourceDateStr = lastSourceDateStr
                )
            )
        }

        currentDate = currentDate.plusDays(1)
    }

    return result
}

fun calculateWeightSummaryStats(
    logs: List<WeightLogEntity>,
    timeline: List<WeightDataPoint>
): WeightSummaryStats {
    if (timeline.isEmpty()) return WeightSummaryStats()

    val startingWeight = timeline.first().weightKg
    val latestWeight = timeline.last().weightKg
    val change = ((latestWeight - startingWeight) * 10.0).roundToInt() / 10.0
    val min = timeline.minOf { it.weightKg }
    val max = timeline.maxOf { it.weightKg }

    return WeightSummaryStats(
        latestWeight = latestWeight,
        startingWeight = startingWeight,
        weightChange = change,
        minWeight = min,
        maxWeight = max,
        totalLoggedDays = logs.size
    )
}
