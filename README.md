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

## 📂 Project Structure

*   `app/`: Main Android application module.
    *   `src/main/java/.../data/`: Room entities, DAOs, and the `Exporter`.
    *   `src/main/java/.../ui/`: Jetpack Compose screens, dialogs, and themes.
    *   `src/main/java/.../viewmodel/`: Hilt-powered ViewModels for state management.
*   `Python/`: Python source code for the desktop application.
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

---
*Documentation love letter from Scribe 📜*
