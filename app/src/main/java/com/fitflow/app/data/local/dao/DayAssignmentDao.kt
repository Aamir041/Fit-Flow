package com.fitflow.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.fitflow.app.data.local.entity.DayAssignmentEntity
import com.fitflow.app.data.local.relation.DayWithTemplate
import kotlinx.coroutines.flow.Flow

@Dao
interface DayAssignmentDao {

    @Transaction
    @Query("SELECT * FROM day_assignments ORDER BY dayOfWeek ASC")
    fun getAllDayAssignmentsWithTemplate(): Flow<List<DayWithTemplate>>

    @Transaction
    @Query("SELECT * FROM day_assignments WHERE dayOfWeek = :dayOfWeek LIMIT 1")
    fun getAssignmentForDay(dayOfWeek: Int): Flow<DayWithTemplate?>

    @Transaction
    @Query("SELECT * FROM day_assignments WHERE dayOfWeek = :dayOfWeek LIMIT 1")
    suspend fun getAssignmentForDayOnce(dayOfWeek: Int): DayWithTemplate?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDayAssignment(dayAssignment: DayAssignmentEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDayAssignments(assignments: List<DayAssignmentEntity>)

    @Update
    suspend fun updateDayAssignment(dayAssignment: DayAssignmentEntity)

    @Query("UPDATE day_assignments SET templateId = :templateId WHERE dayOfWeek = :dayOfWeek")
    suspend fun updateTemplateForDay(dayOfWeek: Int, templateId: Long?)

    @Query("SELECT COUNT(*) FROM day_assignments")
    suspend fun getCount(): Int
}
