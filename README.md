# FitFlow - Native Android Gym Tracking App 🏋️‍♂️⚡

**FitFlow** is a modern native Android gym tracking app built with **Kotlin**, **Jetpack Compose (Material 3)**, **MVVM architecture**, **Room (SQLite)** with multi-table relationships & auto-seeding, and **Jetpack Navigation Compose**.

---

## 🌟 Core Feature: Day-to-Day Template Binding

1. **Custom Workout Templates**: Build splits like *Push Day*, *Pull Day*, *Leg Day*, or *Full Body*.
2. **Weekly Day Assignment**: Attach any template to any day of the week (Monday through Sunday) or set as a Rest Day.
3. **Smart Today Home Screen**: On any given day, FitFlow automatically detects today's day-of-week, loads the attached template, and allows logging sets, reps, and weights with real-time completion tracking.

---

## 🏗️ Architecture & Tech Stack

- **Language**: Kotlin 2.0
- **UI Toolkit**: Jetpack Compose with Material 3 Design System
- **Design Aesthetic**: Dark Gym Theme with Obsidian Slate (`#0D0E12`), Electric Emerald (`#00E676`), Cyber Cyan (`#00E5FF`), and category badge color accents
- **Architecture**: Clean MVVM (ViewModel + StateFlow + Repository Pattern)
- **Local Persistence**: Room SQLite 2.6 with `@Entity`, `@Dao`, `@Relation`, `@Embedded`, `@Transaction`, and automatic database pre-seeding
- **Navigation**: Jetpack Navigation Compose with slide & fade animations
- **Dependency Injection**: Application-scoped Container & Custom `ViewModelProvider.Factory`

---

## 📊 Database Schema (Room SQLite)

- **`ExerciseEntity`** (`exercises`): `id` (PK), `name`, `category` (Chest, Back, Legs, Shoulders, Arms, Core, Cardio), `defaultSets`, `defaultReps`, `isCustom`
- **`TemplateEntity`** (`templates`): `id` (PK), `name`, `createdDate`
- **`TemplateExerciseEntity`** (`template_exercises`): `id` (PK), `templateId` (FK CASCADE), `exerciseId` (FK CASCADE), `targetSets`, `targetReps`, `restTimeSeconds`, `orderIndex`
- **`DayAssignmentEntity`** (`day_assignments`): `id` (PK), `dayOfWeek` (1=Mon to 7=Sun), `templateId` (FK SET_NULL)
- **`WorkoutLogEntity`** (`workout_logs`): `id` (PK), `date` (YYYY-MM-DD), `templateId` (FK), `exerciseId` (FK), `actualSets`, `actualReps`, `actualWeight`, `isCompleted`, `timestamp`

---

## 📱 Screens & Features

### 1. 🏠 Today (Home Screen)
- Resolves today's date (e.g., "Saturday, August 22, 2026") and active split.
- Workout progress header with smooth animated progress bar (e.g., "3 of 5 exercises completed • 60%").
- Interactive exercise cards with steppers for **Sets**, **Reps**, and **Weight (kg)**.
- Quick **Rest Timer** countdown dialog (e.g., 90s, with +/- adjustments).
- One-tap **Mark Complete** button saving to `WorkoutLogEntity`.
- Empty state with CTA when today is a rest day.

### 2. 📑 Template Management
- List of saved templates with exercise count and target muscle category chips.
- Create new templates or edit existing ones.
- Search & add exercises via bottom sheet with category filters.
- Reorder exercises (move up / down) and adjust target sets/reps/rest.
- Form validation (prevents empty name or 0 exercises).

### 3. 📅 Weekly Schedule
- 7-day Monday through Sunday overview.
- Current day highlighted with glowing Electric Emerald border and `TODAY` pill badge.
- Tap any day to open the bottom sheet and assign a template or set as a Rest Day.

### 4. 📚 Exercise Library
- Catalog of pre-seeded exercises categorized into Chest, Back, Legs, Shoulders, Arms, Core, Cardio.
- Real-time search and filter chips.
- Custom exercise CRUD with default sets & reps.

### 5. 📈 History & Stats
- Overview metrics: Total Workout Sessions, Completed Movements, Total Volume Lifted (kg).
- Daily grouped workout history cards.

---

## 🚀 How to Run in Android Studio

1. Open **Android Studio** (Hedgehog, Iguana, Jellyfish, or newer).
2. Select **Open** and choose this project directory: `Learning/Antigravity tutorial`.
3. Let Gradle sync and download dependencies.
4. Select an Android Emulator or physical device (API 26+).
5. Click **Run ▶ (Shift + F10)**.

---

## 🧪 Running Unit Tests

Run the logic and domain tests:
```bash
./gradlew test
```
