package com.fitflow.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "exercises")
data class ExerciseEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val category: String,
    val defaultSets: Int = 3,
    val defaultReps: Int = 10,
    val isCustom: Boolean = false,
    val isSprint: Boolean = false,
    val defaultDurationSeconds: Int = 30
)

