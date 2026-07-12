# 🌌 Ghost Warp: Neural Spacetime Dilation

## 📖 Overview
The **Ghost Warp** experiment implements a "Neural Spacetime Dilation" metaphor for classroom activity. It models students as gravitational masses within a social spacetime fabric, where their "mass" is determined by the intensity and recency of their behavioral logs.

This experiment visualizes the "curvature" of the classroom, allowing teachers to identify areas of high behavioral energy or turbulence that "warp" the background environment.

## 🏛️ Architecture

- **`GhostWarpEngine`**: The logical core. It analyzes behavioral logs to calculate "Mass" and "Curvature" for each student.
    - **Mass Calculation**:
        - **Recency**: Logs within the last 60 minutes are weighted **2.0x** more heavily.
        - **Negativity Bias**: Negative behavioral logs contribute **1.5x** more to the warp than positive logs.
    - **Curvature Synthesis**: Normalizes mass into a 0.0-1.0 curvature index.
- **`GhostWarpShader`**: An AGSL procedural shader that implements the visual distortion.
    - **Inverse-Square Gravity**: Uses a smoothed inverse-distance squared model to distort UV coordinates near high-mass nodes.
    - **Neural Grid**: Renders a distorted background grid that reveals the underlying curvature.
- **`GhostWarpLayer`**: A high-performance Compose layer that bridges the engine and shader.
    - **Coordinate Mapping**: Translates logical 4000x4000 coordinates into screen-space uniforms for the AGSL program.
- **`GhostWarpDialog`**: A glassmorphic reporting interface that displays the Markdown-formatted spacetime analysis and allows for data export.

## ⚡ BOLT Performance Strategy

- **Zero-Allocation Uniforms**: `GhostWarpLayer` utilizes a `remember`-ed `FloatArray` to batch student mass points into a single GPU call, eliminating per-frame allocations.
- **Memoized Gravitation**: Gravity points are only recalculated when student data or behavior logs change, preserving 60fps during standard canvas interactions.
- **Smoothed Singularities**: The shader includes a smoothing constant (`0.1`) in the denominator of the gravity formula to prevent visual glitches or "divide-by-zero" artifacts at the exact center of a student node.

## 🔄 Logic Parity
The curvature analysis is architecturally aligned with the **Python R&D prototype** (`Python/ghost_warp_analysis.py`), ensuring that the spacetime metrics remain consistent across the desktop and mobile ecosystems.

---
*Documentation love letter from Scribe 📜*
