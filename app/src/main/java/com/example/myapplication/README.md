# 📱 Android Core & Entry Points

This package contains the primary Android entry points, activity orchestration logic, and high-level application lifecycle management. It serves as the "Control Center" that connects the underlying logical engines with the user interface.

## 🏛️ Activity Architecture & Orchestration

The application uses a multi-activity architecture where each activity has a distinct role in the user experience:

1.  **`MainActivity` (The Seating Chart Hub)**:
    -   **Role**: The primary interface for classroom management.
    -   **Orchestration**: Manages the integration of multiple ViewModels (`SeatingChartViewModel`, `SettingsViewModel`, `StatsViewModel`, `StudentGroupsViewModel`) and handles system-level result launchers for file I/O (import/export).
    -   **State Management**: Controls the "Unlocked" state of the application and coordinates the high-level navigation between the seating chart, data viewer, and reminders.

2.  **`SettingsActivity` (The Configuration Center)**:
    -   **Role**: A dedicated environment for granular application customization.
    -   **Isolation**: Uses a separate activity to provide a clean boundary for complex navigation within the settings tree (Tabs for General, Display, Data, etc.).
    -   **DI Integration**: Leverages Hilt for property injection of repositories and importers required for bulk data operations.

3.  **`HelpActivity` (The Knowledge Base)**:
    -   **Role**: A static, user-facing documentation hub.
    -   **Purpose**: Provides in-app guidance on gestures, formatting rules, and data management to ensure a shallow learning curve for teachers.

## 🛡️ Shield (Security & Privacy Hardening)

This package implements several critical **Shield** security patterns to protect student PII and teacher credentials:

-   **Automatic Session Locking**: `MainActivity` monitors user interaction and implements a configurable auto-lock timer. If the app is idle, it reverts to the `PasswordScreen` to prevent unauthorized access.
-   **Hardware-Backed Protection (`FLAG_SECURE`)**: `SettingsActivity` utilizes the `WindowManager.LayoutParams.FLAG_SECURE` flag. This prevents the system from taking screenshots or screen recordings of sensitive data like SMTP passwords or PII.
-   **Deterministic Privacy Cleanup**: On startup, `MainActivity` performs a background sweep of the `cache` and `shared` directories to purge any temporary `.xlsx`, `.png`, or `.db` files left over from export or sharing operations.

## ⚙️ Application Lifecycle & Initialization

-   **`MyApplication` (The Engine Room)**:
    -   **Hilt Foundation**: Bootstraps the dependency injection graph via `@HiltAndroidApp`.
    -   **Background Scheduling**: Coordinates the scheduling of periodic background workers (like `EmailWorker`) via `WorkManager`.

---
*Documentation love letter from Scribe 📜*
