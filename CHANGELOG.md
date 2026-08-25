# Changelog

All notable changes to the Fit-Flow project will be documented in this file.

## [Unreleased] - 2026-08-25

### Added
- **Workout History Management**:
  - Implemented **Export History (JSON)** to backup all workout logs to the device storage.
  - Implemented **Import History (JSON)** to restore workout logs, with automatic custom exercise creation for unrecognized exercises.
  - Added **Clear All History** functionality with a safety confirmation dialog.
  - Enhanced [HistoryScreen.kt](file:///d:/Fit-Flow/app/src/main/java/com/fitflow/app/ui/history/HistoryScreen.kt) with a "More Options" menu and Snackbar feedback.
  - Added [HistoryExportJson.kt](file:///d:/Fit-Flow/app/src/main/java/com/fitflow/app/data/local/model/HistoryExportJson.kt) for robust history data serialization.
- **Workout Templates Bulk JSON Export & Import Utility**:
  - Updated JSON models in [TemplateExportJson.kt](file:///d:/Fit-Flow/app/src/main/java/com/fitflow/app/data/local/model/TemplateExportJson.kt) to support bundle exports (`TemplateBundleExportJson`) containing all workout templates at once, with backward-compatible single template imports.
  - Implemented **Export All Templates (JSON)** in [TemplatesScreen.kt](file:///d:/Fit-Flow/app/src/main/java/com/fitflow/app/ui/templates/TemplatesScreen.kt) top bar menu to directly save all templates to the device file system via Android's `CreateDocument` storage launcher.
  - Removed individual per-template export buttons to focus on full-database backup and restore.
  - Added repository methods in [FitFlowRepository.kt](file:///d:/Fit-Flow/app/src/main/java/com/fitflow/app/data/repository/FitFlowRepository.kt) (`exportAllTemplatesToJson`, `importTemplateBundleFromJson`) and DAO queries in [TemplateDao.kt](file:///d:/Fit-Flow/app/src/main/java/com/fitflow/app/data/local/dao/TemplateDao.kt).
  - Maintained automatic custom exercise creation for any unrecognized exercises in imported templates.
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
- **Sprint Duration Configuration in AddEditExerciseDialog** ([AddEditExerciseDialog.kt](file:///d:/Fit-Flow/app/src/main/java/com/fitflow/app/ui/exercises/AddEditExerciseDialog.kt)):
  - Replaced the increment/decrement `NumberStepper` with an editable numeric `OutlinedTextField` for direct duration input.
  - Added an adjacent unit dropdown menu supporting **Seconds (s)** and **Minutes (min)**.
  - Enhanced quick-select preset chips with unit awareness and automatic conversion to seconds on save.
