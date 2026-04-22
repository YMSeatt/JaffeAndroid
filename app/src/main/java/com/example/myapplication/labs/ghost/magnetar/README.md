# 🧲 Ghost Magnetar: Social Magnetic Field Analysis

## 🪐 The Metaphor
**Ghost Magnetar** models the classroom's social dynamics as a complex magnetic field. Every student acts as a **social dipole**, contributing to a global "Social Polarity" that shifts in real-time based on their behavioral history.

- **North Pole (+)**: Represented by positive behavioral events (Cyan).
- **South Pole (-)**: Represented by negative behavioral events (Magenta).
- **Magnetic Flux**: Visualized as streamlines connecting students, showing the invisible "tensions" and "attractions" within the seating chart.

## 🧠 Logic & Social Weighting
The engine implements an **Inverse-Square Magnetic Field** ($1/r^2$) simulation.

A critical heuristic in the Magnetar engine is the **Social Weighting**:
- **Negative behaviors exert 1.5x more magnetic pull** than positive behaviors.
- This simulates the "Negativity Bias" in social groups, where disruptive actions often have a disproportionately large impact on classroom cohesion and the "feel" of the environment.

## 🛠 Technical Implementation

### AGSL Shader Visualization
The `GhostMagnetarShader` uses Android Graphics Shading Language (AGSL) to render the field lines.
- **Line Integral Convolution (LIC)**: A simplified version is used to simulate "iron filings" aligning with the field vectors.
- **External Field Skewing**: The device's physical **Magnetometer** (Sensor.TYPE_MAGNETIC_FIELD) is used to skew the virtual social field. Rotating the tablet physically rotates the visual streamlines, metaphorically "steering" the social analysis.

### BOLT ⚡ Optimizations
To maintain 60fps performance on a high-density seating chart:
- **15-Student Cap**: The AGSL shader is hard-capped to the first 15 students (`iDipolePos[15]`). This prevents uniform buffer overflows and keeps fragment shader pressure manageable.
- **Manual Indexing**: All collection traversals in the `Canvas` and `Engine` use manual index-based loops to eliminate `Iterator` allocations.
- **Background Pipeline**: Heavy behavioral log analysis is offloaded to the `SeatingChartViewModel` background pipeline, providing pre-calculated `magneticStrength` and `magneticRadius` to the layer.

## 📊 Reports
The `GhostMagnetarEngine` generates Markdown reports detailing:
- **Global Status**: STABLE, ACTIVE, or SUPERCHARGED.
- **Quadrant Intensity Map**: Field strength in μG (micro-Gauss) across four quadrants.
- **Polarity Map**: Individual student alignment and contribution.

---
*Part of the Ghost Lab Experimental Suite.*
