# 👻 Ghost Kaleidoscope: Neural Symmetry Visualization

Ghost Kaleidoscope (#82) visualizes classroom harmony and behavioral synchronization as a symmetric, multifaceted radial pattern.

## 🌟 The Vision
A classroom that is "in sync" exhibits high symmetry. Positive behavioral clusters and academic milestones create vibrant, geometric fragments that are mirrored across the canvas. This experiment explores the intersection of social cohesion and radial geometry.

## 🛠️ Components

### 1. Ghost Kaleidoscope Engine (`GhostKaleidoscopeEngine.kt`)
Synthesizes "Neural Fragments" from the last 15 minutes of classroom activity.
- **Fragment Synthesis**: Converts recent logs into shard data (x, y, polarity, intensity).
- **Harmony Index**: Calculates a global symmetry factor (0.1 to 1.0) based on the balance and volume of behavioral events.
- **BOLT Optimization**: Uses a single-pass O(Recent) traversal of pre-grouped logs.

### 2. Ghost Kaleidoscope Shader (`GhostKaleidoscopeShader.kt`)
An AGSL-powered radial symmetry engine.
- **Radial Symmetry**: Maps UV space into multiple mirrored slices (3 to 12 axes) driven by the Harmony Index.
- **Neural Shards**: Renders glowing geometric fragments that rotate and pulse based on classroom momentum.
- **Symmetry Control**: Higher classroom harmony results in higher complexity and more symmetry axes.

### 3. Ghost Kaleidoscope Layer (`GhostKaleidoscopeLayer.kt`)
The Compose UI layer that integrates the visualization into the Seating Chart.
- **Coordinate Mapping**: Translates 4000x4000 logical coordinates into screen-space pixels for the shader.
- **Atmospheric Rendering**: Provides a semi-transparent overlay that reacts to student movement and real-time logging.

## 🔄 Integration
- **Toggle**: Accessible via the 'KALEIDOSCOPE' action in the Ghost Hub (Icons.Default.BlurOn).
- **State**: Driven by `kaleidoscopeFragments` and `harmonyIndex` StateFlows in `SeatingChartViewModel`.

---
*Documentation love letter from Scribe 📜*
