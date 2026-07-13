# 📊 Excel Reporting & Data Export Architecture

This package contains the application's reporting engine, responsible for transforming longitudinal classroom data into professional, multi-sheet Excel (`.xlsx`) workbooks. It leverages the **Apache POI** library for high-fidelity spreadsheet generation.

## 🏛️ Key Components

### 1. The Exporter (`Exporter.kt`)
The central coordinator for the export process. It orchestrates the transformation of Room entities—Students, BehaviorEvents, QuizLogs, and HomeworkLogs—into a structured Excel format.
- **Multi-Sheet Generation**: Automatically creates dedicated sheets for different log types (Behavior, Quiz, Homework), summary statistics, and individual student dossiers.
- **Attendance Reporting**: Includes a sophisticated attendance tracker that infers student presence based on behavioral and academic activity within a specified date range.

### 2. Export Configuration (`ExportOptions.kt`)
A comprehensive configuration object that allows users (and automated background workers) to define:
- **Filtering**: Date ranges (absolute or relative), student subsets, and specific behavior/homework types.
- **Structure**: Whether to separate logs into sheets, include master logs, or generate individual student workbooks.
- **Security**: Toggles for file-level encryption.

## ⚡ BOLT Performance Optimizations

Generating reports for hundreds of students with thousands of logs can be resource-intensive. The `Exporter` utilizes several **BOLT** (Performance-Obsessed) patterns to maintain efficiency:

- **Identity-Based Grouping**: When generating individual student sheets, the engine groups the pre-sorted master log in a single pass. This maintains chronological order without redundant O(N log N) sorting for each student.
- **Memoized JSON Parsing**: Uses `parsedMarksCache` (a mutable map) to store deserialized JSON mark data from logs, avoiding expensive redundant parsing during bulk operations.
- **Pre-calculated Headers**: Common headers and column indices are pre-calculated once for all student-specific sheets, reducing object allocation and CPU cycles in the rendering loop.
- **Zero-Allocation Formula Triggers**: Uses a pre-allocated primitive `CharArray` for formula trigger detection to minimize GC pressure.

## 🛡️ Shield Security & Hardening

The export subsystem handles sensitive Personally Identifiable Information (PII) and implements several **Shield** security measures:

- **Fernet Encryption**: Exported files can be encrypted using the Fernet specification (AES-128-CBC + HMAC-SHA256), ensuring that data remains protected during distribution or cloud storage.
- **CSV/Excel Injection Prevention**: The `sanitize()` method automatically escapes common formula triggers (`=`, `+`, `-`, `@`) by prepending a single quote (`'`). This prevents malicious input from being executed as a formula when the file is opened in spreadsheet software.
- **Secure File Cleanup**: Temporary report files generated for background email transmission are stored in a restricted `shared/` cache directory and are purged immediately after the transmission attempt, minimizing the window of exposure for unencrypted PII.

---
*Documentation love letter from Scribe 📜*
