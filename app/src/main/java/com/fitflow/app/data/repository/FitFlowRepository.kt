package com.fitflow.app.data.repository

import com.fitflow.app.data.local.FitFlowDatabase
import com.fitflow.app.data.local.entity.DayAssignmentEntity
import com.fitflow.app.data.local.entity.ExerciseEntity
import com.fitflow.app.data.local.entity.FoodLogEntity
import com.fitflow.app.data.local.entity.TemplateEntity
import com.fitflow.app.data.local.entity.TemplateExerciseEntity
import com.fitflow.app.data.local.entity.WeightLogEntity
import com.fitflow.app.data.local.entity.WorkoutLogEntity
import com.fitflow.app.data.local.model.FoodLogExport
import com.fitflow.app.data.local.model.WeightLogExport
import com.fitflow.app.data.local.relation.DayWithTemplate
import com.fitflow.app.data.local.relation.TemplateWithExercises
import com.fitflow.app.data.local.relation.WorkoutLogWithExercise
import com.fitflow.app.data.local.model.TemplateBundleExportJson
import com.fitflow.app.data.local.model.TemplateExportExercise
import com.fitflow.app.data.local.model.TemplateExportJson
import com.fitflow.app.data.local.model.HistoryBundleExportJson
import com.fitflow.app.data.local.model.HistoryLogExport
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import kotlin.math.roundToInt

interface FitFlowRepository {
    // Exercises
    fun getAllExercises(): Flow<List<ExerciseEntity>>
    fun getExercisesByCategory(category: String): Flow<List<ExerciseEntity>>
    fun searchExercises(query: String): Flow<List<ExerciseEntity>>
    fun getExerciseByIdFlow(id: Long): Flow<ExerciseEntity?>
    suspend fun getExerciseById(id: Long): ExerciseEntity?
    suspend fun getExerciseByName(name: String): ExerciseEntity?
    suspend fun insertExercise(exercise: ExerciseEntity): Long
    suspend fun updateExercise(exercise: ExerciseEntity)
    suspend fun deleteExercise(exercise: ExerciseEntity)

    // Templates
    fun getAllTemplatesWithExercises(): Flow<List<TemplateWithExercises>>
    fun getTemplateWithExercises(id: Long): Flow<TemplateWithExercises?>
    suspend fun getTemplateWithExercisesOnce(id: Long): TemplateWithExercises?
    suspend fun getAllTemplatesWithExercisesOnce(): List<TemplateWithExercises>
    suspend fun getTemplateByName(name: String): TemplateEntity?
    suspend fun isTemplateNameUnique(name: String, excludeTemplateId: Long = 0L): Boolean
    suspend fun saveTemplateWithExercises(
        template: TemplateEntity,
        exercises: List<TemplateExerciseEntity>
    ): Long
    suspend fun deleteTemplate(template: TemplateEntity)
    suspend fun exportAllTemplatesToJson(): String
    suspend fun importTemplateBundleFromJson(jsonString: String): Result<Int>

    // Day Assignments / Schedule
    fun getAllDayAssignments(): Flow<List<DayWithTemplate>>
    fun getAssignmentForDay(dayOfWeek: Int): Flow<DayWithTemplate?>
    suspend fun getAssignmentForDayOnce(dayOfWeek: Int): DayWithTemplate?
    suspend fun assignTemplateToDay(dayOfWeek: Int, templateId: Long?)

    // Workout Logs
    fun getLogsForDate(date: String): Flow<List<WorkoutLogWithExercise>>
    fun getLogForExerciseFlow(date: String, exerciseId: Long): Flow<WorkoutLogEntity?>
    suspend fun getLogForExercise(date: String, exerciseId: Long): WorkoutLogEntity?
    suspend fun saveWorkoutLog(log: WorkoutLogEntity): Long
    suspend fun saveExerciseSets(
        date: String,
        templateId: Long?,
        exerciseId: Long,
        sets: List<com.fitflow.app.data.local.model.WorkoutSetRecord>,
        durationSeconds: Int = 0
    ): WorkoutLogEntity
    suspend fun toggleExerciseCompletion(
        date: String,
        templateId: Long?,
        exerciseId: Long,
        sets: Int,
        reps: Int,
        weight: Double,
        durationSeconds: Int = 0
    ): Boolean
    fun getAllLogs(): Flow<List<WorkoutLogWithExercise>>
    fun getCompletedLogs(): Flow<List<WorkoutLogWithExercise>>
    fun getCompletedDates(): Flow<List<String>>
    suspend fun exportHistoryToJson(): String
    suspend fun importHistoryFromJson(jsonString: String): Result<Int>
    suspend fun clearAllHistory()

