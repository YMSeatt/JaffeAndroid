# 🖥️ Seating Chart UI & View Orchestration

This package contains the primary screen-level Composables for the application, with a focus on the immersive, high-performance seating chart experience.

## 👑 The Central Hub: `SeatingChartScreen.kt`

The `SeatingChartScreen` is the application's most complex UI component. It manages a multi-layered canvas that integrates standard classroom management tools with over 60 experimental "Ghost Lab" visualizations.

### 📐 The 4000x4000 Logical Canvas
To ensure consistency across devices of varying resolutions and aspect ratios, the seating chart operates on a fixed **4000x4000 logical coordinate system**.
- **Normalization**: Student and furniture positions are stored in these logical units.
- **Transformation**: The UI layer applies scale (zoom) and offset (pan) transformations to map these logical units to actual screen pixels.
- **Blueprint Parity**: This coordinate system matches the scaling logic used in the [GhostBlueprintEngine](../../labs/ghost/GhostBlueprintEngine.kt) for SVG generation.

### 🖖 Fluid Interaction Model
The screen implements a custom gesture handling system designed for high-frequency interaction (dragging students, zooming, and radial menu activation).
- **Optimistic Updates**: Student positions are updated in local Composable state for immediate 60fps visual feedback, while the `SeatingChartViewModel` asynchronously persists the changes to the database via the [Command Pattern](../../commands/README.md).
- **Z-Order Management**: Dozens of AGSL shader layers (Aurora, Flux, Singularity, etc.) are orchestrated using a strict Z-order to ensure that interactive elements remain accessible while background visualizations provide data-driven context.

---

## ⚡ The 3-Stage Transformation Pipeline

To maintain 60fps performance while processing thousands of historical logs and dozens of conditional formatting rules, the `SeatingChartViewModel` utilizes a specialized **BOLT-optimized** pipeline to prepare students for display:

### 1. Stage 1: Data Pre-processing (Memoized)
Calculates global classroom metrics and groups logs by student ID.
- **Why**: Avoids $O(L)$ filtering inside the student loop.
- **Output**: Maps of `studentId -> List<Logs>`, `studentId -> PerformanceMetrics`.

### 2. Stage 2: Per-Student Transformation (Memoized)
Applies conditional formatting rules and resolves individual UI properties.
- **Why**: Evaluates complex logic (Regex, date math, score thresholds) once per student.
- **Identity Preservation**: Uses a `LruCache` to return the same `StudentUiItem` instance if student data and rules haven't changed, enabling Compose's **Skippability** optimization.

### 3. Stage 3: State Sync & Identity Preservation
Synchronizes volatile UI state (like `isSelected` or `isDragging`) with the prepared items.
- **Why**: Ensures that high-frequency UI state changes don't trigger a full re-calculation of the Stage 2 logic.

---

## 👻 Ghost Lab Integration

The `SeatingChartScreen` acts as the primary host for the **Ghost Lab** experimental suite. Features are integrated as optional overlays that can be toggled via the [Ghost Hub](../../labs/ghost/hub/README.md).

- **Performance Hardening**: High-intensity layers (like Ghost Flux or Ghost Spark) utilize **Shader Pooling** and **Primitive Array Uniforms** to minimize JNI overhead and garbage collection pressure during the draw pass.
- **Privacy Shield**: The screen automatically enforces `FLAG_SECURE` whenever sensitive experiments (like the Voice Assistant or AI Prophecies) are active, preventing unauthorized screen capture of PII.

---
*Documentation love letter from Scribe 📜*
