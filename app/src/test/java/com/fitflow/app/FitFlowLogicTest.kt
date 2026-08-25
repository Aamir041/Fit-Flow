package com.fitflow.app

import com.fitflow.app.data.local.entity.DayAssignmentEntity
import com.fitflow.app.data.local.entity.ExerciseEntity
import com.fitflow.app.data.local.entity.TemplateEntity
import com.fitflow.app.data.local.entity.TemplateExerciseEntity
import com.fitflow.app.data.local.entity.WorkoutLogEntity
import com.fitflow.app.ui.home.ExerciseLogItem
import com.fitflow.app.ui.templates.EditableExerciseItem
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.DayOfWeek
import java.time.LocalDate

class FitFlowLogicTest {

    @Test
    fun testDayOfWeekValuesMatchJavaTime() {
        assertEquals(1, DayOfWeek.MONDAY.value)
        assertEquals(2, DayOfWeek.TUESDAY.value)
        assertEquals(3, DayOfWeek.WEDNESDAY.value)
        assertEquals(4, DayOfWeek.THURSDAY.value)
        assertEquals(5, DayOfWeek.FRIDAY.value)
        assertEquals(6, DayOfWeek.SATURDAY.value)
        assertEquals(7, DayOfWeek.SUNDAY.value)
    }

    @Test
    fun testDayAssignmentResolution() {
        val pushDayTemplateId = 100L
        val pullDayTemplateId = 200L

        val assignments = listOf(
            DayAssignmentEntity(id = 1, dayOfWeek = 1, templateId = pushDayTemplateId),
            DayAssignmentEntity(id = 2, dayOfWeek = 2, templateId = pullDayTemplateId),
            DayAssignmentEntity(id = 3, dayOfWeek = 3, templateId = null) // Rest day
        )

        val assignmentsMap = assignments.associateBy { it.dayOfWeek }

        assertEquals(pushDayTemplateId, assignmentsMap[1]?.templateId)
        assertEquals(pullDayTemplateId, assignmentsMap[2]?.templateId)
        assertNull(assignmentsMap[3]?.templateId)
    }

    @Test
    fun testWorkoutProgressCalculation() {
        val exercises = listOf(
            ExerciseLogItem(1, 1, "Bench Press", "Chest", 4, 8, 90, 4, 8, 80.0, true),
            ExerciseLogItem(2, 2, "Incline Press", "Chest", 3, 10, 90, 3, 10, 30.0, true),
            ExerciseLogItem(3, 3, "Overhead Press", "Shoulders", 3, 8, 90, 3, 8, 50.0, false),
            ExerciseLogItem(4, 4, "Lateral Raise", "Shoulders", 4, 15, 60, 4, 15, 12.0, false)
        )

        val completedCount = exercises.count { it.isCompleted }
        val totalCount = exercises.size
        val progressPercent = completedCount.toFloat() / totalCount.toFloat()

        assertEquals(2, completedCount)
        assertEquals(4, totalCount)
        assertEquals(0.5f, progressPercent, 0.001f)
    }

    @Test
    fun testTotalVolumeCalculation() {
        val logs = listOf(
            WorkoutLogEntity(id = 1, date = "2026-08-22", exerciseId = 1, actualSets = 4, actualReps = 8, actualWeight = 100.0, isCompleted = true),
            WorkoutLogEntity(id = 2, date = "2026-08-22", exerciseId = 2, actualSets = 3, actualReps = 10, actualWeight = 50.0, isCompleted = true)
        )

        val totalVolume = logs.sumOf { it.actualSets * it.actualReps * it.actualWeight }
        // (4 * 8 * 100.0) + (3 * 10 * 50.0) = 3200 + 1500 = 4700.0
        assertEquals(4700.0, totalVolume, 0.001)
    }

    @Test
    fun testTemplateValidationRules() {
        // Rule 1: Name cannot be blank
        val emptyName = "   "
        assertTrue(emptyName.trim().isBlank())

        // Rule 2: Non-empty name is valid
        val validName = "Leg Destroyer Split"
        assertTrue(validName.trim().isNotBlank())

        // Rule 3: Exercises list must not be empty
        val emptyExercises = emptyList<EditableExerciseItem>()
        assertTrue(emptyExercises.isEmpty())

        val validExercises = listOf(
            EditableExerciseItem(1, "Squat", "Legs", 4, 8, 120)
        )
        assertFalse(validExercises.isEmpty())
    }

