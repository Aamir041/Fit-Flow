package com.fitflow.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.fitflow.app.data.local.dao.DayAssignmentDao
import com.fitflow.app.data.local.dao.ExerciseDao
import com.fitflow.app.data.local.dao.FoodLogDao
import com.fitflow.app.data.local.dao.TemplateDao
import com.fitflow.app.data.local.dao.WeightLogDao
import com.fitflow.app.data.local.dao.WorkoutLogDao
import com.fitflow.app.data.local.entity.DayAssignmentEntity
import com.fitflow.app.data.local.entity.ExerciseEntity
import com.fitflow.app.data.local.entity.FoodLogEntity
import com.fitflow.app.data.local.entity.TemplateEntity
import com.fitflow.app.data.local.entity.TemplateExerciseEntity
import com.fitflow.app.data.local.entity.WeightLogEntity
import com.fitflow.app.data.local.entity.WorkoutLogEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        ExerciseEntity::class,
        TemplateEntity::class,
        TemplateExerciseEntity::class,
        DayAssignmentEntity::class,
        WorkoutLogEntity::class,
        FoodLogEntity::class,
        WeightLogEntity::class
    ],
    version = 6,
    exportSchema = false
)
abstract class FitFlowDatabase : RoomDatabase() {

    abstract fun exerciseDao(): ExerciseDao
    abstract fun templateDao(): TemplateDao
    abstract fun dayAssignmentDao(): DayAssignmentDao
    abstract fun workoutLogDao(): WorkoutLogDao
    abstract fun foodLogDao(): FoodLogDao
    abstract fun weightLogDao(): WeightLogDao