    // Food Logs
    fun getFoodLogsForDate(date: String): Flow<List<FoodLogEntity>>
    suspend fun getFoodLogsForDateOnce(date: String): List<FoodLogEntity>
    fun getAllFoodLogs(): Flow<List<FoodLogEntity>>
    fun getDistinctFoodDates(): Flow<List<String>>
    fun getTotalCaloriesForDate(date: String): Flow<Int?>
    suspend fun insertFoodLog(foodLog: FoodLogEntity): Long
    suspend fun updateFoodLog(foodLog: FoodLogEntity)
    suspend fun deleteFoodLog(foodLog: FoodLogEntity)
    suspend fun deleteFoodLogById(id: Long)
    suspend fun deleteAllFoodLogs()

    // Weight Logs
    fun getAllWeightLogs(): Flow<List<WeightLogEntity>>
    suspend fun getAllWeightLogsOnce(): List<WeightLogEntity>
    fun getWeightLogForDate(date: String): Flow<WeightLogEntity?>
    suspend fun getWeightLogForDateOnce(date: String): WeightLogEntity?
    fun getLatestWeightLog(): Flow<WeightLogEntity?>
    suspend fun saveWeightLog(date: String, weightKg: Double): Long
    suspend fun deleteWeightLogByDate(date: String)
    suspend fun deleteAllWeightLogs()

    // Initializer
    suspend fun ensureSeeded()
}

