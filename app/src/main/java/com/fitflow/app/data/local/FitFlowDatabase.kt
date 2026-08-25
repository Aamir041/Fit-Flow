package com.fitflow.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.fitflow.app.data.local.dao.DayAssignmentDao
import com.fitflow.app.data.local.dao.ExerciseDao
import com.fitflow.app.data.local.dao.TemplateDao
import com.fitflow.app.data.local.dao.WorkoutLogDao
import com.fitflow.app.data.local.entity.DayAssignmentEntity
import com.fitflow.app.data.local.entity.ExerciseEntity
import com.fitflow.app.data.local.entity.TemplateEntity
import com.fitflow.app.data.local.entity.TemplateExerciseEntity
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
        WorkoutLogEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class FitFlowDatabase : RoomDatabase() {

    abstract fun exerciseDao(): ExerciseDao
    abstract fun templateDao(): TemplateDao
    abstract fun dayAssignmentDao(): DayAssignmentDao
    abstract fun workoutLogDao(): WorkoutLogDao

    companion object {
        @Volatile
        private var INSTANCE: FitFlowDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope = CoroutineScope(Dispatchers.IO)): FitFlowDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    FitFlowDatabase::class.java,
                    "fitflow3.db"
                )
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

            if (exerciseDao.getExerciseCount() > 0) return

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
        }
    }
}
