# 💬 Modal Interaction & Dialog Architecture

This package contains the modal components of the Seating Chart application. It implements a **Dialog-Heavy** interaction model designed to keep the primary seating chart canvas focused on spatial management while offloading data entry and complex configuration to focused, transient interfaces.

## 🏛️ Interaction Model: Spatial vs. Modal

The application distinguishes between two primary modes of interaction:

1.  **Spatial (The Canvas)**: High-frequency gestures (dragging students, zooming) that require immediate visual feedback.
2.  **Modal (Dialogs)**: Intentional, structured data entry (logging behavior, editing student details, configuring exports).

By utilizing standard Material 3 `AlertDialog` and custom modal sheets, the app ensures that users are never distracted by persistent input fields on the seating chart, maintaining a clean "Tactical HUD" aesthetic.

## ⛓️ Architectural Patterns

### 1. ViewModel Orchestration
Dialogs are rarely "standalone." They act as bridges between user intent and the application state managed by ViewModels.
- **Primary Coordinator**: Most dialogs interact with the `SeatingChartViewModel` to fetch contextual data (like a student's current position) or trigger updates.
- **Specialized ViewModels**: Complex dialogs (e.g., `StudentStyleScreen`) leverage Hilt's `hiltViewModel()` to inject specialized logic handlers without cluttering the main screen's state.

### 2. Command Pattern Integration
To support the application's robust **Undo/Redo** system, data-modifying actions within dialogs are encapsulated in `Command` objects.
- **Pattern**: When a user clicks "Save" or "Log," the dialog calls a method on the ViewModel (e.g., `viewModel.addBehaviorEvent(event)`).
- **History**: The ViewModel wraps this action in a `Command` (e.g., `LogBehaviorCommand`), executes it, and pushes it onto the history stack. This ensures that even complex modal actions can be reversed from the main seating chart UI.

### 3. State Management & Lifecycle
Dialogs utilize a mix of local and observed state:
- **Local State**: Transient input (text field values, temporary selections) is managed via `remember { mutableStateOf(...) }` within the Composable.
- **Observed State**: Contextual data (student lists, available behavior types) is observed from the ViewModel using `collectAsState()` or `observeAsState()`.
- **LaunchedEffects**: Used for one-time setup tasks, such as loading a specific student entity when the dialog is first displayed.

## ⚡ Performance Considerations

While dialogs are modal and thus "stop" the main canvas interaction, they still adhere to the app's performance standards:
- **Lazy Loading**: Content-heavy dialogs (like `ExportDialog` or `BehaviorLogViewerDialog`) use `LazyColumn` to handle potentially large lists of students or logs without impacting frame rates.
- **Composition Boundedness**: By keeping input state local to the dialog, the app prevents global recompositions of the seating chart while the user is typing in a modal.

---
*Documentation love letter from Scribe 📜*
