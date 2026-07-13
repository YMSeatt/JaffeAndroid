# ⚡ Ghost Glitch: Neural Spatial Feedback

Ghost Glitch is an experimental feedback system that visualizes "Spatial Tension" in the classroom. It provides real-time digital distortion when students are placed in conflicting or overlapping positions on the seating chart.

## 🌟 The Vision
In a fluid, multi-modal classroom interface, data should react to physical conflicts. Ghost Glitch turns "bad placement" into a "neural feedback loop," using visual and haptic cues to guide the teacher toward optimal spatial organization.

## 🛠️ Components

### 1. Spatial Tension Engine (`GhostGlitchEngine.kt`)
A geometric analysis engine that detects overlaps and proximity alerts.
- **Conflict Detection**: Calculates the "Spatial Tension" between student icons.
- **BOLT ⚡ Optimization**: Uses squared distance heuristics to minimize computational overhead during high-frequency drag operations.

### 2. Neural Glitch Shader (`GhostGlitchShader.kt`)
An **AGSL Shader** that provides real-time visual corruption feedback.
- **Chromatic Aberration**: Splits RGB channels proportionally to the conflict intensity.
- **Digital Corruption**: Introduces procedural noise and scanline displacement.
- **Neural Cyan/Magenta**: Utilizes the Ghost Lab color palette to signify system-level feedback.

### 3. Ghost Glitch Layer (`GhostGlitchLayer.kt`)
A high-performance Compose layer that overlays the seating chart.
- **Dynamic Intensity**: Glitch visuals scale from subtle chromatic shifts to full digital corruption as icons overlap.
- **Haptic Sync**: (Integrated in `SeatingChartViewModel`) Triggers "Neural Friction" haptic pulses via `GhostHapticManager`.

## 🚀 Activation
Ghost Glitch is active whenever `GhostConfig.GLITCH_MODE_ENABLED` is true and students are moved into close proximity (Distance < 160 units).

---
*Ghost - Rapid Prototyping for the Classroom of 2027*
