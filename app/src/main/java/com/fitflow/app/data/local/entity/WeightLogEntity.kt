package com.fitflow.app.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "weight_logs",
    indices = [
        Index(value = ["date"], unique = true),
        Index(value = ["timestamp"])
    ]
)
data class WeightLogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val date: String, // Format: YYYY-MM-DD
    val weightKg: Double,
    val timestamp: Long = System.currentTimeMillis()
)
