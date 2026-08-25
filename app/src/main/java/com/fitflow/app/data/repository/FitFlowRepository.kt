package com.fitflow.app.data.repository

import com.fitflow.app.data.local.FitFlowDatabase
import com.fitflow.app.data.local.entity.DayAssignmentEntity
import com.fitflow.app.data.local.entity.ExerciseEntity
import com.fitflow.app.data.local.entity.TemplateEntity
import com.fitflow.app.data.local.entity.TemplateExerciseEntity
import com.fitflow.app.data.local.entity.WorkoutLogEntity
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
                timestamp = logWithEx.log.timestamp
            )
        }
        val bundle = HistoryBundleExportJson(
            version = 1,
            app = "FitFlow",
            exportedAt = System.currentTimeMillis(),
            logs = exportLogs
        )
        bundle.toJsonString()
    }

    private fun logLogExportWeight(weight: Double): Double = weight

    override suspend fun importHistoryFromJson(jsonString: String): Result<Int> = withContext(Dispatchers.IO) {
        try {
            val bundle = HistoryBundleExportJson.fromJsonString(jsonString)
            if (bundle.logs.isEmpty()) {
                return@withContext Result.failure(IllegalArgumentException("No history logs found in JSON file"))
            }

            var importedCount = 0
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
                    timestamp = exportLog.timestamp
                )

                workoutLogDao.insertWorkoutLog(logEntity)
                importedCount++
            }

            Result.success(importedCount)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun clearAllHistory() = withContext(Dispatchers.IO) {
        workoutLogDao.deleteAllLogs()
    }
}
