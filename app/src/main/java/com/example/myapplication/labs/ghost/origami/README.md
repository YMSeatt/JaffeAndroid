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
- **Hardware-Accelerated**: Leverages the GPU for all 3D rotations and AGSL effects.
- **Zero-Allocation**: Shaders and brushes are hoisted to prevent per-frame object churn.
- **State Isolation**: Folding animations are reactive and isolated from the main seating chart state to ensure 60fps fluidity.
