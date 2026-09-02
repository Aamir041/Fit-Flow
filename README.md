# FitFlow - Native Android Gym & Fitness Tracking App 🏋️‍♂️⚡

**FitFlow** is a modern, high-performance native Android workout tracking application built with **Kotlin**, **Jetpack Compose (Material 3)**, **MVVM architecture**, **Room (SQLite)** with multi-table relationships & auto-seeding, and **Jetpack Navigation Compose**.

---

## 🌟 Core Features

1. **Smart Today Home Screen**:
   - Automatically detects today's day-of-week, resolves the scheduled split, and displays today's workout target.
   - Detailed set-by-set tracking with individual completion checkboxes, reps, weights, and sprint durations.
   - Workout progress bar with live completion percentage.
   - Quick **Rest Timer** countdown dialog (customizable duration with +/- adjustments).
   - Direct shortcut to app settings.

2. **Day-to-Day Template Split Binding**:
   - Build custom workout templates (*Push Day*, *Pull Day*, *Leg Day*, *Upper Body*, etc.).
   - Attach templates to any day of the week (Monday through Sunday) in the **Schedule** tab or mark days as Rest Days.

3. **Unified Library Hub**:
   - Centralized landing hub providing instant access to **Workout Templates** and the **Exercise Catalog**.
   - **Workout Templates**: Create, customize, reorder exercises, validate name uniqueness, and backup/restore templates via JSON.
   - **Exercise Catalog**: Extensive categorized library (Chest, Back, Legs, Shoulders, Arms, Core, Cardio, and Duration-based Sprints) with search, filter chips, and custom exercise creation.

4. **Consistency Heatmap & Detailed Stats**:
   - Monthly GitHub-style activity consistency grid with intensity-based illumination for active workout days.
   - Interactive date selection opening a detailed day breakdown modal listing all completed movements, sets, reps, and weights.

5. **Body Weight Tracking & Progress Chart**:
   - Dedicated weight progression line graph with time-range filters (**1M**, **3M**, **6M**, **1Y**, **All**).
   - Summary statistics cards: Starting Weight, Current Weight, Net Change (+/- kg), and Min/Max Weight.
   - Log or edit daily body weight directly from the Stats screen or date detail dialog.

6. **Customizable Appearance & Data Management**:
   - **Theme Modes**: System Default, Light, Dark, and Pure AMOLED Black.
   - **Accent Colors**: Electric Emerald, Ocean Blue, Sunset Orange, Radiant Purple, Crimson Red, and Golden Amber.
   - **JSON Backup & Restore**: Export and import complete workout history and templates.

---

## 🏗️ Architecture & Tech Stack

- **Language**: Kotlin 2.0+
- **UI Toolkit**: Jetpack Compose with Material 3 Design System
- **Design Aesthetic**: Dark Gym Theme with Obsidian Slate (`#0D0E12`), Electric Emerald (`#00E676`), Cyber Cyan (`#00E5FF`), and dynamic accent support
- **Architecture**: Clean MVVM (ViewModel + StateFlow + Repository Pattern)
- **Local Persistence**: Room SQLite (Schema v7) with `@Entity`, `@Dao`, `@Relation`, `@Embedded`, `@Transaction`, and database pre-seeding
- **Navigation**: Jetpack Navigation Compose with 4 core bottom navigation tabs (**Today**, **Library**, **Schedule**, **Stats**)
- **Dependency Injection**: Application-scoped Container & Custom `ViewModelProvider.Factory`

---

## 📊 Database Schema (Room SQLite v7)

- **`ExerciseEntity`** (`exercises`): `id` (PK), `name` (Unique Index), `category`, `defaultSets`, `defaultReps`, `isCustom`, `isSprint`, `defaultDurationSeconds`
- **`TemplateEntity`** (`templates`): `id` (PK), `name`, `createdDate`
- **`TemplateExerciseEntity`** (`template_exercises`): `id` (PK), `templateId` (FK CASCADE), `exerciseId` (FK CASCADE), `targetSets`, `targetReps`, `targetDurationSeconds`, `restTimeSeconds`, `orderIndex`
- **`DayAssignmentEntity`** (`day_assignments`): `id` (PK), `dayOfWeek` (1=Mon to 7=Sun), `templateId` (FK SET_NULL)
- **`WorkoutLogEntity`** (`workout_logs`): `id` (PK), `date` (YYYY-MM-DD), `templateId` (FK), `exerciseId` (FK), `actualSets`, `actualReps`, `actualWeight`, `actualDurationSeconds`, `isCompleted`, `setsDataJson`, `timestamp`
- **`WeightLogEntity`** (`weight_logs`): `id` (PK), `date` (Unique Index), `weightKg`, `timestamp`

---

## 📱 Bottom Navigation Tabs

| Tab | Route | Description |
| :--- | :--- | :--- |
| **Today** | `home` | Active split workout, set-by-set checklist, rest timer, and progress |
| **Library** | `library` | Hub linking to Workout Templates and Exercise Catalog |
| **Schedule** | `schedule` | 7-day Monday–Sunday visual weekly split planner |
| **Stats** | `history` | Activity consistency heatmap, day detail modal, and body weight line chart |

---

## 🚀 How to Run in Android Studio

1. Open **Android Studio** (Koala, Ladybug, Iguana, or newer).
2. Select **Open** and select the project directory: `d:\Fit-Flow`.
3. Allow Gradle to sync and download dependencies.
4. Select an Android Emulator or physical device running **Android API 26+** (Android 8.0+).
5. Click **Run ▶ (Shift + F10)**.

---

## 🧪 Running Unit Tests

Execute the unit test suite with Gradle:
```bash
./gradlew test
```

Or assemble the debug APK:
```bash
./gradlew assembleDebug
```
