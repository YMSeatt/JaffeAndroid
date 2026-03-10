# 🗄️ Relational Data & Persistence Architecture

This package defines the application's "Source of Truth," utilizing **Room** for high-performance SQLite persistence. It manages the complex web of relationships between students, their physical layout, and their longitudinal behavioral and academic history.

## 🏛️ Core Entity Hierarchy

The data model is organized into three primary clusters:

1.  **Foundational Entities**:
    *   **Student**: The central actor. Supports unique `stringId` (UUID) for cross-platform parity with the Python desktop app and `Long` primary keys for Room performance.
    *   **StudentGroup**: Defines classroom clusters with associated visual styling (colors).
    *   **Furniture**: Represents physical objects on the seating chart (desks, tables, etc.).

2.  **Longitudinal Logs (The History)**:
    *   **BehaviorEvent**: Categorized incidents (Positive/Negative/Neutral) with associated timestamps and comments.
    *   **QuizLog**: Detailed academic performance records.
    *   **HomeworkLog**: Completion tracking and effort logs.

3.  **Configuration & Templates**:
    *   **ConditionalFormattingRule**: Reactive styling logic that drives the "Fluid Interaction" UI.
    *   **LayoutTemplate**: Snapshots of student/furniture arrangements.
    *   **QuizTemplate** / **HomeworkTemplate**: Reusable structures for common classroom assignments.

## ⚡ JSON-Backed Flexibility (The "Future-Proofing" Strategy)

To avoid frequent and disruptive schema migrations as the UI evolves, specific entities utilize a hybrid storage approach:

*   **`QuizLog.marksData`** and **`HomeworkLog.marksData`**: Instead of adding a new column for every possible scoring metric (e.g., "Partial Credit", "Late Penalty", "Oral Fluency"), these entities store complex scoring maps as **JSON strings**.
*   **Benefits**: This allows the application to support dynamic scoring types and experimental metrics without altering the underlying SQLite tables, ensuring better compatibility between different app versions.

## ⛓️ Referential Integrity & Cascading

The schema enforces strict relational hardening through Room's `ForeignKey` constraints:
*   **Cascading Deletes**: If a `Student` is removed, all associated `BehaviorEvents`, `QuizLogs`, and `HomeworkLogs` are automatically purged via `onDelete = ForeignKey.CASCADE`.
*   **Nullification**: If a `StudentGroup` is deleted, member students are automatically ungrouped (`onDelete = SET_NULL`).

## 👑 The Repository Coordinator (`StudentRepository.kt`)

The `StudentRepository` acts as the single source of truth for the ViewModels, orchestrating operations across all DAOs.

*   **BOLT (Performance-Obsessed) Fetching**: The repository implements optimized fetch logic that pushes filtering (by date, group, or student ID) directly to the SQLite layer. This avoids loading thousands of logs into memory just to filter them in the JVM.
*   **Unified Stream Management**: It merges reactive `Flow` and `LiveData` streams, providing the ViewModels with a consistent API for both one-shot actions (adding a student) and long-running observations (live session progress).

---
*Documentation love letter from Scribe 📜*
