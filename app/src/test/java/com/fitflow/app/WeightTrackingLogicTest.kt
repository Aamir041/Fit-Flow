package com.fitflow.app

import com.fitflow.app.data.local.entity.WeightLogEntity
import com.fitflow.app.data.local.model.HistoryBundleExportJson
import com.fitflow.app.data.local.model.WeightLogExport
import com.fitflow.app.data.local.model.calculateWeightSummaryStats
import com.fitflow.app.data.local.model.calculateWeightTimeline
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class WeightTrackingLogicTest {

    @Test
    fun testEmptyWeightLogsReturnsEmptyTimeline() {
        val timeline = calculateWeightTimeline(emptyList(), today = LocalDate.of(2026, 9, 1))
        assertTrue(timeline.isEmpty())

        val stats = calculateWeightSummaryStats(emptyList(), timeline)
        assertNull(stats.latestWeight)
        assertNull(stats.startingWeight)
        assertNull(stats.weightChange)
        assertEquals(0, stats.totalLoggedDays)
    }

    @Test
    fun testSingleWeightLogToday() {
        val today = LocalDate.of(2026, 9, 1)
        val logs = listOf(
            WeightLogEntity(id = 1, date = "2026-09-01", weightKg = 75.5)
        )

        val timeline = calculateWeightTimeline(logs, today = today)
        assertEquals(1, timeline.size)
        assertEquals(LocalDate.of(2026, 9, 1), timeline[0].date)
        assertEquals(75.5, timeline[0].weightKg, 0.001)
        assertFalse(timeline[0].isCarriedForward)
        assertEquals("2026-09-01", timeline[0].sourceDateStr)

        val stats = calculateWeightSummaryStats(logs, timeline)
        assertEquals(75.5, stats.latestWeight!!, 0.001)
        assertEquals(75.5, stats.startingWeight!!, 0.001)
        assertEquals(0.0, stats.weightChange!!, 0.001)
        assertEquals(75.5, stats.minWeight!!, 0.001)
        assertEquals(75.5, stats.maxWeight!!, 0.001)
        assertEquals(1, stats.totalLoggedDays)
    }

    @Test
    fun testSingleWeightLogPastCarriedForwardToToday() {
        // Oldest entry 4 days ago
        val today = LocalDate.of(2026, 9, 5)
        val logs = listOf(
            WeightLogEntity(id = 1, date = "2026-09-01", weightKg = 80.0)
        )

        val timeline = calculateWeightTimeline(logs, today = today)
        // From 2026-09-01 to 2026-09-05 is 5 days inclusive
        assertEquals(5, timeline.size)

        // Day 1: Actual log
        assertEquals("2026-09-01", timeline[0].dateStr)
        assertEquals(80.0, timeline[0].weightKg, 0.001)
        assertFalse(timeline[0].isCarriedForward)

        // Days 2 to 5: Carried forward
        for (i in 1..4) {
            val day = timeline[i]
            assertEquals(80.0, day.weightKg, 0.001)
            assertTrue("Day $i should be carried forward", day.isCarriedForward)
            assertEquals("2026-09-01", day.sourceDateStr)
        }

        assertEquals("2026-09-05", timeline.last().dateStr)
    }

    @Test
    fun testWeightCarryForwardWithMultipleGaps() {
        val today = LocalDate.of(2026, 9, 7)
        val logs = listOf(
            WeightLogEntity(id = 1, date = "2026-09-01", weightKg = 82.0),
            WeightLogEntity(id = 2, date = "2026-09-03", weightKg = 81.5),
            WeightLogEntity(id = 3, date = "2026-09-06", weightKg = 80.5)
        )

        val timeline = calculateWeightTimeline(logs, today = today)
        // 2026-09-01 to 2026-09-07 inclusive = 7 days
        assertEquals(7, timeline.size)

        // Day 1: 2026-09-01 -> Logged 82.0
        assertEquals("2026-09-01", timeline[0].dateStr)
        assertEquals(82.0, timeline[0].weightKg, 0.001)
        assertFalse(timeline[0].isCarriedForward)

        // Day 2: 2026-09-02 -> Carried forward 82.0 from Sep 01
        assertEquals("2026-09-02", timeline[1].dateStr)
        assertEquals(82.0, timeline[1].weightKg, 0.001)
        assertTrue(timeline[1].isCarriedForward)
        assertEquals("2026-09-01", timeline[1].sourceDateStr)

        // Day 3: 2026-09-03 -> Logged 81.5
        assertEquals("2026-09-03", timeline[2].dateStr)
        assertEquals(81.5, timeline[2].weightKg, 0.001)
        assertFalse(timeline[2].isCarriedForward)

        // Day 4: 2026-09-04 -> Carried forward 81.5 from Sep 03
        assertEquals("2026-09-04", timeline[3].dateStr)
        assertEquals(81.5, timeline[3].weightKg, 0.001)
        assertTrue(timeline[3].isCarriedForward)
        assertEquals("2026-09-03", timeline[3].sourceDateStr)

        // Day 5: 2026-09-05 -> Carried forward 81.5 from Sep 03
        assertEquals("2026-09-05", timeline[4].dateStr)
        assertEquals(81.5, timeline[4].weightKg, 0.001)
        assertTrue(timeline[4].isCarriedForward)
        assertEquals("2026-09-03", timeline[4].sourceDateStr)

        // Day 6: 2026-09-06 -> Logged 80.5
        assertEquals("2026-09-06", timeline[5].dateStr)
        assertEquals(80.5, timeline[5].weightKg, 0.001)
        assertFalse(timeline[5].isCarriedForward)

        // Day 7: 2026-09-07 -> Carried forward 80.5 from Sep 06
        assertEquals("2026-09-07", timeline[6].dateStr)
        assertEquals(80.5, timeline[6].weightKg, 0.001)
        assertTrue(timeline[6].isCarriedForward)
        assertEquals("2026-09-06", timeline[6].sourceDateStr)

        // Verify summary statistics
        val stats = calculateWeightSummaryStats(logs, timeline)
        assertEquals(82.0, stats.startingWeight!!, 0.001)
        assertEquals(80.5, stats.latestWeight!!, 0.001)
        assertEquals(-1.5, stats.weightChange!!, 0.001)
        assertEquals(80.5, stats.minWeight!!, 0.001)
        assertEquals(82.0, stats.maxWeight!!, 0.001)
        assertEquals(3, stats.totalLoggedDays)
    }

    @Test
    fun testWeightGainStatsCalculation() {
        val today = LocalDate.of(2026, 9, 3)
        val logs = listOf(
            WeightLogEntity(id = 1, date = "2026-09-01", weightKg = 70.0),
            WeightLogEntity(id = 2, date = "2026-09-02", weightKg = 71.2),
            WeightLogEntity(id = 3, date = "2026-09-03", weightKg = 72.5)
        )

        val timeline = calculateWeightTimeline(logs, today = today)
        val stats = calculateWeightSummaryStats(logs, timeline)

        assertEquals(70.0, stats.startingWeight!!, 0.001)
        assertEquals(72.5, stats.latestWeight!!, 0.001)
        assertEquals(2.5, stats.weightChange!!, 0.001)
        assertEquals(70.0, stats.minWeight!!, 0.001)
        assertEquals(72.5, stats.maxWeight!!, 0.001)
    }

    @Test
    fun testExportAndImportWeightLogsBundleJson() {
        val originalWeightLogs = listOf(
            WeightLogExport(date = "2026-08-20", weightKg = 75.0, timestamp = 1000L),
            WeightLogExport(date = "2026-08-25", weightKg = 74.2, timestamp = 2000L)
        )

        val bundle = HistoryBundleExportJson(
            version = 3,
            app = "FitFlow",
            exportedAt = 3000L,
            logs = emptyList(),
            weightLogs = originalWeightLogs
        )

        val jsonString = bundle.toJsonString()
        val parsedBundle = HistoryBundleExportJson.fromJsonString(jsonString)

        assertEquals(3, parsedBundle.version)
        assertEquals("FitFlow", parsedBundle.app)
        assertEquals(2, parsedBundle.weightLogs.size)
        assertEquals("2026-08-20", parsedBundle.weightLogs[0].date)
        assertEquals(75.0, parsedBundle.weightLogs[0].weightKg, 0.001)
        assertEquals("2026-08-25", parsedBundle.weightLogs[1].date)
        assertEquals(74.2, parsedBundle.weightLogs[1].weightKg, 0.001)
    }

    @Test
    fun testTimeRangeFilters() {
        val today = LocalDate.of(2026, 9, 1)
        val logs = listOf(
            WeightLogEntity(id = 1, date = "2026-01-01", weightKg = 90.0),
            WeightLogEntity(id = 2, date = "2026-08-01", weightKg = 80.0),
            WeightLogEntity(id = 3, date = "2026-08-28", weightKg = 78.0),
            WeightLogEntity(id = 4, date = "2026-09-01", weightKg = 77.0)
        )

        val fullTimeline = calculateWeightTimeline(logs, today = today)
        assertTrue(fullTimeline.size > 200)

        // 1W -> 7 days
        val weekTimeline = com.fitflow.app.data.local.model.filterWeightTimeline(fullTimeline, com.fitflow.app.data.local.model.WeightTimeRange.WEEK_1, today)
        assertEquals(7, weekTimeline.size)
        assertEquals(LocalDate.of(2026, 8, 26), weekTimeline.first().date)
        assertEquals(LocalDate.of(2026, 9, 1), weekTimeline.last().date)

        // 1M -> 30 days
        val monthTimeline = com.fitflow.app.data.local.model.filterWeightTimeline(fullTimeline, com.fitflow.app.data.local.model.WeightTimeRange.MONTH_1, today)
        assertEquals(30, monthTimeline.size)

        // ALL -> Full timeline
        val allTimeline = com.fitflow.app.data.local.model.filterWeightTimeline(fullTimeline, com.fitflow.app.data.local.model.WeightTimeRange.ALL, today)
        assertEquals(fullTimeline.size, allTimeline.size)
    }

    @Test
    fun testUnitConversion() {
        val weightKg = 75.0
        val kgConverted = com.fitflow.app.data.local.model.WeightUnit.KG.fromKg(weightKg)
        val lbsConverted = com.fitflow.app.data.local.model.WeightUnit.LBS.fromKg(weightKg)

        assertEquals(75.0, kgConverted, 0.001)
        assertEquals(165.3, lbsConverted, 0.1) // 75 * 2.20462 = 165.3465 -> 165.3
    }

    @Test
    fun testMovingAverageCalculation() {
        val today = LocalDate.of(2026, 9, 5)
        val logs = listOf(
            WeightLogEntity(id = 1, date = "2026-09-01", weightKg = 80.0),
            WeightLogEntity(id = 2, date = "2026-09-02", weightKg = 82.0),
            WeightLogEntity(id = 3, date = "2026-09-03", weightKg = 84.0),
            WeightLogEntity(id = 4, date = "2026-09-04", weightKg = 82.0),
            WeightLogEntity(id = 5, date = "2026-09-05", weightKg = 82.0)
        )

        val timeline = calculateWeightTimeline(logs, today = today)
        val ma = com.fitflow.app.data.local.model.calculateMovingAverage(timeline, windowSize = 3)

        assertEquals(5, ma.size)
        assertEquals(80.0, ma[0], 0.001) // avg(80)
        assertEquals(81.0, ma[1], 0.001) // avg(80, 82)
        assertEquals(82.0, ma[2], 0.001) // avg(80, 82, 84)
        assertEquals(82.7, ma[3], 0.1)   // avg(82, 84, 82) = 82.666 -> 82.7
        assertEquals(82.7, ma[4], 0.1)   // avg(84, 82, 82) = 82.666 -> 82.7
    }
}