class FitFlowRepositoryImpl(
    private val database: FitFlowDatabase
) : FitFlowRepository {

    private val exerciseDao = database.exerciseDao()
    private val templateDao = database.templateDao()
    private val dayAssignmentDao = database.dayAssignmentDao()
    private val workoutLogDao = database.workoutLogDao()
    private val foodLogDao = database.foodLogDao()
    private val weightLogDao = database.weightLogDao()

    override suspend fun ensureSeeded() = withContext(Dispatchers.IO) {
        FitFlowDatabase.populateDatabase(database)
    }

    // Exercises
    override fun getAllExercises(): Flow<List<ExerciseEntity>> =
        exerciseDao.getAllExercises()

    override fun getExercisesByCategory(category: String): Flow<List<ExerciseEntity>> =
        exerciseDao.getExercisesByCategory(category)

    override fun searchExercises(query: String): Flow<List<ExerciseEntity>> =
        exerciseDao.searchExercises(query)

    override fun getExerciseByIdFlow(id: Long): Flow<ExerciseEntity?> =
        exerciseDao.getExerciseByIdFlow(id)

    override suspend fun getExerciseById(id: Long): ExerciseEntity? = withContext(Dispatchers.IO) {
        exerciseDao.getExerciseById(id)
    }

    override suspend fun getExerciseByName(name: String): ExerciseEntity? = withContext(Dispatchers.IO) {
        exerciseDao.getExerciseByName(name)
    }

    override suspend fun insertExercise(exercise: ExerciseEntity): Long = withContext(Dispatchers.IO) {
        exerciseDao.insertExercise(exercise)
    }

    override suspend fun updateExercise(exercise: ExerciseEntity) = withContext(Dispatchers.IO) {
        exerciseDao.updateExercise(exercise)
    }

    override suspend fun deleteExercise(exercise: ExerciseEntity) = withContext(Dispatchers.IO) {
        exerciseDao.deleteExercise(exercise)
    }

    // Templates
    override fun getAllTemplatesWithExercises(): Flow<List<TemplateWithExercises>> =
        templateDao.getAllTemplatesWithExercises()

    override fun getTemplateWithExercises(id: Long): Flow<TemplateWithExercises?> =
        templateDao.getTemplateWithExercises(id)

    override suspend fun getTemplateWithExercisesOnce(id: Long): TemplateWithExercises? = withContext(Dispatchers.IO) {
        templateDao.getTemplateWithExercisesOnce(id)
    }

    override suspend fun getAllTemplatesWithExercisesOnce(): List<TemplateWithExercises> = withContext(Dispatchers.IO) {
        templateDao.getAllTemplatesWithExercisesOnce()
    }

    override suspend fun getTemplateByName(name: String): TemplateEntity? = withContext(Dispatchers.IO) {
        templateDao.getTemplateByName(name)
    }

    override suspend fun isTemplateNameUnique(name: String, excludeTemplateId: Long): Boolean = withContext(Dispatchers.IO) {
        val existing = templateDao.getTemplateByName(name)
        existing == null || (excludeTemplateId > 0L && existing.id == excludeTemplateId)
    }

    override suspend fun saveTemplateWithExercises(
        template: TemplateEntity,
        exercises: List<TemplateExerciseEntity>
    ): Long = withContext(Dispatchers.IO) {
        templateDao.saveTemplateWithExercises(template, exercises)
    }

    override suspend fun deleteTemplate(template: TemplateEntity) = withContext(Dispatchers.IO) {
        templateDao.deleteTemplateAndExercises(template)
    }

    override suspend fun exportAllTemplatesToJson(): String = withContext(Dispatchers.IO) {
        val allTemplates = templateDao.getAllTemplatesWithExercisesOnce()
        val exportTemplates = allTemplates.map { templateWithEx ->
            val exportExercises = templateWithEx.exercises
                .sortedBy { it.templateExercise.orderIndex }
                .map {
                    TemplateExportExercise(
                        exerciseName = it.exercise.name,
                        category = it.exercise.category,
                        targetSets = it.templateExercise.targetSets,
                        targetReps = it.templateExercise.targetReps,
                        targetDurationSeconds = it.templateExercise.targetDurationSeconds,
                        restTimeSeconds = it.templateExercise.restTimeSeconds,
                        isSprint = it.exercise.isSprint,
                        orderIndex = it.templateExercise.orderIndex
                    )
                }
            TemplateExportJson(
                templateName = templateWithEx.template.name,
                exercises = exportExercises
            )
        }
        val bundle = TemplateBundleExportJson(
            version = 1,
            app = "FitFlow",
            exportedAt = System.currentTimeMillis(),
            templates = exportTemplates
        )
        bundle.toJsonString()
    }

    override suspend fun importTemplateBundleFromJson(jsonString: String): Result<Int> = withContext(Dispatchers.IO) {
        try {
            val bundle = TemplateBundleExportJson.fromJsonString(jsonString)
            if (bundle.templates.isEmpty()) {
                return@withContext Result.failure(IllegalArgumentException("No templates found in JSON file"))
            }

            var importedCount = 0
            val skippedDuplicates = mutableListOf<String>()

            for (templateExport in bundle.templates) {
                val templateName = templateExport.templateName.trim()
                if (templateName.isBlank()) continue
                if (templateExport.exercises.isEmpty()) continue

                // Check uniqueness: if already exists, skip
                if (!isTemplateNameUnique(templateName)) {
                    skippedDuplicates.add(templateName)
                    continue
                }

                val templateEntity = TemplateEntity(
                    name = templateName,
                    createdDate = System.currentTimeMillis()
                )

                val templateExerciseEntities = mutableListOf<TemplateExerciseEntity>()

                templateExport.exercises.forEachIndexed { index, exportEx ->
                    val trimmedExerciseName = exportEx.exerciseName.trim()
                    if (trimmedExerciseName.isNotBlank()) {
                        val existingExercise = exerciseDao.getExerciseByName(trimmedExerciseName)
                        val exerciseId = if (existingExercise != null) {
                            existingExercise.id
                        } else {
                            val newExercise = ExerciseEntity(
                                name = trimmedExerciseName,
                                category = exportEx.category.ifBlank { "General" },
                                defaultSets = exportEx.targetSets,
                                defaultReps = exportEx.targetReps,
                                isCustom = true,
                                isSprint = exportEx.isSprint,
                                defaultDurationSeconds = exportEx.targetDurationSeconds
                            )
                            exerciseDao.insertExercise(newExercise)
                        }

                        templateExerciseEntities.add(
                            TemplateExerciseEntity(
                                templateId = 0L,
                                exerciseId = exerciseId,
                                targetSets = exportEx.targetSets,
                                targetReps = exportEx.targetReps,
                                targetDurationSeconds = exportEx.targetDurationSeconds,
                                restTimeSeconds = exportEx.restTimeSeconds,
                                orderIndex = index
                            )
                        )
                    }
                }

                if (templateExerciseEntities.isNotEmpty()) {
                    templateDao.saveTemplateWithExercises(templateEntity, templateExerciseEntities)
                    importedCount++
                }
            }

            if (importedCount == 0 && skippedDuplicates.isNotEmpty()) {
                return@withContext Result.failure(
                    IllegalArgumentException("All templates already exist: ${skippedDuplicates.joinToString(", ")}")
                )
            }

            Result.success(importedCount)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Day Assignments
    override fun getAllDayAssignments(): Flow<List<DayWithTemplate>> =
        dayAssignmentDao.getAllDayAssignmentsWithTemplate()

    override fun getAssignmentForDay(dayOfWeek: Int): Flow<DayWithTemplate?> =
        dayAssignmentDao.getAssignmentForDay(dayOfWeek)

    override suspend fun getAssignmentForDayOnce(dayOfWeek: Int): DayWithTemplate? = withContext(Dispatchers.IO) {
        dayAssignmentDao.getAssignmentForDayOnce(dayOfWeek)
    }

    override suspend fun assignTemplateToDay(dayOfWeek: Int, templateId: Long?) = withContext(Dispatchers.IO) {
        dayAssignmentDao.updateTemplateForDay(dayOfWeek, templateId)
    }

    // Workout Logs
    override fun getLogsForDate(date: String): Flow<List<WorkoutLogWithExercise>> =
        workoutLogDao.getLogsForDate(date)

    override fun getLogForExerciseFlow(date: String, exerciseId: Long): Flow<WorkoutLogEntity?> =
        workoutLogDao.getLogForExerciseFlow(date, exerciseId)

    override suspend fun getLogForExercise(date: String, exerciseId: Long): WorkoutLogEntity? = withContext(Dispatchers.IO) {
        workoutLogDao.getLogForExercise(date, exerciseId)
    }

    override suspend fun saveWorkoutLog(log: WorkoutLogEntity): Long = withContext(Dispatchers.IO) {
        workoutLogDao.upsertWorkoutLog(log)
    }

    override suspend fun saveExerciseSets(
        date: String,
        templateId: Long?,
        exerciseId: Long,
        sets: List<com.fitflow.app.data.local.model.WorkoutSetRecord>,
        durationSeconds: Int
    ): WorkoutLogEntity = withContext(Dispatchers.IO) {
        val existing = workoutLogDao.getLogForExercise(date, exerciseId)
        val setsJson = com.fitflow.app.data.local.model.WorkoutSetRecord.serializeSetsToJson(sets)
        val allDone = sets.isNotEmpty() && sets.all { it.isCompleted }
        val maxReps = sets.maxOfOrNull { it.reps } ?: 0
        val maxWeight = sets.maxOfOrNull { it.weight } ?: 0.0

        val logToSave = (existing ?: WorkoutLogEntity(
            date = date,
            templateId = templateId,
            exerciseId = exerciseId,
            actualSets = sets.size,
            actualReps = maxReps,
            actualWeight = maxWeight,
            actualDurationSeconds = durationSeconds,
            isCompleted = allDone,
            setsDataJson = setsJson
        )).copy(
            actualSets = sets.size,
            actualReps = if (sets.isNotEmpty()) sets.last().reps else maxReps,
            actualWeight = if (sets.isNotEmpty()) sets.last().weight else maxWeight,
            actualDurationSeconds = durationSeconds,
            isCompleted = allDone,
            setsDataJson = setsJson,
            timestamp = System.currentTimeMillis()
        )

        workoutLogDao.upsertWorkoutLog(logToSave)
        logToSave
    }

    override suspend fun toggleExerciseCompletion(
        date: String,
        templateId: Long?,
        exerciseId: Long,
        sets: Int,
        reps: Int,
        weight: Double,
        durationSeconds: Int
    ): Boolean = withContext(Dispatchers.IO) {
        val existing = workoutLogDao.getLogForExercise(date, exerciseId)
        val newStatus = !(existing?.isCompleted ?: false)
        val logToSave = (existing ?: WorkoutLogEntity(
            date = date,
            templateId = templateId,
            exerciseId = exerciseId,
            actualSets = sets,
            actualReps = reps,
            actualWeight = weight,
            actualDurationSeconds = durationSeconds,
            isCompleted = false
        )).copy(
            actualSets = sets,
            actualReps = reps,
            actualWeight = weight,
            actualDurationSeconds = durationSeconds,
            isCompleted = newStatus,
            timestamp = System.currentTimeMillis()
        )
        workoutLogDao.upsertWorkoutLog(logToSave)
        newStatus
    }

    override fun getAllLogs(): Flow<List<WorkoutLogWithExercise>> =
        workoutLogDao.getAllLogs()

    override fun getCompletedLogs(): Flow<List<WorkoutLogWithExercise>> =
        workoutLogDao.getCompletedLogs()

    override fun getCompletedDates(): Flow<List<String>> =
        workoutLogDao.getCompletedWorkoutDates()

    override suspend fun exportHistoryToJson(): String = withContext(Dispatchers.IO) {
        val allLogs = workoutLogDao.getAllLogsOnce()
        val exportLogs = allLogs.map { logWithEx ->
            HistoryLogExport(
                date = logWithEx.log.date,
                exerciseName = logWithEx.exercise.name,
                category = logWithEx.exercise.category,
                actualSets = logWithEx.log.actualSets,
                actualReps = logWithEx.log.actualReps,
                actualWeight = logLogExportWeight(logWithEx.log.actualWeight),
                actualDurationSeconds = logWithEx.log.actualDurationSeconds,
                isCompleted = logWithEx.log.isCompleted,
                isSprint = logWithEx.exercise.isSprint,
                setsDataJson = logWithEx.log.setsDataJson,
                timestamp = logWithEx.log.timestamp
            )
        }

        val allFoodLogs = foodLogDao.getAllFoodLogsOnce()
        val exportFoodLogs = allFoodLogs.map { food ->
            FoodLogExport(
                date = food.date,
                foodName = food.foodName,
                quantity = food.quantity,
                unit = food.unit,
                calories = food.calories,
                mealTime = food.mealTime,
                timestamp = food.timestamp
            )
        }

        val allWeightLogs = weightLogDao.getAllWeightLogsOnce()
        val exportWeightLogs = allWeightLogs.map { weightLog ->
            WeightLogExport(
                date = weightLog.date,
                weightKg = weightLog.weightKg,
                timestamp = weightLog.timestamp
            )
        }

        val bundle = HistoryBundleExportJson(
            version = 3,
            app = "FitFlow",
            exportedAt = System.currentTimeMillis(),
            logs = exportLogs,
            foodLogs = exportFoodLogs,
            weightLogs = exportWeightLogs
        )
        bundle.toJsonString()
    }

    private fun logLogExportWeight(weight: Double): Double = weight

    override suspend fun importHistoryFromJson(jsonString: String): Result<Int> = withContext(Dispatchers.IO) {
        try {
            val bundle = HistoryBundleExportJson.fromJsonString(jsonString)
            if (bundle.logs.isEmpty() && bundle.foodLogs.isEmpty() && bundle.weightLogs.isEmpty()) {
                return@withContext Result.failure(IllegalArgumentException("No history, food logs, or weight logs found in JSON file"))
            }

            var importedCount = 0

            // Import Workout Logs
            for (exportLog in bundle.logs) {
                val exerciseName = exportLog.exerciseName.trim()
                if (exerciseName.isBlank()) continue

                val existingExercise = exerciseDao.getExerciseByName(exerciseName)
                val exerciseId = if (existingExercise != null) {
                    existingExercise.id
                } else {
                    val newExercise = ExerciseEntity(
                        name = exerciseName,
                        category = exportLog.category,
                        isCustom = true,
                        isSprint = exportLog.isSprint
                    )
                    exerciseDao.insertExercise(newExercise)
                }

                val logEntity = WorkoutLogEntity(
                    date = exportLog.date,
                    exerciseId = exerciseId,
                    actualSets = exportLog.actualSets,
                    actualReps = exportLog.actualReps,
                    actualWeight = exportLog.actualWeight,
                    actualDurationSeconds = exportLog.actualDurationSeconds,
                    isCompleted = exportLog.isCompleted,
                    setsDataJson = exportLog.setsDataJson,
                    timestamp = exportLog.timestamp
                )

                workoutLogDao.insertWorkoutLog(logEntity)
                importedCount++
            }

            // Import Food Logs
            for (exportFood in bundle.foodLogs) {
                val foodName = exportFood.foodName.trim()
                if (foodName.isBlank() || exportFood.date.isBlank()) continue

                val foodEntity = FoodLogEntity(
                    date = exportFood.date,
                    foodName = foodName,
                    quantity = exportFood.quantity,
                    unit = exportFood.unit,
                    calories = exportFood.calories,
                    mealTime = exportFood.mealTime,
                    timestamp = exportFood.timestamp
                )

                foodLogDao.insertFoodLog(foodEntity)
                importedCount++
            }

            // Import Weight Logs
            for (exportWeight in bundle.weightLogs) {
                if (exportWeight.date.isBlank() || exportWeight.weightKg <= 0.0) continue

                val weightEntity = WeightLogEntity(
                    date = exportWeight.date,
                    weightKg = exportWeight.weightKg,
                    timestamp = exportWeight.timestamp
                )

                weightLogDao.insertOrUpdateWeightLog(weightEntity)
                importedCount++
            }

            Result.success(importedCount)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun clearAllHistory() = withContext(Dispatchers.IO) {
        workoutLogDao.deleteAllLogs()
        foodLogDao.deleteAllFoodLogs()
        weightLogDao.deleteAllWeightLogs()
    }

    // Food Logs
    override fun getFoodLogsForDate(date: String): Flow<List<FoodLogEntity>> =
        foodLogDao.getFoodLogsForDate(date)

    override suspend fun getFoodLogsForDateOnce(date: String): List<FoodLogEntity> = withContext(Dispatchers.IO) {
        foodLogDao.getFoodLogsForDateOnce(date)
    }

    override fun getAllFoodLogs(): Flow<List<FoodLogEntity>> =
        foodLogDao.getAllFoodLogs()

    override fun getDistinctFoodDates(): Flow<List<String>> =
        foodLogDao.getDistinctFoodDates()

    override fun getTotalCaloriesForDate(date: String): Flow<Int?> =
        foodLogDao.getTotalCaloriesForDate(date)

    override suspend fun insertFoodLog(foodLog: FoodLogEntity): Long = withContext(Dispatchers.IO) {
        foodLogDao.insertFoodLog(foodLog)
    }

    override suspend fun updateFoodLog(foodLog: FoodLogEntity) = withContext(Dispatchers.IO) {
        foodLogDao.updateFoodLog(foodLog)
    }

    override suspend fun deleteFoodLog(foodLog: FoodLogEntity) = withContext(Dispatchers.IO) {
        foodLogDao.deleteFoodLog(foodLog)
    }

    override suspend fun deleteFoodLogById(id: Long) = withContext(Dispatchers.IO) {
        foodLogDao.deleteFoodLogById(id)
    }

    override suspend fun deleteAllFoodLogs() = withContext(Dispatchers.IO) {
        foodLogDao.deleteAllFoodLogs()
    }

    // Weight Logs
    override fun getAllWeightLogs(): Flow<List<WeightLogEntity>> =
        weightLogDao.getAllWeightLogs()

    override suspend fun getAllWeightLogsOnce(): List<WeightLogEntity> = withContext(Dispatchers.IO) {
        weightLogDao.getAllWeightLogsOnce()
    }

    override fun getWeightLogForDate(date: String): Flow<WeightLogEntity?> =
        weightLogDao.getWeightLogByDate(date)

    override suspend fun getWeightLogForDateOnce(date: String): WeightLogEntity? = withContext(Dispatchers.IO) {
        weightLogDao.getWeightLogByDateOnce(date)
    }

    override fun getLatestWeightLog(): Flow<WeightLogEntity?> =
        weightLogDao.getLatestWeightLog()

    override suspend fun saveWeightLog(date: String, weightKg: Double): Long = withContext(Dispatchers.IO) {
        val roundedWeight = ((weightKg * 10.0).roundToInt()) / 10.0
        val entity = WeightLogEntity(
            date = date.trim(),
            weightKg = roundedWeight,
            timestamp = System.currentTimeMillis()
        )
        weightLogDao.insertOrUpdateWeightLog(entity)
    }

    override suspend fun deleteWeightLogByDate(date: String) = withContext(Dispatchers.IO) {
        weightLogDao.deleteWeightLogByDate(date.trim())
    }

    override suspend fun deleteAllWeightLogs() = withContext(Dispatchers.IO) {
        weightLogDao.deleteAllWeightLogs()
    }
}
