# 🧠 ViewModels & State Management

This package contains the **Jetpack ViewModels** that manage the application's UI state and coordinate between the UI layer (Compose) and the data layer (Repository/DAOs).

## 🏛️ Architecture

The ViewModel layer follows the **Unidirectional Data Flow (UDF)** pattern, exposing state via `LiveData`, `StateFlow`, and `MutableState` (Compose), and receiving events via public methods.

### 👑 The Central Coordinator: `SeatingChartViewModel`
The `SeatingChartViewModel` is the "brain" of the seating chart experience. It coordinates:
- **Reactive Data Integration**: Merges streams from Students, Logs, Groups, and Rules.
- **Undo/Redo Orchestration**: Manages the [Command history](../commands/README.md).
- **Ghost Lab Experiments**: Integrates futuristic AI and physics-based engines.
- **Display Transformation**: Implements a high-performance 3-stage pipeline to convert raw entities into UI-optimized items.

### 🛠️ Specialized ViewModels
- **`SettingsViewModel`**: Manages user preferences, database archiving, and cross-platform data imports.
- **`StatsViewModel`**: Synthesizes historical log data into high-level classroom analytics.
- **`StudentGroupsViewModel`**: Handles the management of relational student groupings.
- **`ReminderViewModel`**: Orchestrates teacher tasks and system-level alarms.

## ⚡ Performance Patterns

To maintain a fluid 60fps interaction model (especially during drag-and-drop operations on a 4000x4000 canvas), the ViewModels utilize several **BOLT** (Performance-Obsessed) patterns:

1.  **Optimistic UI & Local State**: Gesture handlers in the UI update local `MutableState` properties for immediate feedback, while the ViewModel reconciles these with asynchronous database updates.
2.  **Memoized Transformation Pipeline**: Stage 2 of the student update cycle utilizes identity-based caching to avoid re-calculating formatting or log descriptions when irrelevant data changes.
3.  **Identity Preservation**: ViewModels maintain persistent caches of UI objects (e.g., `studentUiItemCache`). Reusing instances allows Compose to perform fine-grained "diff-and-patch" updates rather than full-box recompositions.
4.  **Background Synthesis**: Heavy calculations, such as force-directed layout optimization or large-scale stats aggregation, are offloaded to `Dispatchers.Default`.

## ⛓️ State & Event Flow

1.  **State Observation**: The UI observes aggregated state objects (e.g., `studentsForDisplay`).
2.  **User Action**: User interaction triggers a ViewModel method (e.g., `updateStudentPosition`).
3.  **Command Execution**: The action is encapsulated in a `Command`, executed, and pushed to the undo stack.
4.  **Database Persistence**: The ViewModel triggers an `internal` method to persist the change to Room via the Repository.
5.  **Reactive Update**: The database change emits a new value via the DAO's `Flow` or `LiveData`, triggering a refresh of the UI-optimized state.

---
*Documentation love letter from Scribe 📜*
