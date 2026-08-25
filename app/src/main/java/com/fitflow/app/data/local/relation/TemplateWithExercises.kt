package com.fitflow.app.data.local.relation

import androidx.room.Embedded
import androidx.room.Relation
import com.fitflow.app.data.local.entity.ExerciseEntity
import com.fitflow.app.data.local.entity.TemplateEntity
import com.fitflow.app.data.local.entity.TemplateExerciseEntity

data class TemplateExerciseWithDetail(
    @Embedded val templateExercise: TemplateExerciseEntity,
    @Relation(
        parentColumn = "exerciseId",
        entityColumn = "id"
    )
    val exercise: ExerciseEntity
)

data class TemplateWithExercises(
    @Embedded val template: TemplateEntity,
    @Relation(
        entity = TemplateExerciseEntity::class,
        parentColumn = "id",
        entityColumn = "templateId"
    )
    val exercises: List<TemplateExerciseWithDetail>
)

data class TemplateExerciseItem(
    val templateExerciseId: Long,
    val exerciseId: Long,
    val exerciseName: String,
    val category: String,
    val targetSets: Int,
    val targetReps: Int,
    val restTimeSeconds: Int,
    val orderIndex: Int
)
