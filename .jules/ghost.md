# 👻 Ghost's Journal: R&D & "What if?" Scenarios

## 🌀 Experiment: Ghost Portal (Drag & Drop Evolution)
**Date:** 2026-02-13
**Status:** PROTOTYPE COMPLETE

### 🌟 The Vision
In 2027, the boundaries between applications will blur. "Ghost Portal" is a PoC for a future where classroom data is fluid. Teachers can "teleport" students between different classroom environments or external AI analyzers with a single gesture.

### 🛠️ The Tech
- **Android 15 Drag & Drop:** Integrated `Modifier.dragAndDropSource` and `Modifier.dragAndDropTarget` into the Compose UI.
- **AGSL Shaders:** Created `GhostPortalShader` to provide a futuristic "Wormhole" effect.

---

## 🕸️ Experiment: Ghost Lattice (Social Graph Visualization)
**Date:** 2026-05-20
**Status:** PROTOTYPE COMPLETE

### 🌟 The Vision
Teachers often "feel" the social dynamics of a room but can't see them. "Ghost Lattice" makes the invisible visible. It visualizes student relationships as a glowing neural network, helping teachers identify social clusters, potential friction points, and isolated students at a glance.

### 🛠️ The Tech
- **AGSL Neural Shaders**: Implemented a specialized `NEURAL_LATTICE` shader that draws glowing, pulsing connections between student coordinates. It uses bounding-box optimization for efficient rendering of multiple lines.
- **Social Inference Engine**: A local engine that analyzes physical proximity and behavioral history to determine "Collaboration" vs "Friction" links.
- **Python Bridge**: `ghost_lattice.py` demonstrates how complex social graph metrics (cohesion, turbulence) can be computed from raw JSON exports.

### 🔦 The Discovery
- **Shader Batching**: Drawing multiple lines with individual `RuntimeShader` instances is performant enough for a PoC (~30-50 edges), but a single multi-line shader using uniform arrays would be better for larger classes.
- **Dynamic Recalculation**: By observing the `StudentUiItem` position states, the lattice updates in real-time as students are dragged, creating a "stretchy" social web effect.

### 💡 The "What if?"
*What if the lattice could predict 'social contagion'—where a negative behavior from one node travels through the edges to affect others?*

---
*Ghost - Rapid Prototyping for the Classroom of 2027*
