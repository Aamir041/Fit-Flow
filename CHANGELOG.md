# Changelog

All notable changes to the Fit-Flow project will be documented in this file.

### Added
- **Unified Library Hub Tab** ([LibraryHubScreen.kt](file:///d:/Fit-Flow/app/src/main/java/com/fitflow/app/ui/library/LibraryHubScreen.kt), [Screen.kt](file:///d:/Fit-Flow/app/src/main/java/com/fitflow/app/ui/navigation/Screen.kt), [FitFlowNavGraph.kt](file:///d:/Fit-Flow/app/src/main/java/com/fitflow/app/ui/navigation/FitFlowNavGraph.kt)):
  - Merged **Templates** and **Exercise Library** into a unified **Library** bottom navigation tab.
  - Added a dashboard landing hub displaying total template and exercise counts alongside dedicated navigation cards for **Workout Templates** and **Exercise Catalog**.
  - Tapping either card navigates into the full-featured, dedicated screen with back navigation to return to the Hub.
  - Streamlined the bottom navigation bar to 5 core items: **Today**, **Food**, **Library**, **Schedule**, and **History**.
- **Food & Nutrition Logging Tab** ([FoodScreen.kt](file:///d:/Fit-Flow/app/src/main/java/com/fitflow/app/ui/food/FoodScreen.kt), [FoodViewModel.kt](file:///d:/Fit-Flow/app/src/main/java/com/fitflow/app/ui/food/FoodViewModel.kt), [AddEditFoodDialog.kt](file:///d:/Fit-Flow/app/src/main/java/com/fitflow/app/ui/food/AddEditFoodDialog.kt)):
  - Added a dedicated bottom navigation tab for logging daily food intake and calories for the present date only.
  - Interactive **Log Food** dialog with input for food name, quantity, unit selection (Grams `g`, Milligrams `mg`, Kilograms `kg`, Litres `l`, Millilitres `ml`, `unit`, `candy`, `piece`, `serving`, `cup`, `tbsp`, `tsp`, or custom unit text), calories (kcal), and time of day / meal categorization (Breakfast, Lunch, Dinner, Snack, Pre-Workout, Post-Workout).
  - Daily energy intake summary card calculating total calories consumed and total logged items for today.
  - Grouped meal cards by time of day with quick edit and delete options.
  - Stored in Room database with [FoodLogEntity.kt](file:///d:/Fit-Flow/app/src/main/java/com/fitflow/app/data/local/entity/FoodLogEntity.kt) and [FoodLogDao.kt](file:///d:/Fit-Flow/app/src/main/java/com/fitflow/app/data/local/dao/FoodLogDao.kt).
- **Daily Food History Integration in History Screen** ([HistoryScreen.kt](file:///d:/Fit-Flow/app/src/main/java/com/fitflow/app/ui/history/HistoryScreen.kt)):
  - Consistency heatmap illuminates green solely when exercise workouts were performed on that date.
  - Tapping any date on the consistency heatmap opens the scrollable details dialog with dedicated sections for both logged exercises and food items with quantities, units, and calories.
- **Workout & Food History Management** ([HistoryExportJson.kt](file:///d:/Fit-Flow/app/src/main/java/com/fitflow/app/data/local/model/HistoryExportJson.kt), [FitFlowRepository.kt](file:///d:/Fit-Flow/app/src/main/java/com/fitflow/app/data/repository/FitFlowRepository.kt)):
  - Implemented **Export History (JSON)** to backup all workout logs and food nutrition logs to device storage in a unified JSON bundle (`HistoryBundleExportJson` version 2).
  - Implemented **Import History (JSON)** with backward compatibility for version 1 files, restoring all workouts (with automatic custom exercise creation) and food logs.
  - Added **Clear All History** functionality with a safety confirmation dialog, clearing both workout and food logs.
  - Enhanced [HistoryScreen.kt](file:///d:/Fit-Flow/app/src/main/java/com/fitflow/app/ui/history/HistoryScreen.kt) with a "More Options" menu and Snackbar feedback.
- **Workout Templates Bulk JSON Export & Import Utility**:
  - Updated JSON models in [TemplateExportJson.kt](file:///d:/Fit-Flow/app/src/main/java/com/fitflow/app/data/local/model/TemplateExportJson.kt) to support bundle exports (`TemplateBundleExportJson`) containing all workout templates at once, with backward-compatible single template imports.
  - Implemented **Export All Templates (JSON)** in [TemplatesScreen.kt](file:///d:/Fit-Flow/app/src/main/java/com/fitflow/app/ui/templates/TemplatesScreen.kt) top bar menu to directly save all templates to the device file system via Android's `CreateDocument` storage launcher.
  - Removed individual per-template export buttons to focus on full-database backup and restore.
  - Added repository methods in [FitFlowRepository.kt](file:///d:/Fit-Flow/app/src/main/java/com/fitflow/app/data/repository/FitFlowRepository.kt) (`exportAllTemplatesToJson`, `importTemplateBundleFromJson`) and DAO queries in [TemplateDao.kt](file:///d:/Fit-Flow/app/src/main/java/com/fitflow/app/data/local/dao/TemplateDao.kt).
  - Maintained automatic custom exercise creation for any unrecognized exercises in imported templates.
- **Monthly GitHub-Style Consistency Heatmap & Daily Details Dialog** ([HistoryScreen.kt](file:///d:/Fit-Flow/app/src/main/java/com/fitflow/app/ui/history/HistoryScreen.kt)):
  - Added an interactive activity contribution grid (`MonthContributionHeatmap`) showing a square for each day in the selected month.
  - Squares illuminate based on workout activity intensity (0 workouts: subtle slate, 1 workout: light emerald, 2-3 workouts: emerald, 4+ workouts: vibrant primary emerald).
  - Tapping any date square opens a dedicated, scrollable popup dialog (`DayWorkoutDetailsDialog`) displaying the complete list of movements, sets, reps, sprints, and weights logged on that specific day.
  - Removed the static exercise list below the heatmap for a focused, clean layout.
  - Included a highlighted border for the current day, month navigation controls (Previous / Next month), and an activity legend.
- **Template Name Uniqueness Validation**:
  - Enforced case-insensitive template name uniqueness checks in [TemplateEditViewModel.kt](file:///d:/Fit-Flow/app/src/main/java/com/fitflow/app/ui/templates/TemplateEditViewModel.kt) on save.
  - Enforced template name uniqueness checks during JSON import to prevent duplicate template creation.
  - Added unit tests in [FitFlowLogicTest.kt](file:///d:/Fit-Flow/app/src/test/java/com/fitflow/app/FitFlowLogicTest.kt) verifying serialization/deserialization and uniqueness rules.

### Fixed
- **Duplicate Exercises in Exercise Library**:
  - Added a unique index on `name` in [ExerciseEntity.kt](file:///d:/Fit-Flow/app/src/main/java/com/fitflow/app/data/local/entity/ExerciseEntity.kt) (`@Index(value = ["name"], unique = true)`).
  - Bumped database version to 3 in [FitFlowDatabase.kt](file:///d:/Fit-Flow/app/src/main/java/com/fitflow/app/data/local/FitFlowDatabase.kt) so database seeding and Room insert replacement (`OnConflictStrategy.REPLACE`) automatically deduplicate entries and prevent duplicate exercises from appearing.
- Fixed missing `OutlinedButton` and `ButtonDefaults` Material3 imports in [TemplatesScreen.kt](file:///d:/Fit-Flow/app/src/main/java/com/fitflow/app/ui/templates/TemplatesScreen.kt).

### Changed
- **Exercise Library Delete Flow** ([ExerciseLibraryScreen.kt](file:///d:/Fit-Flow/app/src/main/java/com/fitflow/app/ui/exercises/ExerciseLibraryScreen.kt), [AddEditExerciseDialog.kt](file:///d:/Fit-Flow/app/src/main/java/com/fitflow/app/ui/exercises/AddEditExerciseDialog.kt)):
  - Moved the custom exercise delete action from individual list item tiles into the edit dialog.
  - Added a dedicated **Delete** button in the dialog action row when editing custom exercises, prompting the safety confirmation dialog.
  - Made the entire list item card clickable to open the edit dialog.
- **Sprint Duration Configuration in AddEditExerciseDialog** ([AddEditExerciseDialog.kt](file:///d:/Fit-Flow/app/src/main/java/com/fitflow/app/ui/exercises/AddEditExerciseDialog.kt)):
  - Replaced the increment/decrement `NumberStepper` with an editable numeric `OutlinedTextField` for direct duration input.
  - Added an adjacent unit dropdown menu supporting **Seconds (s)** and **Minutes (min)**.
  - Enhanced quick-select preset chips with unit awareness and automatic conversion to seconds on save.

### Removed
- **Weight Counter from Today's Workout Tab** ([ExerciseLogCard.kt](file:///d:/Fit-Flow/app/src/main/java/com/fitflow/app/ui/home/ExerciseLogCard.kt)):
  - Removed the `DecimalStepper` weight counter from standard exercise logging cards, streamlining logging to focus on Sets and Reps (or duration for sprint exercises).
- **Sessions and Completed Counters from History Screen** ([HistoryScreen.kt](file:///d:/Fit-Flow/app/src/main/java/com/fitflow/app/ui/history/HistoryScreen.kt)):
  - Removed the aggregate stats cards row displaying "Sessions" and "Completed" count metrics, giving full focus to the consistency heatmap on the History screen.
- **Volume Metric from History Screen** ([HistoryScreen.kt](file:///d:/Fit-Flow/app/src/main/java/com/fitflow/app/ui/history/HistoryScreen.kt)):
  - Removed the Volume metric card from the top aggregate stats row in the History screen.
