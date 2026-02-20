# Seating Chart & Behavior Logger

A comprehensive tool for teachers to manage seating charts and track student behavior, academic progress (quizzes), and homework completion. This project features both a mobile Android application and a desktop Python application.

## 🌟 Features

*   **Interactive Seating Chart**: Drag-and-drop interface to arrange students and furniture.
*   **Behavior Tracking**: Log positive and negative behaviors with customizable categories.
*   **Academic Logging**: Track quiz scores and homework completion with detailed mark types.
*   **Live Sessions**: Conduct real-time quiz or homework checks directly from the seating chart.
*   **Data Export**: Generate comprehensive Excel (`.xlsx`) reports with summaries and individual student sheets.
*   **Encryption**: Secure your data with Fernet encryption and optional password protection.
*   **Undo/Redo**: Full history support for all layout and logging actions.
*   **Teacher Reminders**: Schedule and receive system notifications for important classroom tasks.

## 🛠️ Tech Stack

### Android App
*   **Language**: Kotlin
*   **UI**: Jetpack Compose
*   **Database**: Room (with complex migrations)
*   **Dependency Injection**: Hilt
*   **Background Tasks**: WorkManager (for email reports)
*   **Encryption**: Fernet (com.macasaet.fernet)

### Python App (Desktop)
*   **Language**: Python 3
*   **UI**: Tkinter with `sv_ttk` (Sun-Valley Theme)
*   **Reporting**: `openpyxl`
*   **Encryption**: `cryptography` (Fernet)
*   **Experimental**: "Ghost Lab" analysis suite for advanced neural metrics and social gravity modeling.

## 📂 Project Structure

*   `app/`: Main Android application module.
    *   `src/main/java/.../data/`: Room entities, DAOs, and the `Exporter`.
    *   `src/main/java/.../ui/`: Jetpack Compose screens, dialogs, and themes.
    *   `src/main/java/.../viewmodel/`: Hilt-powered ViewModels for state management.
*   `Python/`: Python source code for the desktop application. See [Python/README.md](Python/README.md) for detailed architecture and setup.
*   `gradle/` & `build.gradle.kts`: Android build configuration.
*   `Setup.sh`: Script for setting up the Android SDK in a Linux environment.

## 🚀 Getting Started

### Android
1. Ensure you have the Android SDK installed (or use `Setup.sh`).
2. Open the project in Android Studio.
3. Sync Gradle and run the `app` module on an emulator or physical device.

### Python
1. Navigate to the `Python/` directory.
2. Install dependencies: `pip install tkinter sv_ttk darkdetect openpyxl pillow cryptography`.
3. Run the application: `python seatingchartmain.py`.

## 📜 Documentation

*   Core logic in `Exporter.kt` handles the complex mapping between app data and Excel reports.
*   Encryption logic is centralized in `SecurityUtil.kt`.
*   The `Command` pattern is used for the Undo/Redo system.
*   The **Reminder System** utilizes `android.app.AlarmManager` for precise scheduling and `BroadcastReceiver`s for triggering system notifications even when the app is backgrounded.

### ⏰ Reminders & Alarms Architecture
The application currently contains two parallel implementations for teacher reminders. Developers should adhere to the following guidelines to maintain consistency:

1.  **Primary Implementation (`util` & `screens` packages)**:
    *   **Components**: `ReminderManager`, `ReminderReceiver`, `ReminderViewModel`, and `ui/screens/RemindersScreen.kt`.
    *   **Standard**: This is the Hilt-enabled, modern implementation used by the main application flow. It uses **lowercase** Intent keys (e.g., `reminder_id`).
    *   **Status**: **Source of Truth**. Use this for all new features and modifications.

2.  **Legacy Implementation (`alarm` & `settings` packages)**:
    *   **Components**: `AlarmScheduler`, `AlarmReceiver`, `RemindersViewModel`, and `ui/settings/RemindersScreen.kt`.
    *   **Standard**: A redundant system that uses **uppercase** Intent keys (e.g., `REMINDER_ID`).
    *   **Status**: **Deprecated**. This implementation is non-functional for system notifications (missing Manifest registration) and is maintained only for legacy UI compatibility within the Settings menu.

## 🏗️ Technical Architecture & Data Parity

This project maintains strict data parity between the Android and Python applications, allowing for seamless data migration via JSON exports.

### 📐 Coordinate Systems
Both applications use a **logical coordinate system** for the seating chart canvas, which is independent of the physical screen resolution:
*   **Python App**: Uses a **2000x1500** logical canvas.
*   **Android App**: Uses a **4000x4000** logical canvas to support higher-density student arrangements and futuristic experimental layers.

**Cross-Platform Mapping**: When importing Python data into Android, the coordinates are currently mapped 1:1, meaning Python layouts will appear in the top-left quadrant of the Android canvas. The "Ghost Lab" features (like the Blueprint Engine) apply a scaling factor of `/ 4` and a fixed offset to normalize these coordinates into a unified 1200x800 SVG space.

### 🔄 Data Versioning
The application uses a versioned JSON schema (currently **v10**).
*   **Students & Furniture**: Managed via unique IDs (UUID strings in Python, mapped to auto-incrementing Longs in Android).
*   **Behavior & Academic Logs**: Unified format for behavior, quiz, and homework events, supporting multi-pass ingestion to maintain referential integrity.

## ⚡ Fluid Interaction & Performance Architecture

To maintain a fluid 60fps experience during complex seating chart interactions (like dragging student groups or live session logging), the application utilizes a sophisticated performance pipeline.

### 1. Optimistic UI & Local State
The UI components ([`StudentDraggableIcon`](app/src/main/java/com/example/myapplication/ui/components/StudentDraggableIcon.kt)) directly update their local `MutableState` properties during gestures. This provides immediate visual feedback without waiting for database round-trips or global state reconciliation.

### 2. The Pending State Cache
A `ConcurrentHashMap` ([`pendingStudentPositions`](app/src/main/java/com/example/myapplication/viewmodel/SeatingChartViewModel.kt)) tracks items currently "in flight." When the global state updates from the database, the ViewModel reconciles it with this pending cache to ensure student icons don't "snap back" to old positions before an asynchronous update completes.

### 3. Memoized Transformation Pipeline
The [`SeatingChartViewModel.updateStudentsForDisplay`](app/src/main/java/com/example/myapplication/viewmodel/SeatingChartViewModel.kt) method implements a multi-stage transformation:
- **Stage 1: Pre-processing**: Logs are grouped by student and configuration rules are decoded once per update cycle.
- **Stage 2: Per-Student Transformation**: Student data is enriched with filtered logs and conditional formatting results. This stage is memoized using a [`StudentCacheKey`](app/src/main/java/com/example/myapplication/viewmodel/SeatingChartViewModel.kt) which tracks only relevant data hashes (excluding volatile positions).
- **Stage 3: State Sync**: Instead of creating new UI objects, the pipeline updates the internal `MutableState` of cached [`StudentUiItem`](app/src/main/java/com/example/myapplication/ui/model/StudentUiItem.kt) instances.

### 4. Object Identity Preservation
By maintaining a persistent [`studentUiItemCache`](app/src/main/java/com/example/myapplication/viewmodel/SeatingChartViewModel.kt), the system ensures that Jetpack Compose can perform fine-grained "diff-and-patch" updates. Because the object instances remain the same, Compose only recomposes the specific properties (e.g., `xPosition`, `fontColor`) that changed, rather than re-rendering the entire seating chart.

---
*Documentation love letter from Scribe 📜*
