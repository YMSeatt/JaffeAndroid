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

---
*Documentation love letter from Scribe 📜*
