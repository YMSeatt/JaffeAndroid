# 🎮 Commands & History Management

This package implements the **Command Pattern**, providing a robust, multi-step Undo/Redo system for the Seating Chart application. Every user action that modifies the classroom layout, student data, or behavioral logs is encapsulated as a `Command` object.

## 🏛️ Architecture

The Command system is composed of three primary elements:

1.  **`Command` Interface**: Defines the contract for all reversible operations (`execute()`, `undo()`, and `getDescription()`).
2.  **Command Implementations**: Specific classes (e.g., `MoveStudentCommand`, `AddFurnitureCommand`) that capture all necessary state to perform and reverse an action.
3.  **`SeatingChartViewModel`**: Acts as the invoker and history manager. It maintains two stacks (`commandUndoStack` and `commandRedoStack`) to track the application's timeline.

### ⛓️ Composite Commands
The `CompositeCommand` class allows multiple individual commands to be grouped into a single atomic action. This is used for bulk operations like "Align Items" or "Distribute Items," where multiple movements should be treated as one logical step by the user.

## 🔄 History Manipulation

### Linear Undo/Redo
The standard `undo()` and `redo()` methods provide a linear traversal of the command history.
- **Undo**: Pops the top command from the undo stack, calls its `undo()` method, and pushes it onto the redo stack.
- **Redo**: Pops from the redo stack, calls `execute()`, and pushes back onto the undo stack.

### ⚡ Selective Undo (Non-Linear)
A standout feature of this application is **Selective Undo**, which allows users to manipulate a specific historical action without losing subsequent work.

#### The "Rollback -> Isolate -> Re-branch" Algorithm:
1.  **Rollback**: The system temporarily undos all commands that occurred *after* the target command.
2.  **Isolate**: The target command itself is undone (or re-executed/toggled).
3.  **Re-branch**: To maintain data integrity and avoid "temporal paradoxes" (where a future command depends on a now-missing past state), all commands that were rolled back are **permanently discarded**. History "re-branches" from the point of the modification.

## 🛠️ Implementing a New Command

To add a new reversible action:
1.  Create a new class implementing the `Command` interface.
2.  Inject the `SeatingChartViewModel` (or specific DAOs) into the constructor.
3.  Capture the **Minimal Necessary State** (e.g., old and new coordinates, or a snapshot of the entity) to support `undo()`.
4.  Implement `execute()` by calling the corresponding `internal` method in the ViewModel.
5.  Implement `undo()` to restore the previous state.
6.  Trigger the command from the ViewModel using the `executeCommand(command)` helper.

---
*Documentation love letter from Scribe 📜*
