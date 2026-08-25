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
    suspend fun insertExercise(exercise: ExerciseEntity): Long
    suspend fun updateExercise(exercise: ExerciseEntity)
    suspend fun deleteExercise(exercise: ExerciseEntity)

    // Templates
    fun getAllTemplatesWithExercises(): Flow<List<TemplateWithExercises>>
    fun getTemplateWithExercises(id: Long): Flow<TemplateWithExercises?>
    suspend fun getTemplateWithExercisesOnce(id: Long): TemplateWithExercises?
    suspend fun saveTemplateWithExercises(
        template: TemplateEntity,
        exercises: List<TemplateExerciseEntity>
    ): Long
    suspend fun deleteTemplate(template: TemplateEntity)

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

    override suspend fun saveTemplateWithExercises(
        template: TemplateEntity,
        exercises: List<TemplateExerciseEntity>
    ): Long = withContext(Dispatchers.IO) {
        templateDao.saveTemplateWithExercises(template, exercises)
    }

    override suspend fun deleteTemplate(template: TemplateEntity) = withContext(Dispatchers.IO) {
        templateDao.deleteTemplateAndExercises(template)
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
}
