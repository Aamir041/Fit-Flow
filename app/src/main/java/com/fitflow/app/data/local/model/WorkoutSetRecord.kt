package com.fitflow.app.data.local.model

import org.json.JSONArray
import org.json.JSONObject

/**
 * Represents a single set performed in a workout exercise.
 */
data class WorkoutSetRecord(
    val setNumber: Int,
    val reps: Int,
    val weight: Double,
    val isCompleted: Boolean = false
) {
    fun toJson(): JSONObject {
        return JSONObject().apply {
            put("setNumber", setNumber)
            put("reps", reps)
            put("weight", weight)
            put("isCompleted", isCompleted)
        }
    }

    companion object {
        fun fromJson(json: JSONObject): WorkoutSetRecord {
            return WorkoutSetRecord(
                setNumber = json.optInt("setNumber", 1),
                reps = json.optInt("reps", 10),
                weight = json.optDouble("weight", 0.0),
                isCompleted = json.optBoolean("isCompleted", false)
            )
        }

        fun parseSetsFromJson(jsonStr: String): List<WorkoutSetRecord> {
            if (jsonStr.isBlank()) return emptyList()
            return try {
                val array = JSONArray(jsonStr)
                val list = mutableListOf<WorkoutSetRecord>()
                for (i in 0 until array.length()) {
                    val obj = array.optJSONObject(i)
                    if (obj != null) {
                        list.add(fromJson(obj))
                    }
                }
                list
            } catch (e: Exception) {
                emptyList()
            }
        }

        fun serializeSetsToJson(sets: List<WorkoutSetRecord>): String {
            val array = JSONArray()
            sets.forEach { array.put(it.toJson()) }
            return array.toString()
        }
    }
}
