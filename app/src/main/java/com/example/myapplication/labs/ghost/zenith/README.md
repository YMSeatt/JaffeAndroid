# 🌌 Ghost Zenith: Neural Depth & Parallax Modeling

## Overview
**Ghost Zenith** is an experimental visualization that transforms the 2D seating chart into a multi-layered 3D "Neural Sea." By mapping student academic and behavioral data to vertical elevation (Altitude) and utilizing the device's hardware sensors for real-time parallax, it creates a physical hierarchy of student needs.

## The "Neural Zenith" Metaphor
In this mode, the classroom is no longer a flat plane. It is a deep, luminous environment where students "float" at different depths based on their current classroom standing.

### 🎈 Student Altitude (Buoyancy)
Each student is assigned an **Altitude** value [0.0 to 1.0], which determines their Z-axis depth in the spatial field.
- **Formula**: `(Academic Score * 0.7) + (Behavior Stability * 0.3)`
- **High Altitude (Close to the surface)**: Students with high academic performance and stable behavior float higher, appearing larger and moving less during device tilt.
- **Low Altitude (Submerged)**: Students requiring more support sink deeper into the "Neural Sea," exhibiting significant parallax displacement when the device is moved.

## 🛰️ Spatial Mechanics

### 3D Parallax System
The engine tracks the device's **Pitch** (X-axis) and **Roll** (Y-axis) using the rotation vector sensor. This data is filtered and mapped to three visual effects:
1. **Z-Translation**: Objects with lower altitude are projected further "behind" the glass.
2. **Parallax Offset**: Deep nodes move more aggressively in response to tilt (`tilt * 100f * (1.0 - altitude)`), creating a sense of 3D volume.
3. **Dynamic Rotation**: The entire container rotates up to 20 degrees, following the user's perspective.

### 🧪 The Neural Sea (AGSL Shader)
The background is rendered using a specialized AGSL program that simulates three distinct currents:
- **Layer 1 (Deep)**: Slow-moving, low-parallax currents representing long-term classroom trends.
- **Layer 2 (Mid)**: Medium-speed luminescence representing active engagement.
- **Layer 3 (Surface)**: Fast, high-parallax ripples reacting instantly to device movement.

## Implementation Details
- **Engine**: `GhostZenithEngine.kt` manages sensor lifecycles and low-pass filtering (80% persistence) for smooth movement.
- **Layer**: `GhostZenithLayer.kt` provides the `ZenithScope` and `graphicsLayer` transformations.
- **Shader**: `GhostZenithShader.kt` implements the multi-layer FBM (Fractal Brownian Motion) noise background.
- **Parity**: Mathematical alignment is maintained with `Python/ghost_zenith_analysis.py`.

---
*Documentation love letter from Scribe 📜*
