# Android Application Development Roadmap

This roadmap outlines the current state of feature implementation in the Android application compared to the reference Python application, and identifies the next steps for development.

## 1. Features Already Implemented (or indicated as supported by preferences)

Based on `AppPreferencesRepository.kt`, the Android application has laid the groundwork for, or already implemented, the persistence of the following features/preferences:

*   **App Preferences:**
    *   **Recent Logs Limit:** The `RECENT_LOGS_LIMIT` and `RECENT_BEHAVIOR_INCIDENTS_LIMIT` keys indicate support for configuring the number of recent logs/incidents to display.
    *   **Use Initials for Behavior:** The `USE_INITIALS_FOR_BEHAVIOR` preference suggests the ability to display student initials for behavior logs.
    *   **App Theme:** `APP_THEME` allows for setting the application's visual theme (LIGHT, DARK, SYSTEM).
    *   **Show Recent Behavior:** `SHOW_RECENT_BEHAVIOR` implies a toggle for displaying recent behavior logs on student boxes.
    *   **Default Student Box Appearance:** Preferences for `DEFAULT_STUDENT_BOX_WIDTH`, `DEFAULT_STUDENT_BOX_HEIGHT`, `DEFAULT_STUDENT_BOX_BG_COLOR`, `DEFAULT_STUDENT_BOX_OUTLINE_COLOR`, and `DEFAULT_STUDENT_BOX_TEXT_COLOR` are present, indicating control over the visual defaults for student boxes.
    *   **Default Student Box Outline Thickness:** `DEFAULT_STUDENT_BOX_OUTLINE_THICKNESS` is a new setting in Android, providing more granular control than the Python app (which uses a hardcoded thickness relative to zoom).
    *   **Password Protection:** `PASSWORD_ENABLED` and `PASSWORD_HASH` indicate that the Android app supports password-based security.
    *   **Customizable Behavior Types:** `BEHAVIOR_TYPES_LIST` suggests that users can define custom behavior categories.
    *   **Customizable Homework Assignment Types:** `HOMEWORK_ASSIGNMENT_TYPES_LIST` allows for defining custom types of homework assignments.
    *   **Customizable Homework Statuses:** `HOMEWORK_STATUSES_LIST` enables users to define custom statuses for homework.

## 2. Features Missing from the Android App

The following core functionalities and UI components from the Python application are currently missing or not explicitly indicated by `AppPreferencesRepository.kt`. This list represents significant development work.

*   **Core Data Management & UI:**
    *   **Student Management UI:** The ability to add, edit, or delete students via a user interface. This includes capturing first name, last name, nickname, gender, and assigning to groups.
    *   **Furniture Management UI:** UI for adding, editing, or deleting furniture items (e.g., desks, tables).
    *   **Seating Chart Canvas:** The interactive visual canvas where students and furniture are displayed, dragged, resized, and interacted with. This includes zoom, pan, rulers, and grid.
    *   **Behavior Logging UI:** Dialogs and logic for logging behavior incidents for individual or multiple students.
    *   **Quiz Logging UI:** Dialogs and logic for manually logging quiz scores (quiz name, number of questions, marks data).
    *   **Homework Logging UI:** Dialogs and logic for manually logging homework (type, status, detailed marks).
    *   **Live Session Modes:** The "Live Quiz" and "Live Homework" session functionalities, including the UI to start/end sessions and the per-student marking interface.
    *   **Undo/Redo System:** The entire command-pattern based undo/redo stack for application actions.
    *   **Data Persistence Logic:** While preferences are defined, the actual loading/saving of core application data (students, furniture, logs) to local storage (e.g., Room Database, Flat Files, Encrypted DataStore) and handling data migrations.
    *   **Backup & Restore:** Functionality to backup and restore the entire application data.
    *   **Import/Export (Excel/CSV):** Importing student data from Excel or exporting logs to Excel/CSV.
    *   **Layout Templates:** Saving and loading predefined seating chart layouts.
    *   **Student Groups Management UI:** Creating, editing, deleting student groups, and assigning/unassigning students to groups from a dedicated UI.
    *   **Quiz/Homework Templates Management UI:** Creating and managing templates for quizzes and homework.

