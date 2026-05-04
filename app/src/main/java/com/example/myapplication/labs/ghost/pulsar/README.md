# 📡 Ghost Pulsar: Harmonic Synchronicity & Classroom Rhythm

Ghost Pulsar is an experimental visualization module that models student activity as a series of harmonic oscillations. By analyzing the frequency of behavioral events, the engine identifies "Classroom Rhythms" and visualizes social synchronicity through wave interference patterns.

## 🏛️ The "Harmonic Synchronicity" Model

The core metaphor of Ghost Pulsar is that every student contributes to the "pulse" of the classroom.

### 1. Rhythmic Parameters
- **Frequency (Logs Per Minute)**: Calculated within a sliding **10-minute (600,000ms)** window. A student with frequent logs has a higher frequency, resulting in faster visual pulses.
- **Amplitude (Log Density)**: Represents the "strength" of the student's current harmonic state. It scales with the number of logs, capped at **1.5x** to prevent visual overwhelming.
- **Phase**: Derived from the system clock and frequency. This ensures that students with the same frequency pulse in unison, even if their logs occurred at slightly different times.

### 2. Harmonic Bonds (Synchronicity)
The engine detects **Harmonic Bonds** when two students have a frequency delta of less than **0.2 LPM**.
- **Sync Score**: Calculated as `1.0 - delta`, representing how closely "in sync" two students are.
- **Social Inference**: High synchronicity often indicates collaborative clusters, shared engagement levels, or localized behavioral "echoes."

## 🛠️ Implementation Architecture

### 1. Ghost Pulsar Engine (`GhostPulsarEngine.kt`)
The logical brain of the experiment.
- **BOLT Optimization**: Groups events by `studentId` ($O(N+L)$) to ensure frequency analysis doesn't lag the UI thread.
- **Python Parity**: Maintains strict algorithmic alignment with `Python/ghost_pulsar_analyzer.py` for frequency calculation and bond detection.

### 2. Ghost Pulsar Layer (`GhostPulsarLayer.kt`)
The Compose rendering bridge.
- **Zero-Allocation Loop**: Pre-allocates `FloatArray` buffers for student points, phases, and amplitudes to achieve 60fps stability.
- **Item Capping**: Samples the first 20 students to maintain performance on mid-range hardware while still conveying the classroom's global rhythm.

### 3. Ghost Pulsar Shader (`GhostPulsarShader.kt`)
An AGSL-powered visualizer.
- **Wave Interference**: Renders students as wave sources. When synchronized students are near each other, their waves exhibit constructive interference (brighter glows).
- **Dynamic Color Shifting**: Uses a `mix` of Cyan (Positive) and Magenta (Negative/Active) based on the current harmonic phase.
- **Digital Aesthetics**: Includes a subtle scanline effect to maintain the "Ghost Lab" tactical HUD aesthetic.

## 🐍 Logic Parity

This module is a mobile-optimized port of the **Ghost Pulsar Analysis Suite**.
- **Reference**: `Python/ghost_pulsar_analyzer.py`
- **Constant Alignment**: The **0.2 LPM threshold** and **10-minute window** are hardcoded across both platforms to ensure that a classroom report generated on Android matches the analysis from the Python desktop suite.

---
*Documentation love letter from Scribe 📜*
