# 🌀 Ghost Vortex: Rotational Social Momentum

The **Ghost Vortex** experiment identifies and visualizes "Social Whirlpools" in the classroom. It detects clusters of high-activity students whose collective behavior creates localized rotational momentum, which is then rendered as a spatial distortion effect on the seating chart.

## 🏛️ The "Social Whirlpool" Model

The engine implements a physics-inspired model where student density and behavior log frequency act as localized pressure points. When multiple high-activity nodes are in close proximity, their collective "Energy" is summed and normalized to create a rotational field.

### 🌪️ Behavioral Cyclones
- **Vortex Identification**: The system scans for students with significant recent activity (logs within a 10-minute window).
- **Clustering**: Students within **800 logical units** of each other are grouped into a potential vortex.
- **Momentum Calculation**: The "Angular Momentum" of a vortex is derived from the average log intensity of its constituent members.
- **Social Polarity**: Vortices are categorized as **Synergy** (positive logs dominant) or **Distraction** (negative logs dominant).

---

## 🎨 Visual Mapping: AGSL Swirl Distortion

The `GhostVortexLayer` transforms the abstract vortex data into a futuristic visual experience using **AGSL (Android Graphics Shading Language)**.

### 🌀 Spatial Swirl
A radial rotation formula is applied to the UI space around the vortex center. The strength of the swirl decreases with distance, creating a localized "warping" effect that visually represents the social momentum.

### ✨ Spiral Glow & Accretion
Procedural noise is used to render glowing "accretion lines" that spiral into the center of the vortex.
- **Cyan Glow**: Represents a positive synergy vortex.
- **Magenta Glow**: Represents a negative distraction vortex.

---

## ⚡ BOLT Optimization

To maintain 60fps performance during complex seating chart interactions:
1.  **Complexity Reduction**: The engine pre-filters students into an `activeNodes` list, transforming an $O(N^2)$ operation into $O(A^2)$, where $A$ is the number of active students.
2.  **Manual Loops**: Replaces expensive functional operators with optimized manual loops and early-exit conditions.
3.  **Shader Pooling**: The UI layer utilizes a `brushPool` to cache and reuse `ShaderBrush` instances, eliminating per-frame object allocations.
4.  **Spatial Pruning**: Uses squared distance calculations to avoid expensive `sqrt()` operations during the clustering phase.

---
*Documentation love letter from Scribe 📜*
