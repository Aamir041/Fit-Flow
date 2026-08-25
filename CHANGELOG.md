# Changelog

All notable changes to the Fit-Flow project will be documented in this file.

## [Unreleased] - 2026-08-25

### Changed
- **Sprint Duration Configuration in AddEditExerciseDialog** ([AddEditExerciseDialog.kt](file:///d:/Fit-Flow/app/src/main/java/com/fitflow/app/ui/exercises/AddEditExerciseDialog.kt)):
  - Replaced the increment/decrement `NumberStepper` with an editable numeric `OutlinedTextField` for direct duration input.
  - Added an adjacent unit dropdown menu supporting **Seconds (s)** and **Minutes (min)**.
  - Enhanced quick-select preset chips with unit awareness and automatic conversion to seconds on save.
