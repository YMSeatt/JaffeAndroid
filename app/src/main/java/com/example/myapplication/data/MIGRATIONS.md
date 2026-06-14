# 🗺️ Database Migration Roadmap

This document provides a high-level overview of the evolution of the Seating Chart database schema. It tracks the architectural shifts and schema hardening strategies implemented across 35+ versions of the **Room** database.

## 🏛️ Architectural Eras

### 1. The Foundational Era (v1 – v5)
**Focus**: Establishing the core student model and spatial awareness.
- **v2**: Introduction of logical (x, y) coordinates for student placement.
- **v3**: Initial implementation of the `HomeworkLog` entity.
- **v4 – v5**: Expanded visual customization (colors, sizes) and student initials for the seating chart.

### 2. Relational Expansion & Hardening (v6 – v8)
**Focus**: Moving beyond flat lists to a multi-entity relational model.
- **v6**: Introduction of the `Furniture` entity and student metadata (Gender, Nicknames).
- **v7**: Major expansion including `StudentGroup`, `LayoutTemplate`, and `ConditionalFormattingRule`.
- **v8 (The Great Hardening)**: A critical migration that recreated the core tables to enforce consistent `Long` primary and foreign keys, ensuring data integrity across all relational boundaries.

### 3. The Flexibility Strategy: JSON-Backed Storage (v9 – v12)
**Focus**: Future-proofing the logging system against frequent UI changes.
- **v9 – v10**: Introduction of custom categories and the `QuizMarkType` system.
- **v11 – v12 (JSON Transition)**: Migrated flat scoring columns in `QuizLog` and `HomeworkLog` into flexible `marksData` JSON strings.
    - **Why**: This allows the application to support new scoring metrics and dynamic mark types without requiring a disruptive SQLite schema migration for every UI improvement.

### 4. UI Framework & Customization (v13 – v21)
**Focus**: Enhancing the "Fluid Interaction" UI and administrative controls.
- **v15 – v16**: Advanced typography overrides (Font Family, Size, Color) for students.
- **v17**: Introduction of visual `Guide` lines for precise layout alignment.
- **v19 – v21**: Enhanced `ConditionalFormattingRule` logic and `QuizTemplate` normalization.

### 5. System Automation & Background Services (v22 – v28)
**Focus**: Offloading teacher tasks to the system and securing PII.
- **v22 – v23**: Implementation of the `SystemBehavior` metadata and the `Reminder` system.
- **v24 – v25**: Introduction of automated `EmailSchedule` reporting and the `PendingEmail` queue for reliable background delivery.
- **v26 – v28**: Historical log snapshots and granular export configuration options.

### 6. The Normalized Assessment Era (v29 – v35)
**Focus**: Moving towards a fully relational, template-driven assessment model.
- **v29 – v31**: Introduction of the normalized `Quiz` and `Homework` entities, distinct from the legacy ad-hoc log model.
- **v32 – v34**: Normalization of templates and UI-level administrative controls (Pinned students, Rule toggles).
- **v35**: Introduction of `HomeworkMarkMetadata` for mapping teacher labels to numeric point values.

## 🛡️ Migration & Integrity Principles

1.  **Immutability of History**: Migrations (like **v26**) prioritize capturing snapshots of metadata (e.g., initials) at the time of logging to ensure historical accuracy even if categories are later renamed.
2.  **Safety via Transactions**: All complex migrations are wrapped in `db.withTransaction` (where supported) or executed as atomic SQL blocks to prevent partial schema updates.
3.  **Fallback Patterns**: When altering column types or constraints (as in **v7** or **v32**), the system utilizes a **Create -> Copy -> Drop -> Rename** pattern to ensure data is preserved and constraints are correctly re-applied.
4.  **PII Privacy**: Background entities (like `PendingEmail` and `Reminder`) are designed with privacy in mind, often using ID-only protocols to minimize the persistence of sensitive data in system-level queues.

---
*Documentation love letter from Scribe 📜*
