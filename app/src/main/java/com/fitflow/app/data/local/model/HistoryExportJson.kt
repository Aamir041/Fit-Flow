package com.fitflow.app.data.local.model

import org.json.JSONArray
import org.json.JSONObject

/**
 * Represents a single workout log entry for export/import purposes.
 * Includes flattened exercise details to maintain integrity across devices/installs.
 */
data class HistoryLogExport(
    val date: String,
    val exerciseName: String,
    val category: String = "General",
    val actualSets: Int = 3,
    val actualReps: Int = 10,
    val actualWeight: Double = 0.0,
    val actualDurationSeconds: Int = 0,
    val isCompleted: Boolean = true,
    val isSprint: Boolean = false,
    val setsDataJson: String = "",
    val timestamp: Long = System.currentTimeMillis()
) {
    fun toJson(): JSONObject {
        return JSONObject().apply {
            put("date", date)
            put("exerciseName", exerciseName)
            put("category", category)
            put("actualSets", actualSets)
            put("actualReps", actualReps)
            put("actualWeight", actualWeight)
            put("actualDurationSeconds", actualDurationSeconds)
            put("isCompleted", isCompleted)
            put("isSprint", isSprint)
            put("setsDataJson", setsDataJson)
            put("timestamp", timestamp)
        }
    }

    companion object {
        fun fromJson(json: JSONObject): HistoryLogExport {
            return HistoryLogExport(
                date = json.optString("date", ""),
                exerciseName = json.optString("exerciseName", "").trim(),
                category = json.optString("category", "General").trim().ifEmpty { "General" },
                actualSets = json.optInt("actualSets", 3),
                actualReps = json.optInt("actualReps", 10),
                actualWeight = json.optDouble("actualWeight", 0.0),
                actualDurationSeconds = json.optInt("actualDurationSeconds", 0),
                isCompleted = json.optBoolean("isCompleted", true),
                isSprint = json.optBoolean("isSprint", false),
                setsDataJson = json.optString("setsDataJson", ""),
                timestamp = json.optLong("timestamp", System.currentTimeMillis())
            )
        }
    }
}

/**
 * Represents a single weight log entry for export/import purposes.
 */
data class WeightLogExport(
    val date: String,
    val weightKg: Double,
    val timestamp: Long = System.currentTimeMillis()
) {
    fun toJson(): JSONObject {
        return JSONObject().apply {
            put("date", date)
            put("weightKg", weightKg)
            put("timestamp", timestamp)
        }
    }

    companion object {
        fun fromJson(json: JSONObject): WeightLogExport {
            return WeightLogExport(
                date = json.optString("date", ""),
                weightKg = json.optDouble("weightKg", 0.0),
                timestamp = json.optLong("timestamp", System.currentTimeMillis())
            )
        }
    }
}

/**
 * Bundle containing the entire history (workouts and weight logs) for backup and restore.
 */
data class HistoryBundleExportJson(
    val version: Int = 3,
    val app: String = "FitFlow",
    val exportedAt: Long = System.currentTimeMillis(),
    val logs: List<HistoryLogExport> = emptyList(),
    val weightLogs: List<WeightLogExport> = emptyList()
) {
    fun toJsonString(indentSpaces: Int = 2): String {
        val json = JSONObject().apply {
            put("version", version)
            put("app", app)
            put("exportedAt", exportedAt)
            val logsArray = JSONArray()
            logs.forEach { logsArray.put(it.toJson()) }
            put("logs", logsArray)

            val weightLogsArray = JSONArray()
            weightLogs.forEach { weightLogsArray.put(it.toJson()) }
            put("weightLogs", weightLogsArray)
        }
        return json.toString(indentSpaces)
    }

    companion object {
        fun fromJsonString(jsonStr: String): HistoryBundleExportJson {
            val json = JSONObject(jsonStr)
            val version = json.optInt("version", 1)
            val app = json.optString("app", "FitFlow")
            val exportedAt = json.optLong("exportedAt", System.currentTimeMillis())

            val logsList = mutableListOf<HistoryLogExport>()
            val logsArray = json.optJSONArray("logs")
            if (logsArray != null) {
                for (i in 0 until logsArray.length()) {
                    val itemObj = logsArray.optJSONObject(i)
                    if (itemObj != null) {
                        val log = HistoryLogExport.fromJson(itemObj)
                        if (log.exerciseName.isNotBlank() && log.date.isNotBlank()) {
                            logsList.add(log)
                        }
                    }
                }
            }

            val weightLogsList = mutableListOf<WeightLogExport>()
            val weightLogsArray = json.optJSONArray("weightLogs")
            if (weightLogsArray != null) {
                for (i in 0 until weightLogsArray.length()) {
                    val itemObj = weightLogsArray.optJSONObject(i)
                    if (itemObj != null) {
                        val weightLog = WeightLogExport.fromJson(itemObj)
                        if (weightLog.date.isNotBlank() && weightLog.weightKg > 0.0) {
                            weightLogsList.add(weightLog)
                        }
                    }
                }
            }

            return HistoryBundleExportJson(
                version = version,
                app = app,
                exportedAt = exportedAt,
                logs = logsList,
                weightLogs = weightLogsList
            )
        }
    }
}
