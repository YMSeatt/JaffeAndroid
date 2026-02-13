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

---
*Documentation love letter from Scribe 📜*