    @Test
    fun testReorderingExercises() {
        val initialList = mutableListOf("Squat", "Bench Press", "Deadlift")
        // Move Bench Press (index 1) up to index 0
        val item = initialList.removeAt(1)
        initialList.add(0, item)

        assertEquals(listOf("Bench Press", "Squat", "Deadlift"), initialList)
    }

    @Test
    fun testCustomExerciseCreationAndMapping() {
        val customExercise = ExerciseEntity(
            id = 55L,
            name = "Incline Cable Curl",
            category = "Forearms",
            defaultSets = 4,
            defaultReps = 12,
            isCustom = true
        )

        // Mapping custom exercise to a template item
        val templateExerciseItem = EditableExerciseItem(
            exerciseId = customExercise.id,
            name = customExercise.name,
            category = customExercise.category,
            targetSets = customExercise.defaultSets,
            targetReps = customExercise.defaultReps,
            restTimeSeconds = 60
        )

        assertEquals(55L, templateExerciseItem.exerciseId)
        assertEquals("Incline Cable Curl", templateExerciseItem.name)
        assertEquals("Forearms", templateExerciseItem.category)
        assertEquals(4, templateExerciseItem.targetSets)
        assertEquals(12, templateExerciseItem.targetReps)
        assertEquals(60, templateExerciseItem.restTimeSeconds)
        assertFalse(templateExerciseItem.isSprint)
    }

    @Test
    fun testSprintExerciseMappingAndDefaults() {
        val sprintExercise = ExerciseEntity(
            id = 101L,
            name = "100m Track Sprint",
            category = "Cardio",
            defaultSets = 0,
            defaultReps = 0,
            isCustom = false,
            isSprint = true,
            defaultDurationSeconds = 45
        )

        val templateItem = EditableExerciseItem(
            exerciseId = sprintExercise.id,
            name = sprintExercise.name,
            category = sprintExercise.category,
            targetSets = 0,
            targetReps = 0,
            restTimeSeconds = 0,
            isSprint = true,
            targetDurationSeconds = sprintExercise.defaultDurationSeconds
        )

        assertTrue(templateItem.isSprint)
        assertEquals(45, templateItem.targetDurationSeconds)
        assertEquals(0, templateItem.restTimeSeconds)

        val logItem = ExerciseLogItem(
            templateExerciseId = 1L,
            exerciseId = sprintExercise.id,
            name = sprintExercise.name,
            category = sprintExercise.category,
            targetSets = 0,
            targetReps = 0,
            restTimeSeconds = 0,
            actualSets = 0,
            actualReps = 0,
            actualWeight = 0.0,
            isCompleted = true,
            isSprint = true,
            targetDurationSeconds = 45,
            actualDurationSeconds = 50
        )

        assertTrue(logItem.isSprint)
        assertEquals(50, logItem.actualDurationSeconds)
        assertTrue(logItem.isCompleted)
    }

    @Test
    fun testVolumeCalculationExcludesSprints() {
        data class TestLogWithEx(
            val log: WorkoutLogEntity,
            val isSprint: Boolean
        )

        val logs = listOf(
            TestLogWithEx(
                log = WorkoutLogEntity(id = 1, date = "2026-08-22", exerciseId = 1, actualSets = 4, actualReps = 8, actualWeight = 100.0, isCompleted = true),
                isSprint = false
            ),
            TestLogWithEx(
                log = WorkoutLogEntity(id = 2, date = "2026-08-22", exerciseId = 2, actualSets = 3, actualReps = 10, actualWeight = 50.0, isCompleted = true),
                isSprint = false
            ),
            TestLogWithEx(
                log = WorkoutLogEntity(id = 3, date = "2026-08-22", exerciseId = 3, actualDurationSeconds = 60, actualWeight = 0.0, isCompleted = true),
                isSprint = true
            )
        )

        val totalVolume = logs.filter { !it.isSprint }.sumOf {
            it.log.actualSets * it.log.actualReps * it.log.actualWeight
        }
        // (4 * 8 * 100.0) + (3 * 10 * 50.0) = 3200 + 1500 = 4700.0, sprint is not counted
        assertEquals(4700.0, totalVolume, 0.001)
    }
}
