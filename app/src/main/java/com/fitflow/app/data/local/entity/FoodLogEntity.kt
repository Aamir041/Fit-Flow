package com.fitflow.app.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "food_logs",
    indices = [
        Index(value = ["date"]),
        Index(value = ["timestamp"])
    ]
)
data class FoodLogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val date: String, // Format: YYYY-MM-DD
    val foodName: String,
    val quantity: Double,
    val unit: String, // e.g. "g", "mg", "kg", "l", "ml", "unit", "candy", etc.
    val calories: Int,
    val mealTime: String, // e.g. "Breakfast", "Lunch", "Dinner", "Snack", etc.
    val timestamp: Long = System.currentTimeMillis()
)
