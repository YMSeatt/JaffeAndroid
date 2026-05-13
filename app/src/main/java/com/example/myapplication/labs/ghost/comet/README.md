# ☄️ Ghost Comet: High-Momentum Activity Visualization

Ghost Comet is an experimental visualization layer that models classroom activity as high-energy, physical projectiles. It provides a dynamic, streak-based view of behavioral events as they "ripple" through the classroom's social landscape.

## 🚀 Overview

Unlike [Ghost Spark], which uses lightweight particles for immediate feedback, Ghost Comet focuses on **Momentum** and **Trajectory**. When a behavior is logged, a "Comet" is emitted with significant velocity, leaving a glowing, tapering trail that persists as it navigates the seating chart.

## 🛠️ Components

### 1. Comet Physics Engine (`GhostCometEngine.kt`)
Manages the lifecycle and physics of comets using a **Zero-Allocation** model.
- **Momentum & Drag**: Comets preserve velocity but gradually slow down due to a 0.97 friction factor.
- **Social Gravity**: Comets are attracted to student "energy nodes" using an inverse-square law attraction model ($force \propto 1/dist^2$).
- **Trail Mapping**: Each comet maintains a primitive `FloatArray` of its recent positions, enabling high-performance streak rendering without object churn.
- **Object Pooling**: Utilizes a pre-allocated pool of 30 comets to eliminate garbage collection pressure during bursts of activity.

### 2. Neural Streak Shader (`GhostCometShader.kt`)
A custom **AGSL Shader** designed for high-performance rendering of tapering paths.
- **Distance-to-Segment Math**: Calculates the distance from pixels to the comet's trailing segments to create smooth, connected lines.
- **Dynamic Tapering**: The streak's width and glow intensity taper from the "head" (newest position) to the "tail" (oldest position).
- **Chromatic Polarity**: Comets are color-coded by behavior type: Cyan (Positive), Magenta (Negative), and Purple (Academic).

### 3. Comet Layer (`GhostCometLayer.kt`)
The Jetpack Compose integration layer.
- **Coordinate Transformation**: Automatically aligns the logical 4000x4000 comet space with the seating chart's current zoom and pan states.
- **RuntimeShader Integration**: Utilizes Android 13+ `RuntimeShader` for GPU-accelerated rendering.

## 🔄 Integration

Ghost Comet is integrated into the core classroom management pipeline:
- **Automatic Emission**: Logging any behavior or academic mark triggers a comet burst from the student's location.
- **Radial Menu Hub**: Can be toggled via the 'COMET' action in the [Ghost Hub] radial menu.
- **Master Toggle**: Controlled via `GhostConfig.COMET_MODE_ENABLED`.

## ⚡ Performance Standards (BOLT)

To ensure a fluid 60fps experience:
1. **Manual Loops**: Replaces functional iterators in the physics update loop.
2. **Primitive Arrays**: Uses `FloatArray` for trail data and hoisted student coordinates.
3. **Shader Efficiency**: Consolidates trail segments into a single shader pass per comet.

---
*Documentation love letter from Ghost 👻*
