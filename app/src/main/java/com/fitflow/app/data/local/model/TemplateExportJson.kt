package com.fitflow.app.data.local.model

import org.json.JSONArray
import org.json.JSONObject

data class TemplateExportExercise(
    val exerciseName: String,
    val category: String = "General",
    val targetSets: Int = 3,
    val targetReps: Int = 10,
    val targetDurationSeconds: Int = 30,
    val restTimeSeconds: Int = 90,
    val isSprint: Boolean = false,
    val orderIndex: Int = 0
) {
    fun toJson(): JSONObject {
        return JSONObject().apply {
            put("exerciseName", exerciseName)
            put("category", category)
            put("targetSets", targetSets)
            put("targetReps", targetReps)
            put("targetDurationSeconds", targetDurationSeconds)
            put("restTimeSeconds", restTimeSeconds)
            put("isSprint", isSprint)
            put("orderIndex", orderIndex)
        }
    }

    companion object {
        fun fromJson(json: JSONObject): TemplateExportExercise {
            return TemplateExportExercise(
                exerciseName = json.optString("exerciseName", "").trim(),
                category = json.optString("category", "General").trim().ifEmpty { "General" },
                targetSets = json.optInt("targetSets", 3).coerceAtLeast(1),
                targetReps = json.optInt("targetReps", 10).coerceAtLeast(1),
                targetDurationSeconds = json.optInt("targetDurationSeconds", 30).coerceAtLeast(1),
                restTimeSeconds = json.optInt("restTimeSeconds", 90).coerceAtLeast(0),
                isSprint = json.optBoolean("isSprint", false),
                orderIndex = json.optInt("orderIndex", 0)
            )
        }
    }
}

data class TemplateExportJson(
    val templateName: String,
    val exercises: List<TemplateExportExercise> = emptyList()
) {
    fun toJson(): JSONObject {
        return JSONObject().apply {
            put("templateName", templateName)
            val exercisesArray = JSONArray()
            exercises.forEach { exercisesArray.put(it.toJson()) }
            put("exercises", exercisesArray)
        }
    }

    companion object {
        fun fromJson(json: JSONObject): TemplateExportJson {
            val templateName = json.optString("templateName", "").trim()
            val exercisesList = mutableListOf<TemplateExportExercise>()
            val exercisesArray = json.optJSONArray("exercises")
            if (exercisesArray != null) {
                for (i in 0 until exercisesArray.length()) {
                    val itemObj = exercisesArray.optJSONObject(i)
                    if (itemObj != null) {
                        val exercise = TemplateExportExercise.fromJson(itemObj)
                        if (exercise.exerciseName.isNotBlank()) {
                            exercisesList.add(exercise)
                        }
                    }
                }
            }
            return TemplateExportJson(
                templateName = templateName,
                exercises = exercisesList
            )
        }
    }
}

data class TemplateBundleExportJson(
    val version: Int = 1,
    val app: String = "FitFlow",
    val exportedAt: Long = System.currentTimeMillis(),
    val templates: List<TemplateExportJson> = emptyList()
) {
    fun toJsonString(indentSpaces: Int = 2): String {
        val json = JSONObject().apply {
            put("version", version)
            put("app", app)
            put("exportedAt", exportedAt)
            val templatesArray = JSONArray()
            templates.forEach { templatesArray.put(it.toJson()) }
            put("templates", templatesArray)
        }
        return json.toString(indentSpaces)
    }

    companion object {
        fun fromJsonString(jsonStr: String): TemplateBundleExportJson {
            val json = JSONObject(jsonStr)

            // Case 1: Standard bundle with "templates" array
            if (json.has("templates")) {
                val version = json.optInt("version", 1)
                val app = json.optString("app", "FitFlow")
                val exportedAt = json.optLong("exportedAt", System.currentTimeMillis())

                val templatesList = mutableListOf<TemplateExportJson>()
                val templatesArray = json.optJSONArray("templates")
                if (templatesArray != null) {
                    for (i in 0 until templatesArray.length()) {
                        val itemObj = templatesArray.optJSONObject(i)
                        if (itemObj != null) {
                            val template = TemplateExportJson.fromJson(itemObj)
                            if (template.templateName.isNotBlank()) {
                                templatesList.add(template)
                            }
                        }
                    }
                }

                return TemplateBundleExportJson(
                    version = version,
                    app = app,
                    exportedAt = exportedAt,
                    templates = templatesList
                )
            }

            // Case 2: Single template JSON with "templateName"
            if (json.has("templateName")) {
                val singleTemplate = TemplateExportJson.fromJson(json)
                return TemplateBundleExportJson(
                    version = json.optInt("version", 1),
                    app = "FitFlow",
                    exportedAt = json.optLong("exportedAt", System.currentTimeMillis()),
                    templates = if (singleTemplate.templateName.isNotBlank()) listOf(singleTemplate) else emptyList()
                )
            }

            return TemplateBundleExportJson()
        }
    }
}