    companion object {
        @Volatile
        private var INSTANCE: FitFlowDatabase? = null

        val MIGRATION_4_5 = object : androidx.room.migration.Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE workout_logs ADD COLUMN setsDataJson TEXT NOT NULL DEFAULT ''")
            }
        }

        val MIGRATION_5_6 = object : androidx.room.migration.Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS weight_logs (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        date TEXT NOT NULL,
                        weightKg REAL NOT NULL,
                        timestamp INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_weight_logs_date ON weight_logs (date)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_weight_logs_timestamp ON weight_logs (timestamp)")
            }
        }

        fun getDatabase(context: Context, scope: CoroutineScope = CoroutineScope(Dispatchers.IO)): FitFlowDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    FitFlowDatabase::class.java,
                    "fitflow3.db"
                )
                    .addMigrations(MIGRATION_4_5, MIGRATION_5_6)
                    .fallbackToDestructiveMigration()
                    .addCallback(DatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        populateDatabase(database)
                    }
                }
            }
        }

        suspend fun populateDatabase(database: FitFlowDatabase) {
            val exerciseDao = database.exerciseDao()
            val templateDao = database.templateDao()
            val dayAssignmentDao = database.dayAssignmentDao()

            if (exerciseDao.getExerciseCount() > 0) {
                seedSampleWeightLogs(database.weightLogDao())
                return
            }

            // 1. Seed Exercises
            val seedExercises = listOf(
                // Chest
                ExerciseEntity(name = "Barbell Bench Press", category = "Chest", defaultSets = 4, defaultReps = 8),
                ExerciseEntity(name = "Incline Dumbbell Press", category = "Chest", defaultSets = 3, defaultReps = 10),
                ExerciseEntity(name = "Cable Chest Flyes", category = "Chest", defaultSets = 3, defaultReps = 12),
                ExerciseEntity(name = "Dips (Chest Focus)", category = "Chest", defaultSets = 3, defaultReps = 10),
                ExerciseEntity(name = "Push-ups", category = "Chest", defaultSets = 3, defaultReps = 15),

                // Back
                ExerciseEntity(name = "Deadlift", category = "Back", defaultSets = 4, defaultReps = 6),
                ExerciseEntity(name = "Pull-ups", category = "Back", defaultSets = 3, defaultReps = 8),
                ExerciseEntity(name = "Barbell Bent-Over Row", category = "Back", defaultSets = 4, defaultReps = 8),
                ExerciseEntity(name = "Lat Pulldown", category = "Back", defaultSets = 3, defaultReps = 10),
                ExerciseEntity(name = "Seated Cable Row", category = "Back", defaultSets = 3, defaultReps = 12),
                ExerciseEntity(name = "Face Pulls", category = "Back", defaultSets = 3, defaultReps = 15),

                // Legs
                ExerciseEntity(name = "Barbell Back Squat", category = "Legs", defaultSets = 4, defaultReps = 8),
                ExerciseEntity(name = "Romanian Deadlift", category = "Legs", defaultSets = 3, defaultReps = 10),
                ExerciseEntity(name = "Leg Press", category = "Legs", defaultSets = 3, defaultReps = 12),
                ExerciseEntity(name = "Leg Extensions", category = "Legs", defaultSets = 3, defaultReps = 12),
                ExerciseEntity(name = "Hamstring Leg Curls", category = "Legs", defaultSets = 3, defaultReps = 12),
                ExerciseEntity(name = "Standing Calf Raises", category = "Legs", defaultSets = 4, defaultReps = 15),
                ExerciseEntity(name = "Bulgarian Split Squat", category = "Legs", defaultSets = 3, defaultReps = 10),

                // Shoulders
                ExerciseEntity(name = "Overhead Barbell Press", category = "Shoulders", defaultSets = 4, defaultReps = 8),
                ExerciseEntity(name = "Dumbbell Lateral Raise", category = "Shoulders", defaultSets = 4, defaultReps = 15),
                ExerciseEntity(name = "Dumbbell Shoulder Press", category = "Shoulders", defaultSets = 3, defaultReps = 10),
                ExerciseEntity(name = "Reverse Pec Deck Fly", category = "Shoulders", defaultSets = 3, defaultReps = 12),

                // Arms
                ExerciseEntity(name = "Barbell Bicep Curl", category = "Arms", defaultSets = 3, defaultReps = 10),
                ExerciseEntity(name = "Dumbbell Hammer Curl", category = "Arms", defaultSets = 3, defaultReps = 12),
                ExerciseEntity(name = "Tricep Cable Pushdown", category = "Arms", defaultSets = 3, defaultReps = 12),
                ExerciseEntity(name = "Skull Crushers", category = "Arms", defaultSets = 3, defaultReps = 10),

                // Core & Cardio
                ExerciseEntity(name = "Hanging Leg Raise", category = "Core", defaultSets = 3, defaultReps = 15),
                ExerciseEntity(name = "Plank Hold", category = "Core", defaultSets = 3, defaultReps = 60),
                ExerciseEntity(name = "Cable Woodchoppers", category = "Core", defaultSets = 3, defaultReps = 12),
                ExerciseEntity(name = "Treadmill Steady Cardio", category = "Cardio", defaultSets = 1, defaultReps = 20),
                ExerciseEntity(name = "Rowing Machine", category = "Cardio", defaultSets = 1, defaultReps = 15),

                // Sprints (Duration-Based)
                ExerciseEntity(name = "100m Track Sprint", category = "Cardio", isSprint = true, defaultDurationSeconds = 30),
                ExerciseEntity(name = "Treadmill HIIT Sprint", category = "Cardio", isSprint = true, defaultDurationSeconds = 45),
                ExerciseEntity(name = "Assault Bike Max Sprint", category = "Cardio", isSprint = true, defaultDurationSeconds = 30),
                ExerciseEntity(name = "Outdoor Hill Sprints", category = "Cardio", isSprint = true, defaultDurationSeconds = 60)
            )
            val exerciseIds = exerciseDao.insertExercises(seedExercises)

            // 2. Seed Default Templates (Push Day, Pull Day, Leg Day)
            val pushTemplateId = templateDao.insertTemplate(
                TemplateEntity(name = "Push Day (Chest, Shoulders, Triceps)")
            )
            val pushExercises = listOf(
                TemplateExerciseEntity(
                    templateId = pushTemplateId,
                    exerciseId = exerciseIds[0], // Bench Press
                    targetSets = 4, targetReps = 8, restTimeSeconds = 120, orderIndex = 0
                ),
                TemplateExerciseEntity(
                    templateId = pushTemplateId,
                    exerciseId = exerciseIds[18], // Overhead Barbell Press
                    targetSets = 3, targetReps = 8, restTimeSeconds = 90, orderIndex = 1
                ),
                TemplateExerciseEntity(
                    templateId = pushTemplateId,
                    exerciseId = exerciseIds[1], // Incline Dumbbell Press
                    targetSets = 3, targetReps = 10, restTimeSeconds = 90, orderIndex = 2
                ),
                TemplateExerciseEntity(
                    templateId = pushTemplateId,
                    exerciseId = exerciseIds[19], // Lateral Raise
                    targetSets = 4, targetReps = 15, restTimeSeconds = 60, orderIndex = 3
                ),
                TemplateExerciseEntity(
                    templateId = pushTemplateId,
                    exerciseId = exerciseIds[24], // Tricep Cable Pushdown
                    targetSets = 3, targetReps = 12, restTimeSeconds = 60, orderIndex = 4
                )
            )
            templateDao.insertTemplateExercises(pushExercises)

            val pullTemplateId = templateDao.insertTemplate(
                TemplateEntity(name = "Pull Day (Back, Biceps, Rear Delts)")
            )
            val pullExercises = listOf(
                TemplateExerciseEntity(
                    templateId = pullTemplateId,
                    exerciseId = exerciseIds[5], // Deadlift
                    targetSets = 4, targetReps = 6, restTimeSeconds = 150, orderIndex = 0
                ),
                TemplateExerciseEntity(
                    templateId = pullTemplateId,
                    exerciseId = exerciseIds[6], // Pull-ups
                    targetSets = 3, targetReps = 8, restTimeSeconds = 90, orderIndex = 1
                ),
                TemplateExerciseEntity(
                    templateId = pullTemplateId,
                    exerciseId = exerciseIds[7], // Barbell Row
                    targetSets = 4, targetReps = 8, restTimeSeconds = 90, orderIndex = 2
                ),
                TemplateExerciseEntity(
                    templateId = pullTemplateId,
                    exerciseId = exerciseIds[10], // Face Pulls
                    targetSets = 3, targetReps = 15, restTimeSeconds = 60, orderIndex = 3
                ),
                TemplateExerciseEntity(
                    templateId = pullTemplateId,
                    exerciseId = exerciseIds[22], // Barbell Bicep Curl
                    targetSets = 3, targetReps = 10, restTimeSeconds = 60, orderIndex = 4
                )
            )
            templateDao.insertTemplateExercises(pullExercises)

            val legTemplateId = templateDao.insertTemplate(
                TemplateEntity(name = "Leg Day (Quads, Hamstrings, Calves)")
            )
            val legExercises = listOf(
                TemplateExerciseEntity(
                    templateId = legTemplateId,
                    exerciseId = exerciseIds[11], // Barbell Squat
                    targetSets = 4, targetReps = 8, restTimeSeconds = 150, orderIndex = 0
                ),
                TemplateExerciseEntity(
                    templateId = legTemplateId,
                    exerciseId = exerciseIds[12], // Romanian Deadlift
                    targetSets = 3, targetReps = 10, restTimeSeconds = 90, orderIndex = 1
                ),
                TemplateExerciseEntity(
                    templateId = legTemplateId,
                    exerciseId = exerciseIds[13], // Leg Press
                    targetSets = 3, targetReps = 12, restTimeSeconds = 90, orderIndex = 2
                ),
                TemplateExerciseEntity(
                    templateId = legTemplateId,
                    exerciseId = exerciseIds[14], // Leg Extensions
                    targetSets = 3, targetReps = 12, restTimeSeconds = 60, orderIndex = 3
                ),
                TemplateExerciseEntity(
                    templateId = legTemplateId,
                    exerciseId = exerciseIds[16], // Standing Calf Raises
                    targetSets = 4, targetReps = 15, restTimeSeconds = 60, orderIndex = 4
                )
            )
            templateDao.insertTemplateExercises(legExercises)

            // 3. Seed Default Day Assignments (Monday to Sunday: 1 to 7)
            val defaultAssignments = listOf(
                DayAssignmentEntity(dayOfWeek = 1, templateId = pushTemplateId), // Mon -> Push
                DayAssignmentEntity(dayOfWeek = 2, templateId = pullTemplateId), // Tue -> Pull
                DayAssignmentEntity(dayOfWeek = 3, templateId = null),           // Wed -> Rest
                DayAssignmentEntity(dayOfWeek = 4, templateId = legTemplateId),  // Thu -> Legs
                DayAssignmentEntity(dayOfWeek = 5, templateId = pushTemplateId), // Fri -> Push
                DayAssignmentEntity(dayOfWeek = 6, templateId = pullTemplateId), // Sat -> Pull
                DayAssignmentEntity(dayOfWeek = 7, templateId = null)            // Sun -> Rest
            )
            dayAssignmentDao.insertDayAssignments(defaultAssignments)

            // 4. Seed Sample Weight Tracking Logs
            seedSampleWeightLogs(database.weightLogDao())
        }

        suspend fun seedSampleWeightLogs(weightLogDao: com.fitflow.app.data.local.dao.WeightLogDao) {
            if (weightLogDao.getWeightLogCount() > 0) return

            val sampleWeightLogs = listOf(
                WeightLogEntity(date = "2026-01-05", weightKg = 85.2),
                WeightLogEntity(date = "2026-01-08", weightKg = 85.0),
                WeightLogEntity(date = "2026-01-12", weightKg = 84.7),
                WeightLogEntity(date = "2026-01-17", weightKg = 84.9),
                WeightLogEntity(date = "2026-01-21", weightKg = 84.3),
                WeightLogEntity(date = "2026-01-27", weightKg = 84.0),
                WeightLogEntity(date = "2026-02-01", weightKg = 83.6),
                WeightLogEntity(date = "2026-02-06", weightKg = 83.8),
                WeightLogEntity(date = "2026-02-10", weightKg = 83.2),
                WeightLogEntity(date = "2026-02-15", weightKg = 83.0),
                WeightLogEntity(date = "2026-02-22", weightKg = 82.5),
                WeightLogEntity(date = "2026-02-26", weightKg = 82.7),
                WeightLogEntity(date = "2026-03-03", weightKg = 82.1),
                WeightLogEntity(date = "2026-03-08", weightKg = 81.9),
                WeightLogEntity(date = "2026-03-14", weightKg = 82.2),
                WeightLogEntity(date = "2026-03-19", weightKg = 81.5),
                WeightLogEntity(date = "2026-03-25", weightKg = 81.3),
                WeightLogEntity(date = "2026-03-30", weightKg = 81.0),
                WeightLogEntity(date = "2026-04-04", weightKg = 80.6),
                WeightLogEntity(date = "2026-04-10", weightKg = 80.8),
                WeightLogEntity(date = "2026-04-15", weightKg = 80.2),
                WeightLogEntity(date = "2026-04-21", weightKg = 79.9),
                WeightLogEntity(date = "2026-04-26", weightKg = 80.1),
                WeightLogEntity(date = "2026-05-02", weightKg = 79.5),
                WeightLogEntity(date = "2026-05-07", weightKg = 79.2),
                WeightLogEntity(date = "2026-05-13", weightKg = 79.4),
                WeightLogEntity(date = "2026-05-19", weightKg = 78.8),
                WeightLogEntity(date = "2026-05-25", weightKg = 78.5),
                WeightLogEntity(date = "2026-05-31", weightKg = 78.3),
                WeightLogEntity(date = "2026-06-05", weightKg = 78.6),
                WeightLogEntity(date = "2026-06-11", weightKg = 78.0),
                WeightLogEntity(date = "2026-06-16", weightKg = 77.7),
                WeightLogEntity(date = "2026-06-22", weightKg = 77.5),
                WeightLogEntity(date = "2026-06-28", weightKg = 77.9),
                WeightLogEntity(date = "2026-07-03", weightKg = 77.2),
                WeightLogEntity(date = "2026-07-09", weightKg = 76.9),
                WeightLogEntity(date = "2026-07-14", weightKg = 77.1),
                WeightLogEntity(date = "2026-07-20", weightKg = 76.6),
                WeightLogEntity(date = "2026-07-26", weightKg = 76.4),
                WeightLogEntity(date = "2026-08-01", weightKg = 76.8),
                WeightLogEntity(date = "2026-08-06", weightKg = 76.3),
                WeightLogEntity(date = "2026-08-11", weightKg = 76.0),
                WeightLogEntity(date = "2026-08-16", weightKg = 76.2),
                WeightLogEntity(date = "2026-08-20", weightKg = 75.7),
                WeightLogEntity(date = "2026-08-24", weightKg = 75.9),
                WeightLogEntity(date = "2026-08-27", weightKg = 75.5),
                WeightLogEntity(date = "2026-08-29", weightKg = 75.8),
                WeightLogEntity(date = "2026-08-30", weightKg = 75.6),
                WeightLogEntity(date = "2026-08-31", weightKg = 75.7),
                WeightLogEntity(date = "2026-09-01", weightKg = 75.4)
            )
            weightLogDao.insertWeightLogs(sampleWeightLogs)
        }
    }
}
