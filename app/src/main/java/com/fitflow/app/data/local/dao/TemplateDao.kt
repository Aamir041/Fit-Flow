package com.fitflow.app.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.fitflow.app.data.local.entity.TemplateEntity
import com.fitflow.app.data.local.entity.TemplateExerciseEntity
import com.fitflow.app.data.local.relation.TemplateWithExercises
import kotlinx.coroutines.flow.Flow

@Dao
interface TemplateDao {

    @Query("SELECT * FROM templates ORDER BY name ASC")
    fun getAllTemplates(): Flow<List<TemplateEntity>>

    @Query("SELECT * FROM templates WHERE id = :id LIMIT 1")
    suspend fun getTemplateById(id: Long): TemplateEntity?

    @Query("SELECT * FROM templates WHERE id = :id LIMIT 1")
    fun getTemplateByIdFlow(id: Long): Flow<TemplateEntity?>

    @Transaction
    @Query("SELECT * FROM templates WHERE id = :id LIMIT 1")
    fun getTemplateWithExercises(id: Long): Flow<TemplateWithExercises?>

    @Transaction
    @Query("SELECT * FROM templates WHERE id = :id LIMIT 1")
    suspend fun getTemplateWithExercisesOnce(id: Long): TemplateWithExercises?

    @Transaction
    @Query("SELECT * FROM templates ORDER BY createdDate DESC")
    fun getAllTemplatesWithExercises(): Flow<List<TemplateWithExercises>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTemplate(template: TemplateEntity): Long

    @Update
    suspend fun updateTemplate(template: TemplateEntity)

    @Delete
    suspend fun deleteTemplate(template: TemplateEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTemplateExercises(items: List<TemplateExerciseEntity>): List<Long>

    @Query("DELETE FROM template_exercises WHERE templateId = :templateId")
    suspend fun deleteTemplateExercisesForTemplate(templateId: Long)

    @Query("SELECT * FROM template_exercises WHERE templateId = :templateId ORDER BY orderIndex ASC")
    suspend fun getTemplateExercisesForTemplate(templateId: Long): List<TemplateExerciseEntity>

    @Transaction
    suspend fun saveTemplateWithExercises(
        template: TemplateEntity,
        exercises: List<TemplateExerciseEntity>
    ): Long {
        val templateId = if (template.id == 0L) {
            insertTemplate(template)
        } else {
            updateTemplate(template)
            deleteTemplateExercisesForTemplate(template.id)
            template.id
        }

        val mappedExercises = exercises.mapIndexed { index, ex ->
            ex.copy(templateId = templateId, orderIndex = index)
        }
        insertTemplateExercises(mappedExercises)
        return templateId
    }

    @Transaction
    suspend fun deleteTemplateAndExercises(template: TemplateEntity) {
        deleteTemplateExercisesForTemplate(template.id)
        deleteTemplate(template)
    }

    @Query("SELECT COUNT(*) FROM templates")
    suspend fun getTemplateCount(): Int
}
