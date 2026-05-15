# 🐚 Ghost Shell: Immersive Neural Dock

## 🛰️ The Metaphor
The **Ghost Shell** is the classroom's "Neural Dock"—a high-fidelity, immersive control interface that resides at the bottom of the seating chart. It acts as both a tactical dashboard for monitoring real-time classroom "Health" and a mission-control hub for activating advanced Ghost Lab experiments.

## 🏛️ Architectural Components

The Ghost Shell system is composed of three primary layers:

### 1. The Metrics Engine (`GhostShellEngine.kt`)
The **Analytical Core** that synthesizes raw behavioral data into actionable metrics.
- **Sliding Window Analysis**: Uses a hardcoded 5-minute (300,000ms) window to focus strictly on the current classroom atmosphere.
- **Health Index**: A normalized value (0.0 to 1.0) derived from the balance of positive vs. negative logs. A value of 0.7 is maintained as a "Stable" fallback for quiet classrooms.
- **Pulse Frequency**: A synthetic frequency (0.5 to 4.0 Hz) that scales with the volume of activity, driving the visual "heartbeat" of the dock.
- **BOLT ⚡ Optimization**: Implements a single-pass manual loop to process logs with zero object allocations, ensuring sub-millisecond calculation times even with hundreds of historical events.

### 2. The Neural Pulse Shader (`GhostShellShader.kt`)
A custom **AGSL Shader** that provides a physical visualization of the classroom's data pulse.
- **Wave Interference**: Uses a triple-sine interference pattern to create an organic, fluid-like movement that avoids predictable mechanical repetition.
- **Data-Driven Visualization**: The shader's frequency and glow intensity are directly linked to the engine's pulse metrics.
- **Color Refraction**: The shader dynamically shifts its color signature toward Red if the Health Index drops below 40%, providing a subtle but effective ambient warning.

### 3. The Immersive Layer (`GhostShellLayer.kt`)
The **User Interface** built with Jetpack Compose.
- **Glassmorphic Surface**: Utilizes `GhostGlassmorphicSurface` for a futuristic, frosted-glass aesthetic that maintains high contrast against the seating chart.
- **Mission Control**: Provides a centralized row of toggles for high-frequency Ghost modes:
    - **HUD**: Tactical Radar and Cognitive Auras.
    - **VISION**: Sensor-driven AR viewport.
    - **BRAIN (Strategist)**: Generative AI tactical engine.
    - **CLIMATE (Aurora)**: Procedural atmospheric visualization.
- **Tactile Feedback**: Integrated with `GhostHapticManager` to provide sharp, clean interaction "clicks" during mode toggling.

## 🔄 Integration

The Ghost Shell is permanently docked at the bottom of the `SeatingChartScreen`. It remains inactive unless `GhostConfig.SHELL_MODE_ENABLED` is true. Its metrics are automatically recalculated whenever the student behavioral log stream emits a new update.

## ⚡ Performance Standards (BOLT)

To maintain a fluid 60fps interaction model on the seating chart:
1. **Zero-Allocation Engine**: The metrics engine avoids functional operators like `filter` or `map`.
2. **Shader Pooling**: The layer reuses its `RuntimeShader` instance across recompositions.
3. **Hoisted State**: Metrics are memoized using `remember(behaviorLogs)` to ensure they are only recalculated when the underlying data changes.

---
*Documentation love letter from Ghost 👻*
