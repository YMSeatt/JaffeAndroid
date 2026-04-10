# 🏗️ Ghost Architect: Strategic Layout Optimization

Ghost Architect is an experimental generative engine within the Ghost Lab that focuses on **pedagogical layout optimization**. Unlike the physics-based distribution of the [Ghost Cognitive Engine](../README.md#1-cognitive-proximity-engine-ghostcognitiveenginekt), Ghost Architect uses high-level heuristics to propose "Neural Trajectories" for student seating based on specific classroom goals.

## 🎯 Strategic Goals

The engine supports three primary optimization strategies:

1.  **COLLABORATION**: Minimizes the distance between "social allies" (students with Collaboration links in the [Ghost Lattice](../lattice/)). It aims to cluster students who work well together.
2.  **FOCUS**: Maximizes the distance between "friction points" (students with Friction links). It proposes moving potentially disruptive pairs apart to improve classroom focus.
3.  **STABILITY**: Aims to balance the classroom by diffusing "high-energy" nodes (students with frequent negative behavior logs) towards the center or more stable zones.

## 🛰️ Neural Trajectories & Visualization

When active, Ghost Architect visualizes proposed moves as **Neural Trajectories**:
- **Blueprint Grid**: An AGSL-powered cyan grid background that sets the "strategic planning" atmosphere.
- **Trajectory Beams**: Glowing beams that point from a student's current position to their proposed optimal position.
- **Priority Highlighting**: Beams for high-priority moves (e.g., critical focus adjustments) shift from Cyan to **Magenta**.

## ⚡ Bolt Optimizations

Ghost Architect is built for real-time performance on Android:
- **O(N + E) Analysis**: The `proposeLayout` function utilizes pre-grouped edge maps to avoid $O(N \times E)$ nested loops during analysis.
- **Shader Pooling**: `GhostArchitectLayer` maintains a pool of `RuntimeShader` instances to render multiple trajectory beams without per-frame object allocations.
- **Squared Distance Heuristics**: All proximity checks use squared distance ($d^2$) to eliminate expensive `sqrt` calls.
- **Haptic Feedback**: Uses Android 15 `VibrationEffect.Composition` to provide tactile "Architectural Locking" feedback when new strategies are generated.

## 🔄 Logic Parity

This engine maintains strict logical parity with the **Python R&D Bridge** (`Python/ghost_architect_analysis.py`).
> **Note**: Android implementation uses a 2x coordinate scaling factor (4000x4000 vs 2000x1500) to match the mobile canvas.

---
*Documentation love letter from Scribe 📜*