*   **Advanced Features & Interactions:**
    *   **Conditional Formatting:** The sophisticated system for visually highlighting student boxes based on various rules (group, behavior count, quiz/homework scores, live session responses, time-based). This is a complex feature requiring significant UI and logic.
    *   **Student Style Customization (per student):** Beyond default preferences, the ability to customize individual student box appearance (colors, font, size).
    *   **Guides:** Adding, moving, and deleting horizontal/vertical guides on the canvas.
    *   **Layout Tools:** Alignment (top, bottom, left, right, center) and distribution (horizontal, vertical) tools for selected items.
    *   **Attendance Report:** Generating attendance reports based on log data.
    *   **Password Management Logic:** Implementing the full password flow including auto-lock, password on open, and password on sensitive actions.
    *   **Search/Filter Functionality:** Searching through logs or students.

## 3. Technical Considerations and Dependencies

Implementing the missing features will require careful planning and likely involve new Android-specific technologies and architectural patterns.

*   **Data Storage:**
    *   **Room Persistence Library:** For storing structured data like `Student`, `Furniture`, `BehaviorLog`, `HomeworkLog`, `StudentGroup`, `QuizTemplate`, and `HomeworkTemplate` objects. This is the standard Android way for local database persistence and is a strong candidate for replacing the JSON file storage in Python.
    *   **DataStore (Preferences/Proto):** `Preference DataStore` is already in use for preferences. `Proto DataStore` could be considered for more complex, schema-defined single-source-of-truth application settings, potentially replacing some JSON files like custom behaviors/homework types.
    *   **Encryption:** The Python app uses `cryptography.fernet` for encryption. For Android, consider Android KeyStore along with a symmetric encryption cipher (e.g., AES) to secure sensitive user data (like passwords and potentially encrypted data files).

*   **User Interface (UI):**
    *   **Compose Multiplatform:** If the goal is a truly cross-platform app (Android, Desktop), Compose Multiplatform would be the ideal choice for UI, replicating the Python app's cross-platform nature. If Android-only, Jetpack Compose or traditional Android Views (XML layouts) would be used. Given Android Studio context, Jetpack Compose is the modern preference.
    *   **Custom View/Canvas:** Replicating the interactive seating chart canvas will require a custom `View` (or a `Canvas` in Compose) to handle drawing, touch events (drag, resize, selection), zoom, pan, rulers, grid, and guides. This is a complex task.
    *   **Dialogs and Bottom Sheets:** Android provides various components for dialogs (`AlertDialog`, `DialogFragment`) and bottom sheets (`BottomSheetDialogFragment` in Material Design) for user input and selections (e.g., adding students, logging behavior, settings).
    *   **Recycler Views:** For displaying lists of logs, students (in management screens), custom types, and templates, `RecyclerView` is essential for efficient scrolling.

*   **Architecture:**
    *   **MVVM (Model-View-ViewModel) with Kotlin Flow/LiveData:** This is the recommended modern Android architecture pattern for separating concerns, promoting testability, and handling asynchronous data streams. The existing `Flow` usage in `AppPreferencesRepository` aligns with this.
    *   **Dependency Injection (Hilt/Koin):** For managing dependencies (e.g., `AppPreferencesRepository`, database instances, ViewModels), a DI framework like Hilt (Jetpack-recommended) or Koin would be beneficial.
    *   **Coroutines:** For asynchronous operations (database access, file I/O, network if applicable), Kotlin Coroutines with `Flow` are the standard for managing concurrency safely.

*   **Feature-Specific Logic:**
    *   **Command Pattern:** The Python app uses a command pattern for Undo/Redo. A similar pattern could be implemented in Kotlin for Android, managing a stack of executable and reversible operations.
    *   **Date and Time Handling:** Use `java.time` (or `kotlinx-datetime`) for modern date and time operations, replacing Python's `datetime`.
    *   **Image Processing:** For export layout as image, Android's `Bitmap` class and potentially third-party libraries for more advanced image manipulation will be needed.

## Conclusion

The Android application has a solid foundation for preferences management and data persistence infrastructure. However, the core interactive "seating chart" UI, dynamic data modeling (beyond preferences), and the majority of the rich feature set found in the Python reference implementation are yet to be built. The roadmap highlights the significant work ahead in developing the interactive canvas, logging mechanisms, live session capabilities, and advanced reporting and customization features, leveraging Android's modern development ecosystem.
