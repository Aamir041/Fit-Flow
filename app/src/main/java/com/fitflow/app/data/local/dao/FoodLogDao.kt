package com.fitflow.app.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.fitflow.app.data.local.entity.FoodLogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FoodLogDao {

    @Query("SELECT * FROM food_logs WHERE date = :date ORDER BY timestamp ASC")
    fun getFoodLogsForDate(date: String): Flow<List<FoodLogEntity>>

    @Query("SELECT * FROM food_logs WHERE date = :date ORDER BY timestamp ASC")
    suspend fun getFoodLogsForDateOnce(date: String): List<FoodLogEntity>

    @Query("SELECT * FROM food_logs ORDER BY date DESC, timestamp DESC")
    fun getAllFoodLogs(): Flow<List<FoodLogEntity>>

    @Query("SELECT * FROM food_logs ORDER BY date DESC, timestamp DESC")
    suspend fun getAllFoodLogsOnce(): List<FoodLogEntity>

    @Query("SELECT DISTINCT date FROM food_logs ORDER BY date DESC")
    fun getDistinctFoodDates(): Flow<List<String>>

    @Query("SELECT SUM(calories) FROM food_logs WHERE date = :date")
    fun getTotalCaloriesForDate(date: String): Flow<Int?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFoodLog(log: FoodLogEntity): Long

    @Update
    suspend fun updateFoodLog(log: FoodLogEntity)

    @Delete
    suspend fun deleteFoodLog(log: FoodLogEntity)

    @Query("DELETE FROM food_logs WHERE id = :id")
    suspend fun deleteFoodLogById(id: Long)

    @Query("DELETE FROM food_logs")
    suspend fun deleteAllFoodLogs()
}
