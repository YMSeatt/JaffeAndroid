# 📥 Data Ingestion & Cross-Platform Synchronization

This package serves as the architectural bridge between the Python desktop application and the Android mobile application. It ensures data portability and maintains relational integrity during complex ingestion and migration operations.

## 🏛️ Ingestion Architectures

The system utilizes two distinct strategies for ingesting classroom data:

1.  **Unified Ingestion (`Importer.kt`)**: The modern standard. It processes a single "v10" JSON file (`ClassroomDataDto`) containing the entire classroom state (students, furniture, and all logs). This is the preferred method for full-classroom backups and synchronization.
2.  **Fragmented Ingestion (`JsonImporter.kt`)**: A legacy bridge. It facilitates the sequential import of discrete JSON files (e.g., separate files for behaviors, groups, and students). This is used for older desktop exports or partial data updates.

## 🔄 Technical Constraints & Parity

### 📐 Logical Coordinate Mapping
The Python and Android applications utilize different logical canvas dimensions. To maintain layout consistency, the ingestion logic handles implicit transformations:

-   **Python Canvas**: 2000x1500 logical pixels.
-   **Android Canvas**: 4000x4000 logical pixels.

**Ingestion Rule**: Modern importers map Python coordinates 1:1 into the top-left quadrant of the Android canvas. Experimental features, such as the **Ghost Blueprint Engine**, apply a normalization factor of `/ 4` and a fixed offset to project these coordinates into a unified 1200x800 SVG blueprint frame.

### 🔐 Cryptographic Glue (`FALLBACK_KEY`)
To support the transition from desktop to mobile, `SecurityUtil` maintains a `FALLBACK_KEY`.
-   **Role**: This hardcoded key matches the legacy shared secret used by the Python application.
-   **Migration**: During ingestion, if the modern hardware-backed key fails to decrypt a file, the system attempts a fallback using this key. Upon success, the data is immediately re-encrypted using the Android KeyStore-managed key for enhanced security.

## ⛓️ Referential Integrity (Multi-Pass Strategy)

To ensure that relational data is correctly linked in Android's local Room database, the `Importer` utilizes a strict multi-pass processing order:

1.  **Pass 1: Foundational Entities**: Student Groups and Students are imported first. This pass builds an in-memory mapping between Python's string-based UUIDs and Android's auto-incrementing `Long` primary keys.
2.  **Pass 2: Spatial Entities**: Furniture items are ingested.
3.  **Pass 3: Log Ingestion**: Behavior, Quiz, and Homework logs are processed. The ID mapping from Pass 1 is used to resolve foreign key relationships in real-time.

---
*Documentation love letter from Scribe 📜*
