package com.fitflow.app.data.local.relation

import androidx.room.Embedded
import androidx.room.Relation
import com.fitflow.app.data.local.entity.DayAssignmentEntity
import com.fitflow.app.data.local.entity.ExerciseEntity
import com.fitflow.app.data.local.entity.TemplateEntity
import com.fitflow.app.data.local.entity.WorkoutLogEntity

data class DayWithTemplate(
    @Embedded val dayAssignment: DayAssignmentEntity,
    @Relation(
        parentColumn = "templateId",
        entityColumn = "id"
    )
    val template: TemplateEntity?
)

data class WorkoutLogWithExercise(
    @Embedded val log: WorkoutLogEntity,
    @Relation(
        parentColumn = "exerciseId",
        entityColumn = "id"
    )
    val exercise: ExerciseEntity
)
