# 🔗 Ghost Link: Neural Pairing

This experiment visualizes the hidden "Neural Links" that form between students based on spatial proximity and behavioral synergy. It models the social fabric of the classroom as a dynamic, interconnected network.

## 🛠️ Components

### 1. Neural Pairing Engine (`GhostLinkEngine.kt`)
The analytical core that identifies connections between student nodes.
- **Proximity Threshold**: Analyzes student pairs within **600 logical units**.
- **Behavioral Synergy**: Calculates resonance based on parity in recent log frequency.
- **Spatial Weighting**: Applies a Gaussian decay function to favor closer students.
- **Performance**: BOLT-optimized $O(N^2)$ analysis with spatial pruning.

### 2. Neural Strand Shader (`GhostLinkShader.kt`)
A high-performance **AGSL Shader** that renders the connections.
- **Organic Movement**: Uses procedural noise and domain warping to simulate living neural fibers.
- **Data Pulsing**: Animates white "data pulses" along the strands to visualize information flow.
- **Reactive Intensity**: Scales thickness and brightness based on calculated link strength.

### 3. Neural Link Layer (`GhostLinkLayer.kt`)
The Jetpack Compose rendering layer.
- **Shader Pooling**: Implements a strict pooling strategy to prevent uniform overwrites during batch rendering.
- **Zero-Allocation**: Designed for 60fps performance by minimizing object churn in the draw loop.
- **Transform-Aware**: Synchronizes perfectly with the seating chart's pan and zoom states.

## 💡 Implementation Secrets

- **The "Link" Distinction**: This package implements **Neural Pairing** (spatial connections). It is distinct from the `GhostLinkEngine.kt` in the parent package, which generates **Neural Dossiers** (textual reports).
- **Synergy Parity**: The synergy calculation favors "sync" over raw count. If two students both have 5 logs, they have 100% synergy; if one has 10 and the other has 0, they have 0% synergy.
- **Padding Buffer**: The `drawRect` for each link includes a **50f padding** buffer to ensure the shader's glow and organic warping aren't clipped by the bounding box.

---
*Documentation love letter from Scribe 📜*
