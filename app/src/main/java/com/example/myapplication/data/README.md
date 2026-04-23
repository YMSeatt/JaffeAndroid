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

## 🧪 Normalized Assignment Model (Modern) vs. Legacy Log Model

The application is currently transitioning from a flat, log-based history to a normalized, template-based assignment system.

### 1. Legacy Log Model (`QuizLog`, `HomeworkLog`, `BehaviorEvent`)
*   **Architecture**: Optimized for rapid, unstructured data entry. Every log is self-contained.
*   **Flexibility**: Uses **JSON-Backed Storage** (`marksData`) for granular scoring metrics. This prevents frequent schema migrations as UI reporting needs change.
*   **Use Case**: Best for quick "one-off" notes or historical records where strict template adherence isn't required.

### 2. Normalized Assignment Model (`Quiz`, `Homework`, `QuizTemplate`, `HomeworkTemplate`)
*   **Architecture**: Highly structured and relational. Assignments are instantiated from reusable **Templates**.
*   **Consistency**: Ensures that assessments (like a "Unit 1 Quiz") follow a consistent schema (number of questions, specific scoring steps) across all students.
*   **Benefits**: Easier to perform longitudinal analysis on specific assessments and supports multi-step homework check-ins (Checkboxes, Scores, and Comments).
*   **Use Case**: Preferred for standardized classroom assessments and structured checking routines.

## ⚡ JSON-Backed Flexibility (The "Future-Proofing" Strategy)

To avoid frequent and disruptive schema migrations as the UI evolves, specific entities utilize a hybrid storage approach:

*   **`QuizLog.marksData`** and **`HomeworkTemplate.marksData`**: These store complex scoring maps or multi-step assignment structures as **JSON strings**.
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
