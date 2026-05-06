# 🌌 Ghost Phasing: Neural Backstage Visualization

Ghost Phasing is a visual and tactile experiment that models the classroom interface as a thin veil over a deep data layer, referred to as the **"Neural Backstage."** It allows the UI to "phase" between the physical seating chart and a hidden, generative data void.

## 🧪 Core Concepts

### 1. The Neural Backstage
The experiment posits that every classroom has a "hidden" state composed of raw behavioral signals and academic trajectories. The **Neural Void** (`NEURAL_VOID` shader) represents this state as a deep space background filled with floating "Neural Seeds" and data particle streams.

### 2. Phase Dilation
The "Phase Level" (0.0 to 1.0) determines the transparency and glitch intensity of the physical UI.
- **Phase 0.0**: Standard classroom UI.
- **Phase 0.1 - 0.9**: The UI begins to distort with chromatic aberration and scanline glitches.
- **Phase 1.0**: The physical UI is completely "phased out," revealing only the Neural Backstage.

## 🛠️ Components

### 1. Phasing Engine (`GhostPhasingEngine.kt`)
Manages the lifecycle and state of the phase shift.
- **State Tracking**: Monitors the `phaseLevel` and `isPhased` status.
- **Synchronized Haptics**: Triggers specific haptic pulses at transition thresholds (0.1 and 0.9) and provides a "Phase Pulse" sequence using `VibrationEffect.Composition` (on API 31+).

### 2. Phasing Layer (`GhostPhasingLayer.kt`)
The visual bridge implemented as a Compose `Box`.
- **Background Rendering**: Renders the `NEURAL_VOID` AGSL shader behind the content when phasing is active.
- **Transition Effect**: Applies the `PHASE_TRANSITION` AGSL shader to the main content using `RenderEffect.createRuntimeShaderEffect`. This introduces chromatic aberration and jitter that scales with the phase level.
- **Optimization**: Utilizes `remember` blocks for `RuntimeShader` and `ShaderBrush` instances to maintain 60fps during transitions.

### 3. Phasing Shaders (`GhostPhasingShader.kt`)
High-performance AGSL shaders driving the visuals.
- **`PHASE_TRANSITION`**: Implements a distance-based chromatic aberration (R/B shift) and a sin-wave scanline glitch.
- **`NEURAL_VOID`**: A complex procedural shader featuring:
    - **Neural Seeds**: Floating, glowing orbs generated via `hash33` noise.
    - **Data Streams**: Vertical particle trails representing real-time information flow.

## ⚡ BOLT Performance Notes
- **Conditional Rendering**: The `GhostPhasingLayer` skips shader application if the phase level is 0.0, avoiding unnecessary GPU overhead.
- **Hardware Requirement**: Requires **API 33+** for `RuntimeShader` support. The layer gracefully falls back to standard rendering on older devices.
- **Resource Management**: Uses `graphicsLayer` for shader effects to leverage hardware acceleration and minimize recomposition triggers.

---
*Documentation love letter from Scribe 📜*
