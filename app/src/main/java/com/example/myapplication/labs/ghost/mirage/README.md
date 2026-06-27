# 🌫️ Ghost Mirage: Neural Focus Tracking

Ghost Mirage is an experimental spatial analysis tool that tracks and visualizes teacher attention within the classroom. By mapping interactions into a persistent neural heatmap, it surfaces "spatial bias," helping educators identify which students or areas of the classroom receive the most (or least) focus over time.

## 🧠 The Metaphor

In a busy classroom, a teacher's attention is a finite resource. It's natural to gravitate toward certain "active" zones while others fall into a "blind spot." Ghost Mirage acts as a digital mirror, reflecting the temporal and spatial density of teacher focus as an ethereal, shimmering fog.

## 🛠️ Technical Architecture

The system is built on a high-performance **Grid-based Thermal Model** that balances spatial resolution with GPU efficiency.

### 1. Spatial Coordinate Mapping
The seating chart operates on a **4000x4000 logical canvas**. To optimize performance and adhere to AGSL uniform limits, the `GhostMirageEngine` downsamples this space into a **20x20 primitive grid** (400 cells total).

- **Inversion Logic**:
  - `col = (x / 4000f * 20).toInt()`
  - `row = (y / 4000f * 20).toInt()`
- **Intensity Mapping**: Each "focus hit" (e.g., a tap on a student icon or a behavioral log event) increments the grid cell's intensity by a default weight of `0.2f`, capped at `1.0f`.

### 2. Temporal Decay Algorithm
To ensure the heatmap reflects current activity rather than the entire session's history, the engine implements a **linear temporal decay model**.
- **Decay Rate**: Defaults to `0.05f` units per second.
- **Fading**: Focus areas naturally "evaporate" over time, encouraging the teacher to re-engage with quiet zones to keep the heatmap balanced.

### 3. AGSL Neural Shader (`MIRAGE_HEATMAP`)
The visualization is rendered using an advanced **AGSL (Android Graphics Shading Language)** shader on Android 13+ (API 33).

- **Domain Warping**: The shader uses domain-warped **Fractal Brownian Motion (FBM)** noise to create a "shimmering" effect. This ensures the heatmap feels alive and futuristic, rather than a static gradient.
- **Color Palette**: Uses a blend between **Deep Cyan** (`#00334D`) for low intensity and **Amber Ghost** (`#FFCC33`) for peak focus areas.
- **Pulse Dynamics**: Incorporates a subtle sine-wave shimmer driven by `iTime` to simulate the "mirage" effect.

## ⚡ BOLT Performance Optimizations

Ghost Mirage is designed for zero-impact on the 60fps seating chart experience:
- **Zero-Allocation Sync**: The engine uses primitive `FloatArray`s and manual index-based loops to avoid iterator overhead and GC pressure during state updates.
- **Uniform Batching**: All 400 grid points are passed to the GPU in a single `float iFocusGrid[400]` array, minimizing JNI overhead.
- **Object Pooling**: The `GhostMirageLayer` hoists `RuntimeShader` and `ShaderBrush` objects into `remember` blocks to prevent per-frame allocations.

## 🚀 Integration

Ghost Mirage is automatically triggered by:
1. **Direct Focus**: Tapping on the seating chart background calls `recordFocus(x, y)`.
2. **Behavioral Logging**: Logging an event for a student automatically records a focus hit at that student's coordinates.

---
*Documentation love letter from Scribe 📜*
