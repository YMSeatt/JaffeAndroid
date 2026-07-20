# 🧠 Developer Insights: Tribal Knowledge & Implementation Secrets

This document consolidates the critical "Tribal Knowledge" and technical secrets that drive the Seating Chart & Behavior Logger ecosystem. These insights are essential for maintainers looking to modify core engines or ensure cross-platform compatibility.

## 🔐 Cross-Platform Security Bridge

Ensuring data sync between the Python desktop app and the Android mobile app requires cryptographic parity.

- **The Bridge Key**: `SecurityUtil.kt` (Android) contains a `FALLBACK_KEY` that matches the hardcoded key in Python's `encryption_key.py`. This serves as the "cryptographic glue" for ingestion. Android will automatically migrate ingested data to its hardened, KeyStore-managed key (`fernet.key.v2`).
- **Hash Detection**: Python uses **SHA3-512** (128-char hex strings) for passwords, while Android uses **PBKDF2**. The Android `verifyPassword` method detects the length of the stored hash; if it is 128 characters, it applies the legacy SHA3-512 verification to prevent user lockout during migration.

## 🐍 Python File Splitting

The primary Python script `seatingchartmain.py` (approx. 400KB) is physically split into `First Half.py` and `Second half.py`.
- **Rationale**: This was done to circumvent legacy file size limitations in certain deployment or editing environments.
- **Maintenance**: Both "half" files should be kept in sync with the combined `seatingchartmain.py`, as they represent the same logical application state.

## 🗄️ Database Schema Evolution

The application's Room database has evolved through numerous architectural shifts to support increasing complexity.

- **Relational Hardening**: The schema transitioned from simple string-based group assignments (v6) to a robust relational model with `Long` primary keys (v7/8). This ensures data integrity when students are moved between groups or deleted.
- **JSON-Backed Flexibility**: To avoid frequent schema migrations for UI-driven changes, log entities (`HomeworkLog` v11, `QuizLog` v12) utilize JSON-based `marksData` fields. This allows the application to support dynamic scoring types (e.g., "Partial Credit", "Effort Marks") without altering the underlying SQLite tables.
- **Automated Lifecycle**: Modern versions introduce automated subsystems like `Reminder` (v23) and `EmailSchedule` (v24), which operate as independent background workers driven by specific database triggers.

## 📧 Email Reporting Architecture

The application utilizes a sophisticated background reporting system to ensure data portability and archival without impacting the teacher's interactive experience.

### 1. The Coordination Pipeline
The flow for automated or manual exports follows a strict chain of command:
- **Initiator**: The UI (via `SeatingChartViewModel`) or a system trigger (like `onStop` or a scheduled alarm).
- **Scheduler**: `WorkManager` enqueues a request for the `EmailWorker`.
- **Coordinator**: `EmailWorker` executes on a background thread. It fetches fresh data from the database, coordinates with the `Exporter` to generate the `.xlsx` file, and delegates the final transmission to `EmailUtil`.
- **Transmission**: `EmailUtil` handles the low-level SMTP handshake using JavaMail.

### 2. The Security Boundary
To protect student PII while data is "in transit" within the Android system:
- **WorkData Encryption**: Sensitive fields like the recipient email address, SMTP password, and report subjects are **encrypted** using `SecurityUtil` before being put into the `WorkManager` input map.
- **Worker Decryption**: The `EmailWorker` is the only component authorized to decrypt these values immediately before they are needed for the SMTP session.
- **Temporary Persistence**: Reports generated for email are stored in the app's **private cache directory** (`applicationContext.cacheDir`) and are explicitly deleted in a `finally` block once transmission is attempted.

## 📥 Data Ingestion & Parity

Maintaining data portability between the Python desktop application and the Android mobile app is a core architectural requirement.

- **Unified Standard (v10)**: The modern ingestion pipeline (see [Importer.kt](app/src/main/java/com/example/myapplication/data/importer/Importer.kt)) utilizes a unified JSON schema that encapsulates students, furniture, and all historical logs.
- **Coordinate Transformation**: Both apps use logical coordinates to remain resolution-independent.
    - **Python**: 2000x1500 logical space.
    - **Android**: 4000x4000 logical space.
    - **Mapping**: Data is mapped 1:1 into the top-left quadrant during standard ingestion.
- **Multi-Pass Integrity**: Relational integrity is ensured by a multi-pass strategy (Foundational -> Spatial -> Logs) which builds in-memory ID maps to bridge Python's UUID strings and Android's `Long` primary keys.

For detailed architecture, see the [Importer Package README](app/src/main/java/com/example/myapplication/data/importer/README.md).

## 🧠 ViewModel Architecture & Performance

The ViewModel layer manages the application's UI state and coordinates between the Compose UI and the relational Room database.

- **Unidirectional Data Flow (UDF)**: ViewModels expose state via `LiveData` or `StateFlow` and receive user actions via public methods, ensuring a predictable data cycle.
- **The Central Coordinator**: `SeatingChartViewModel` acts as the primary hub, merging streams from students, logs, groups, and conditional formatting rules into a unified UI state.
- **BOLT (Performance-Obsessed) Patterns**:
    - **Identity Preservation**: Persistent caches of UI items (e.g., `StudentUiItem`) allow Compose to perform property-level "diff-and-patch" updates, maintaining 60fps even during complex drag gestures.
    - **Memoized Pipelines**: Stage 2 of the student update cycle utilizes identity-based caching to avoid redundant re-calculations of formatting or log descriptions when irrelevant data (like item positions) changes.
    - **Optimistic UI**: The icon components update their local `MutableState` immediately during gestures, while the ViewModel handles the asynchronous database synchronization in the background.

---
*Documentation love letter from Scribe 📜*
