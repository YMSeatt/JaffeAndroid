# 👻 Ghost Neural Origami: Foldable UI

A high-fidelity spatial transition for the Seating Chart that allows the UI to "fold" like a piece of paper, revealing the hidden "Neural Backstage."

## 🧩 Components

### 1. Folding Engine (`GhostOrigamiEngine.kt`)
Manages the normalized folding state (0.0 to 1.0) and reactive animation parameters.

### 2. Neural Shaders (`GhostOrigamiShader.kt`)
- **Paper Crease**: An AGSL shadow shader that simulates the physical crease of a folded page.
- **Backside Material**: A procedural data-grid texture that represents the "Backstage" of the classroom.

### 3. Origami Layer (`GhostOrigamiLayer.kt`)
A Jetpack Compose wrapper that applies 3D `graphicsLayer` transformations (Y-axis rotation and perspective) driven by the engine.

## ⚡ Performance (BOLT)
- **Hardware-Accelerated**: Leverages the GPU for all 3D rotations and AGSL effects via `graphicsLayer`.
- **Zero-Allocation Rendering**:
  - `RuntimeShader` and `ShaderBrush` instances are hoisted into `remember` blocks.
  - Uniform updates are performed within the `Canvas` draw block to avoid unnecessary recompositions.
- **State Isolation**: Folding animations are reactive (via `animateFloatAsState`) and isolated from the main seating chart student data to ensure 60fps fluidity during transitions.

## 🧪 Verification
- **Logic**: Verified via `GhostOrigamiEngineTest.kt` (Unit Tests) and `verify_origami_logic.py` (Python Simulation).
  - Toggling: 0.0 -> 1.0, 1.0 -> 0.0.
  - Thresholding: Partial folds correctly resolve during toggle.
  - Clamping: Inputs outside [0, 1] are safely coerced.
- **UI**: 3D transformations verified via manual code audit of `TransformOrigin` and `rotationY` parameters.
