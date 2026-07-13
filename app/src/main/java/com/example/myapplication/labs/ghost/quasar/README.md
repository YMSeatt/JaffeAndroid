# 🌀 Ghost Quasar: High-Energy Focal Points

Ghost Quasar is an experimental visualization layer that identifies and highlights "High-Energy" students in the classroom ecosystem. By analyzing the density and polarity of behavioral logs in real-time, it identifies students who are currently focal points of activity.

## 🌌 The Metaphor: Classroom Quasars
In astronomy, a quasar is an extremely luminous active galactic nucleus. In Ghost Lab, a **Quasar Student** is one who has a high frequency of recent behavioral events, creating a "Gravity Well" of social or academic energy.

- **Energy**: Represents the intensity or frequency of recent logs.
- **Polarity**: Represents the "charge" of the energy—Cyan for positive engagement, Magenta for negative or disruptive bursts.
- **Accretion Disk**: The visual manifestation of this energy, rendered as a swirling, pulsing disk around the student icon.

## 🧠 Logical Engine (`GhostQuasarEngine.kt`)
The engine identifies Quasars using a spatio-temporal analysis of the behavior log stream.

### 1. Sliding Window Analysis
- **Window**: 30 minutes. Only logs within the last 30 minutes contribute to Quasar status.
- **Threshold**: A student must have at least **3 logs** within the window to trigger the Quasar effect.

### 2. Metric Calculation
- **Energy**: Calculated as `Total Logs / 10`, capped at `1.0`.
- **Polarity**: Calculated as `(Positive - Negative) / Total`.
    - `+1.0`: Purely positive energy (Cyan).
    - `-1.0`: Purely negative energy (Magenta).
    - `0.0`: Balanced or neutral energy.

## 🎨 Visualization (`GhostQuasarLayer.kt` & `GhostQuasarShader.kt`)
The Quasar effect is rendered using a high-performance **AGSL Shader** (`ACCRETION_DISK`).

### Visual Components:
- **Pulsing Core**: The disk expands and contracts based on a sine-wave temporal function.
- **Swirl Effect**: A procedural swirl animation represents the "rotation" of social momentum.
- **Dynamic Radius**: The screen-space size of the disk scales with the calculated `iEnergy`.

## ⚡ BOLT Optimizations
To ensure a fluid 60fps experience even with many active Quasars:
- **O(Recent) Performance**: The engine assumes logs are sorted DESC and uses an early-exit break once it moves past the 30-minute window.
- **Shader Pooling**: `GhostQuasarLayer` maintains a growable pool of `RuntimeShader` and `ShaderBrush` objects to prevent "Uniform Overwrite" bugs and minimize JNI overhead.
- **Frame-Rate Stability**: Uses `rememberInfiniteTransition` to drive animations, moving state updates out of the main recomposition path.
- **Manual Loops**: Replaces high-level collection operators with manual index-based loops in the drawing pass.

---
*Documentation love letter from Scribe 📜*
