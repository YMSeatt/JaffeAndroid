# 🌟 Ghost Flare: Behavioral Milestone Visualization

Ghost Flare is an experimental visualization layer that celebrates student achievements and behavioral milestones through high-intensity, procedurally generated visual events.

## 🎭 The Metaphor: "Neural Flares"
In the Ghost Lab ecosystem, student activity is treated as a form of energy. A "Flare" represents a peak state where a student's positive engagement reaches a critical threshold, triggering a visual "starburst" that illuminates the seating chart. This serves as a non-verbal, data-driven celebratory cue for the teacher.

## 🛠️ Technical Implementation

### 1. Milestone Heuristics (`GhostFlareEngine.kt`)
The engine monitors the `StudentUiItem` stream in real-time. A flare is triggered based on the following criteria:
- **Quantity**: 3 or more behavioral logs within the active session.
- **Valence**: A `behaviorBalance` exceeding **0.8f** (highly positive).
- **De-duplication**: To prevent visual clutter, the engine ensures only one "young" flare (life > 0.5) is active per student at any time.

### 2. Anamorphic Rendering (`GhostFlareLayer.kt`)
The visualization uses a multi-layered rendering approach:
- **Infinite Transition**: Drives a `time` uniform to animate rays and pulses at 60fps.
- **Coordinate Mapping**: Transforms logical 4000x4000 canvas coordinates into screen-space pixel coordinates for the AGSL shader.
- **Lifecycle Management**: Flares follow a linear decay model (`life` 1.0 -> 0.0), typically fading out over 1.25 seconds.

### 3. AGSL Shader Architecture (`GhostFlareShader.kt`)
The core visual is a custom AGSL shader implementing:
- **Starburst**: Radial rays that rotate and pulse over time.
- **Anamorphic Streak**: A horizontal lens streak simulating high-intensity light refraction.
- **Chromatic Ring**: A dispersive ring effect that shifts toward Cyan/Blue at the edges.

## ⚡ BOLT Performance Optimizations
- **Background Detection**: Milestone scanning is performed using manual index-based loops to eliminate iterator overhead.
- **Shader Hosting**: The `GhostFlareLayer` uses a `remember`-ed `RuntimeShader` instance to minimize JNI overhead and object churn.
- **Deferred Lifecycles**: Engine updates are driven by a `LaunchedEffect` synchronized with the Compose animation clock, ensuring physics and visuals stay in perfect sync without redundant calculations.

---
*Documentation love letter from Scribe 📜*
