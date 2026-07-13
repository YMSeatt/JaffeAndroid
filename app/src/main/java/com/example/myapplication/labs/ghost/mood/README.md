# 🎭 Ghost Mood Board: Neural Classroom Atmosphere

The **Ghost Mood Board** is a futuristic visualization layer that translates classroom data into a dynamic, atmospheric background. It synthesizes behavioral and academic logs to reflect the collective "mood" of the classroom in real-time.

## 🏛️ Architecture & Synthesis

The system operates as a three-tier pipeline:

### 1. The Synthesis Engine (`GhostMoodEngine.kt`)
Analyzes student data using a **15-minute sliding window** to determine individual and classroom mood states.
- **States**:
    - **CALM**: Stable, balanced activity.
    - **FOCUSED**: High academic engagement with zero behavioral turbulence (> 50% academic intensity).
    - **TURBULENT**: High frequency of negative logs (> 20% of students in turbulent state).
    - **ENERGETIC**: High frequency of positive logs (> 30% of students in energetic state).
- **BOLT ⚡ Optimization**: Uses $O(\text{Recent})$ scans of DESC-sorted logs and manual index-based loops to maintain 60fps responsiveness during high-frequency updates.

### 2. The Atmospheric Layer (`GhostMoodLayer.kt`)
A Compose-based layer that renders the synthesized mood as an organic background.
- **Smoothing**: Employs `animateFloatAsState` to smoothly transition between intensities and valences, preventing visual jarring when new logs are added.
- **Color Mapping**:
    - **Turbulent**: Red/Purple palette.
    - **Focused**: Cyan/Blue palette.
    - **Energetic**: Yellow/Magenta palette.
    - **Calm**: Green/Teal palette.

### 3. The Neural Shader (`GhostMoodShader.kt`)
An **AGSL (Android Graphics Shading Language)** shader that generates the fluid visuals.
- **Organic Flow**: Combines multi-octave Voronoi-like noise with temporal oscillations.
- **Data-Driven Distortion**: The `iStability` uniform drives the speed of the flow and the amount of "neural turbulence" (high-frequency noise) in the field.

## ⚡ Performance Patterns

- **Zero-Allocation Rendering**: The `GhostMoodLayer` hoists the `RuntimeShader` and `ShaderBrush` objects, updating uniforms directly in the `Canvas` draw pass.
- **Incremental Cache**: The `SeatingChartViewModel` caches student-level mood parameters to avoid redundant O(N) calculations unless a student's history has actually changed.

---
*Documentation love letter from Scribe 📜*
