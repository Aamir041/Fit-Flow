package com.fitflow.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.fitflow.app.data.local.entity.WorkoutLogEntity
import com.fitflow.app.data.local.relation.WorkoutLogWithExercise
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkoutLogDao {

    @Transaction
    @Query("SELECT * FROM workout_logs WHERE date = :date ORDER BY timestamp ASC")
    fun getLogsForDate(date: String): Flow<List<WorkoutLogWithExercise>>

    @Transaction
    @Query("SELECT * FROM workout_logs WHERE date = :date ORDER BY timestamp ASC")
    suspend fun getLogsForDateOnce(date: String): List<WorkoutLogWithExercise>

    @Query("SELECT * FROM workout_logs WHERE date = :date AND exerciseId = :exerciseId LIMIT 1")
    suspend fun getLogForExercise(date: String, exerciseId: Long): WorkoutLogEntity?

    @Query("SELECT * FROM workout_logs WHERE date = :date AND exerciseId = :exerciseId LIMIT 1")
    fun getLogForExerciseFlow(date: String, exerciseId: Long): Flow<WorkoutLogEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkoutLog(log: WorkoutLogEntity): Long

    @Update
    suspend fun updateWorkoutLog(log: WorkoutLogEntity)

    @Transaction
    suspend fun upsertWorkoutLog(log: WorkoutLogEntity): Long {
        val existing = getLogForExercise(log.date, log.exerciseId)
        return if (existing != null) {
            val updated = log.copy(id = existing.id)
            updateWorkoutLog(updated)
            existing.id
        } else {
            insertWorkoutLog(log)
        }
    }

    @Query("DELETE FROM workout_logs WHERE date = :date AND exerciseId = :exerciseId")
    suspend fun deleteLogForExercise(date: String, exerciseId: Long)

    @Transaction
    @Query("SELECT * FROM workout_logs ORDER BY date DESC, timestamp DESC")
    fun getAllLogs(): Flow<List<WorkoutLogWithExercise>>

    @Transaction
    @Query("SELECT * FROM workout_logs WHERE isCompleted = 1 ORDER BY date DESC, timestamp DESC")
    fun getCompletedLogs(): Flow<List<WorkoutLogWithExercise>>

    @Query("SELECT DISTINCT date FROM workout_logs WHERE isCompleted = 1 ORDER BY date DESC")
    fun getCompletedWorkoutDates(): Flow<List<String>>

    @Query("SELECT COUNT(*) FROM workout_logs WHERE date = :date AND isCompleted = 1")
    fun getCompletedCountForDate(date: String): Flow<Int>

    @Transaction
    @Query("SELECT * FROM workout_logs ORDER BY date DESC, timestamp DESC")
    suspend fun getAllLogsOnce(): List<WorkoutLogWithExercise>

    @Query("DELETE FROM workout_logs")
    suspend fun deleteAllLogs()
}
