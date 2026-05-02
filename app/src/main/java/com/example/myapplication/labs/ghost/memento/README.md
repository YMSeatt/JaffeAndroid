# 💾 Ghost Memento: Persistent Command History

## 🛰️ The Metaphor
The **Ghost Memento** experiment takes its name from the classical "Memento" design pattern. Its role is to act as the classroom's "Long-Term Memory," capturing the application's internal state (specifically the Undo/Redo stacks) and externalizing it to persistent storage. This allows the application to "remember" its entire operational history even after a full system termination or device restart.

## 🏛️ Architectural Components

The Ghost Memento system is composed of three primary layers:

### 1. The Models (`MementoModels.kt`)
A collection of **Serializable Data Transfer Objects (DTOs)** that mirror the application's production `Command` hierarchy. These models are designed to be pure-data representations, stripped of any database or ViewModel dependencies, making them suitable for JSON serialization via `kotlinx.serialization`.
- **`MementoHistory`**: The root container for the `undoStack` and `redoStack`.
- **`MementoCommand`**: A sealed class hierarchy representing individual actions (e.g., `AddStudent`, `LogBehavior`, `MoveItems`).

### 2. The Mapper (`GhostMementoMapper.kt`)
The **Translation Bridge** between the live, operational `Command` objects and their serializable `MementoCommand` counterparts.
- **`toMemento()`**: Decomposes a live command, extracting its minimal state (IDs, coordinates, log data) into a Memento.
- **`fromMemento()`**: Reconstructs a fully functional command from a Memento, re-injecting the necessary `SeatingChartViewModel` or DAO dependencies required for execution.

### 3. The Store (`GhostMementoStore.kt`)
The **Persistence Engine** built on top of **Jetpack DataStore**.
- **Encryption**: Leverages `SecurityUtil` to encrypt the serialized JSON history before it touches the disk, ensuring that historical data remains private and secure.
- **Atomicity**: Utilizes DataStore's atomic update capabilities to ensure that the history stack never enters a corrupted state.
- **Reactive Stream**: Provides a `Flow<MementoHistory>` that the `SeatingChartViewModel` observes to recover the history during application initialization.

## 🔄 The Persistence Loop

The Memento system operates in a continuous loop during the seating chart session:
1. **Action**: A user performs an action (e.g., moves a student).
2. **Execution**: A production `Command` is created and executed.
3. **Capture**: The `SeatingChartViewModel` passes the updated undo stack to the `GhostMementoMapper`.
4. **Serialization**: The Mapper converts the stack into a `MementoHistory` object.
5. **Persistence**: The `GhostMementoStore` encrypts and saves the history to the DataStore.
6. **Recovery**: On the next app launch, the ViewModel reads the history from the store and re-hydrates its undo/redo stacks, allowing the teacher to resume work seamlessly.

---
*Documentation love letter from Scribe 📜*
