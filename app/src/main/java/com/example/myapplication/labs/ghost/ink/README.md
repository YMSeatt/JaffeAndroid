# 🖋️ Ghost Ink: Persistent Spatial Annotations

Ghost Ink provides an interactive surface for teachers to draw persistent, glowing annotations directly onto the seating chart. Unlike standard drawing tools, Ghost Ink is "Data-Aware" and spatially anchored to the classroom's logical coordinate system.

## 🏛️ Architecture

The Ink system is built on a three-tier architecture:

1.  **`GhostInkEngine` (The Logic)**:
    - **Spatio-temporal Storage**: Strokes are stored as a list of `Offset` points in a **4000x4000 logical coordinate space**. This ensures that drawings remain perfectly anchored to desks and students regardless of the device resolution or zoom level.
    - **History Management**: Supports `undo()` and `clearAll()` operations for rapid iteration during live instruction.

2.  **`GhostInkLayer` (The Surface)**:
    - **Coordinate Transformation**: Intercepts screen-space touch events and transforms them into logical chart-space coordinates using the current `canvasScale` and `canvasOffset`.
    - **Adaptive Rendering**: Implements a dual-path rendering strategy:
        - **API 33+ (Modern)**: Uses high-performance AGSL shaders and `RuntimeShader` pooling.
        - **Legacy**: Falls back to standard `Canvas` path drawing for older devices.

3.  **`GhostInkShader` (The Aesthetic)**:
    - **Energy Ink Metaphor**: Renders lines with a "Neural Glow" and procedural "Data Flicker" artifacts, simulating a futuristic digital chalkboard.

## ⚡ BOLT Performance Optimizations

Drawing high-fidelity lines in a reactive UI requires aggressive performance management:

- **Distance-Based Thinning**: The engine implements a **25f squared-distance threshold** for incoming points. If a new point is too close to the previous one, it is discarded. This prevents "Point Explosion" and keeps the stroke data lean.
- **Shader & Brush Pooling**: To avoid the massive overhead of re-allocating GPU resources every frame, `GhostInkLayer` maintains a pool of **16 pre-allocated RuntimeShaders**. Each active stroke is assigned a shader from the pool.
- **Zero-Allocation Buffers**: Point data is passed to the GPU via a **pre-allocated `FloatArray` buffer**, eliminating thousands of per-frame object allocations during complex drawing gestures.
- **Manual Bitwise Extraction**: The layer extracts ARGB components from long-format colors using manual bitwise operations, bypassing the overhead of the `Color` object lifecycle.

## 🛡️ Shield Security & Hardening

- **Temporary Persistence**: Ink annotations are currently transient and kept in memory within the `GhostInkEngine`. This ensures that sensitive spatial notes don't inadvertently leak into permanent backups unless explicitly integrated into the `GhostMemento` archival system.

---
*Documentation love letter from Scribe 📜*
