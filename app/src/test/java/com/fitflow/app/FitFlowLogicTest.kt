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

    @Test
    fun testTemplateBundleExportJsonSerializationAndDeserialization() {
        val exportExercises1 = listOf(
            com.fitflow.app.data.local.model.TemplateExportExercise(
                exerciseName = "Barbell Bench Press",
                category = "Chest",
                targetSets = 4,
                targetReps = 8,
                targetDurationSeconds = 30,
                restTimeSeconds = 90,
                isSprint = false,
                orderIndex = 0
            )
        )
        val exportExercises2 = listOf(
            com.fitflow.app.data.local.model.TemplateExportExercise(
                exerciseName = "Deadlift",
                category = "Back",
                targetSets = 3,
                targetReps = 6,
                targetDurationSeconds = 30,
                restTimeSeconds = 120,
                isSprint = false,
                orderIndex = 0
            )
        )

        val bundle = com.fitflow.app.data.local.model.TemplateBundleExportJson(
            version = 1,
            app = "FitFlow",
            templates = listOf(
                com.fitflow.app.data.local.model.TemplateExportJson(
                    templateName = "Chest Day",
                    exercises = exportExercises1
                ),
                com.fitflow.app.data.local.model.TemplateExportJson(
                    templateName = "Back Day",
                    exercises = exportExercises2
                )
            )
        )

        val jsonString = bundle.toJsonString()
        assertTrue(jsonString.contains("Chest Day"))
        assertTrue(jsonString.contains("Back Day"))

        val parsed = com.fitflow.app.data.local.model.TemplateBundleExportJson.fromJsonString(jsonString)
        assertEquals(2, parsed.templates.size)
        assertEquals("Chest Day", parsed.templates[0].templateName)
        assertEquals("Back Day", parsed.templates[1].templateName)
        assertEquals("Barbell Bench Press", parsed.templates[0].exercises[0].exerciseName)
        assertEquals("Deadlift", parsed.templates[1].exercises[0].exerciseName)
    }

    @Test
    fun testTemplateBundleBackwardCompatibilitySingleTemplateJson() {
        val singleJson = """
            {
              "templateName": "Full Body Power",
              "exercises": [
                {
                  "exerciseName": "Squat",
                  "category": "Legs",
                  "targetSets": 5,
                  "targetReps": 5
                }
              ]
            }
        """.trimIndent()

        val parsed = com.fitflow.app.data.local.model.TemplateBundleExportJson.fromJsonString(singleJson)
        assertEquals(1, parsed.templates.size)
        assertEquals("Full Body Power", parsed.templates[0].templateName)
        assertEquals(1, parsed.templates[0].exercises.size)
        assertEquals("Squat", parsed.templates[0].exercises[0].exerciseName)
    }

    @Test
    fun testTemplateNameUniquenessRule() {
        val existingNames = listOf("Push Day", "Pull Day", "Leg Day")

        fun isUnique(name: String): Boolean {
            val trimmed = name.trim().lowercase()
            return existingNames.none { it.trim().lowercase() == trimmed }
        }

        assertFalse(isUnique("Push Day"))
        assertFalse(isUnique("  push day  "))
        assertFalse(isUnique("PULL DAY"))
        assertTrue(isUnique("Upper Body Power"))
    }

    @Test
    fun testFoodLogEntityAndCalorieCalculations() {
        val foodLogs = listOf(
            com.fitflow.app.data.local.entity.FoodLogEntity(
                id = 1,
                date = "2026-08-31",
                foodName = "Oatmeal with Honey",
                quantity = 150.0,
                unit = "g",
                calories = 350,
                mealTime = "Breakfast"
            ),
            com.fitflow.app.data.local.entity.FoodLogEntity(
                id = 2,
                date = "2026-08-31",
                foodName = "Candy Bar",
                quantity = 1.0,
                unit = "candy",
                calories = 220,
                mealTime = "Snack"
            ),
            com.fitflow.app.data.local.entity.FoodLogEntity(
                id = 3,
                date = "2026-08-31",
                foodName = "Grilled Chicken Breast",
                quantity = 250.0,
                unit = "g",
                calories = 410,
                mealTime = "Lunch"
            ),
            com.fitflow.app.data.local.entity.FoodLogEntity(
                id = 4,
                date = "2026-08-31",
                foodName = "Protein Shake with Milk",
                quantity = 500.0,
                unit = "ml",
                calories = 380,
                mealTime = "Post-Workout"
            )
        )

        val totalCalories = foodLogs.sumOf { it.calories }
        assertEquals(1360, totalCalories)

        val breakfastLogs = foodLogs.filter { it.mealTime == "Breakfast" }
        assertEquals(1, breakfastLogs.size)
        assertEquals("Oatmeal with Honey", breakfastLogs[0].foodName)
        assertEquals("150.0 g", "${breakfastLogs[0].quantity} ${breakfastLogs[0].unit}")

        val candyLog = foodLogs.first { it.unit == "candy" }
        assertEquals(1.0, candyLog.quantity, 0.001)
        assertEquals("candy", candyLog.unit)
        assertEquals(220, candyLog.calories)
    }

    @Test
    fun testFoodLogsDateGrouping() {
        val foodLogs = listOf(
            com.fitflow.app.data.local.entity.FoodLogEntity(id = 1, date = "2026-08-30", foodName = "Apple", quantity = 1.0, unit = "piece", calories = 95, mealTime = "Snack"),
            com.fitflow.app.data.local.entity.FoodLogEntity(id = 2, date = "2026-08-31", foodName = "Banana", quantity = 2.0, unit = "unit", calories = 210, mealTime = "Snack"),
            com.fitflow.app.data.local.entity.FoodLogEntity(id = 3, date = "2026-08-31", foodName = "Rice & Beans", quantity = 300.0, unit = "g", calories = 450, mealTime = "Dinner")
        )

        val grouped = foodLogs.groupBy { it.date }
        assertEquals(2, grouped.keys.size)
        assertEquals(1, grouped["2026-08-30"]?.size)
        assertEquals(2, grouped["2026-08-31"]?.size)
        assertEquals(660, grouped["2026-08-31"]?.sumOf { it.calories })
    }

    @Test
    fun testHistoryBundleExportAndImportWithFoodLogs() {
        val bundle = com.fitflow.app.data.local.model.HistoryBundleExportJson(
            version = 2,
            app = "FitFlow",
            logs = listOf(
                com.fitflow.app.data.local.model.HistoryLogExport(
                    date = "2026-08-31",
                    exerciseName = "Barbell Squat",
                    category = "Legs",
                    actualSets = 4,
                    actualReps = 8
                )
            ),
            foodLogs = listOf(
                com.fitflow.app.data.local.model.FoodLogExport(
                    date = "2026-08-31",
                    foodName = "Grilled Chicken",
                    quantity = 200.0,
                    unit = "g",
                    calories = 330,
                    mealTime = "Lunch"
                ),
                com.fitflow.app.data.local.model.FoodLogExport(
                    date = "2026-08-31",
                    foodName = "Candy",
                    quantity = 1.0,
                    unit = "candy",
                    calories = 150,
                    mealTime = "Snack"
                )
            )
        )

        val jsonString = bundle.toJsonString()
        assertTrue(jsonString.contains("Barbell Squat"))
        assertTrue(jsonString.contains("Grilled Chicken"))
        assertTrue(jsonString.contains("candy"))

        val parsed = com.fitflow.app.data.local.model.HistoryBundleExportJson.fromJsonString(jsonString)
        assertEquals(1, parsed.logs.size)
        assertEquals("Barbell Squat", parsed.logs[0].exerciseName)
        assertEquals(2, parsed.foodLogs.size)
        assertEquals("Grilled Chicken", parsed.foodLogs[0].foodName)
        assertEquals(330, parsed.foodLogs[0].calories)
        assertEquals("candy", parsed.foodLogs[1].unit)
    }

    @Test
    fun testHistoryBundleBackwardCompatibilityWithVersion1() {
        // v1 JSON without foodLogs array
        val v1Json = """
            {
              "version": 1,
              "app": "FitFlow",
              "exportedAt": 1725000000000,
              "logs": [
                {
                  "date": "2026-08-31",
                  "exerciseName": "Bench Press",
                  "category": "Chest",
                  "actualSets": 3,
                  "actualReps": 10,
                  "actualWeight": 80.0,
                  "actualDurationSeconds": 0,
                  "isCompleted": true,
                  "isSprint": false,
                  "timestamp": 1725000000000
                }
              ]
            }
        """.trimIndent()

        val parsed = com.fitflow.app.data.local.model.HistoryBundleExportJson.fromJsonString(v1Json)
        assertEquals(1, parsed.logs.size)
        assertEquals("Bench Press", parsed.logs[0].exerciseName)
        assertTrue(parsed.foodLogs.isEmpty())
    }

    @Test
    fun testAppThemeModes() {
        val emerald = com.fitflow.app.ui.theme.AppThemeMode.EMERALD
        val spiderman = com.fitflow.app.ui.theme.AppThemeMode.SPIDERMAN

        assertEquals("Electric Emerald", emerald.displayName)
        assertEquals("Spider-Man Edition", spiderman.displayName)
        assertEquals("EMERALD", emerald.name)
        assertEquals("SPIDERMAN", spiderman.name)
    }
}
