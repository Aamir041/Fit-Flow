package com.fitflow.app.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.fitflow.app.data.local.entity.WeightLogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WeightLogDao {

    @Query("SELECT * FROM weight_logs ORDER BY date ASC")
    fun getAllWeightLogs(): Flow<List<WeightLogEntity>>

    @Query("SELECT * FROM weight_logs ORDER BY date ASC")
    suspend fun getAllWeightLogsOnce(): List<WeightLogEntity>

    @Query("SELECT * FROM weight_logs WHERE date = :date LIMIT 1")
    fun getWeightLogByDate(date: String): Flow<WeightLogEntity?>

    @Query("SELECT * FROM weight_logs WHERE date = :date LIMIT 1")
    suspend fun getWeightLogByDateOnce(date: String): WeightLogEntity?

    @Query("SELECT * FROM weight_logs ORDER BY date DESC LIMIT 1")
    fun getLatestWeightLog(): Flow<WeightLogEntity?>

    @Query("SELECT * FROM weight_logs ORDER BY date ASC LIMIT 1")
    fun getOldestWeightLog(): Flow<WeightLogEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateWeightLog(weightLog: WeightLogEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWeightLogs(weightLogs: List<WeightLogEntity>): List<Long>

    @Update
    suspend fun updateWeightLog(weightLog: WeightLogEntity)

    @Delete
    suspend fun deleteWeightLog(weightLog: WeightLogEntity)

    @Query("DELETE FROM weight_logs WHERE date = :date")
    suspend fun deleteWeightLogByDate(date: String)

    @Query("DELETE FROM weight_logs")
    suspend fun deleteAllWeightLogs()

    @Query("SELECT COUNT(*) FROM weight_logs")
    suspend fun getWeightLogCount(): Int
}
