# ⚙️ Logical Engines & Core Utilities

This package contains the foundational "Logical Engines" that drive the Seating Chart application. These utilities handle high-performance spatial calculations, complex data transformations, and critical security operations.

## ⚡ The "BOLT" Philosophy
Performance is treated as a first-class feature in this package. To ensure a fluid 60fps experience—even during expensive operations like student dragging or live quiz sessions—these utilities adhere to the **BOLT** (Performance-Obsessed) design patterns:

- **Zero-Allocation Loops**: High-frequency rendering paths (e.g., in `CollisionDetector`) avoid object allocations to minimize Garbage Collection (GC) pressure.
- **Memoization & Caching**: Expensive operations, such as JSON parsing in the `ConditionalFormattingEngine` or string similarity calculations, use `LruCache` or `ThreadLocal` buffers to reuse results and resources.
- **O(1) Data Structures**: Whenever possible, flat lists are transformed into Maps or Sets in background threads to allow constant-time lookups during UI updates.
- **Primitive Optimization**: Engines like the `GhostCognitiveEngine` (documented in `labs/ghost`) and various utilities use primitive arrays (`FloatArray`, `IntArray`) to bypass the overhead of boxed types.

## 🏛️ Key Components

### 1. Conditional Formatting Engine (`ConditionalFormattingEngine.kt`)
The brain behind the seating chart's dynamic styling.
- **Reactive Styling**: Evaluates complex conditions (behavior counts, quiz scores, live session responses) to resolve student visual states.
- **Optimization**: Uses a multi-stage decoding process where rules are pre-processed into `DecodedConditionalFormattingRule` objects, moving expensive logic out of the rendering loop.

### 2. Collision Detector (`CollisionDetector.kt`)
Manages the spatial organization of student icons.
- **Greedy Placement**: Implements a column-based algorithm that automatically finds the next best available spot for new students, ensuring a compact layout.
- **Performance**: Optimized to resolve collisions for dozens of students in sub-millisecond time.

### 3. Security Manager (`SecurityUtil.kt`)
The central authority for data confidentiality and identity verification.
- **Cryptographic Glue**: Maintains a `FALLBACK_KEY` that acts as a bridge between the Python desktop application and the Android app, ensuring seamless data migration.
- **Hardened Storage**: Leverages the Android KeyStore (`EncryptedFile`) for modern, hardware-backed key protection while maintaining legacy compatibility via multi-format password verification.

### 4. Email Reporting Subsystem (`EmailUtil.kt` & `EmailWorker.kt`)
Coordinates the generation and secure transmission of classroom reports.
- **Background Coordination**: Utilizes `WorkManager` to offload heavy Excel generation and SMTP communication to background threads.
- **Privacy First**: Enforces PII encryption at the worker boundary and ensures deterministic cleanup of temporary report files.

### 5. String Similarity (`StringSimilarity.kt`)
A performance-tuned fuzzy matching utility.
- **Levenshtein Distance**: Used to match student names when loading layout templates if database IDs have changed.
- **Resource Reuse**: Employs `ThreadLocal` integer buffers to eliminate row allocations during similarity calculations.

### 6. Excel Import Utility (`ExcelImportUtil.kt`)
A robust entry point for bulk student data ingestion.
- **Dynamic Header Mapping**: Uses a curated alias map to detect student fields (names, gender, groups) regardless of spreadsheet column naming.
- **Heuristic Name Parsing**: Gracefully handles various name formats (combined vs. split) to minimize user data-cleaning effort.
- **N+1 Avoidance**: Pre-loads student groups into memory to ensure high-performance ID resolution during batch imports.

## 🖼️ Visual Serialization & Artifact Generation

The application includes a sophisticated engine for transforming GPU-rendered UI components into shareable visual artifacts. This is used for generating seating chart snapshots and blueprints.

### 1. The Capture Engine (`Screenshot.kt`)
Traditional `View.draw(Canvas)` methods often fail to capture complex Compose layers or AGSL shaders.
- **Hardware Copy**: Utilizes the `PixelCopy` API (API 26+) to perform a hardware-level copy of the system surface buffer. This ensures that what the user sees—including "Ghost Lab" procedural glows and animations—is accurately captured.
- **Precision Framing**: Dynamically calculates the view's relative position within the window to ensure pixel-perfect cropping.

### 2. Logical Blueprinting (`GhostBlueprintEngine.kt`)
Bridges the gap between the 4000x4000 logical canvas and standard vector formats.
- **SVG Serialization**: Transforms student and furniture entities into a stylized, scalable vector format suitable for professional printing or classroom planning.
- **Coordinate Normalization**: Implements a consistent mapping formula to project large-scale canvas data into a standard 1200x800 SVG viewport.

---
*Documentation love letter from Scribe 📜*
