# 🧪 Ghost Catalyst: Behavioral Chain Reaction Mapping

The **Ghost Catalyst** experiment treats the classroom as a dynamic chemical system where student actions act as catalysts for subsequent behavioral events. It identifies and visualizes "Social Chain Reactions" by detecting sequential logs in close spatio-temporal proximity.

## 🏛️ The "Kinetics" Model

The engine implements a macroscopic kinetics model to quantify classroom social volatility.

### ⚛️ Reaction Identification
- **Spatio-temporal Window**: A reaction is detected when a student (the "Reactant") performs an action within **300 seconds** (5 minutes) and **800 logical units** of another student (the "Catalyst") who recently performed an action.
- **Intensity**: Calculated based on temporal proximity (closer events have higher intensity).
- **Pruning**: Uses squared distance checks for $O(N)$ spatial efficiency and chronological pruning for $O(T)$ temporal efficiency.

### 📊 Macroscopic Metrics
- **Reaction Rate**: The frequency of unique student-to-student reactions, normalized to a 5-minute window.
- **Activation Energy**: A heuristic representing the behavioral threshold required to initiate a chain reaction. It is inversely proportional to the global classroom engagement level.
- **Equilibrium Constant ($K_{eq}$)**: The ratio of the reaction rate to the activation energy, representing the overall stability of the social environment.

---

## 🎨 Visual Mapping: AGSL Ionic Field

The `GhostCatalystLayer` utilizes high-performance shaders to render the classroom's "chemical" state.

### 🫧 Reaction Field (Effervescence)
A procedural background layer that visualizes the global reaction rate.
- **Bubbles**: Procedural "Reaction Bubbles" rise through the UI space, with density and speed driven by the `iRate` uniform.
- **Brownian Distortion**: A "Heat Haze" effect distorts the seating chart, simulating the thermal energy of high-activity zones.

### ⚡ Ionic Bonds
Visualizes the connection between a Catalyst and a Reactant.
- **Glow Lines**: Pulsating, glowing AGSL lines connect the participants of a reaction.
- **Sparkle Noise**: High-frequency procedural noise ("sparks") travels along the bonds, with intensity driven by the reaction's temporal proximity.

---

## ⚡ BOLT Optimization

To maintain 60fps performance during complex seating chart interactions:
1.  **Shader/Brush Pooling**: The UI layer utilizes a persistent pool of `RuntimeShader` and `ShaderBrush` instances, eliminating per-frame object allocations in the `Canvas` draw loop.
2.  **Mapping Resolution**: Pre-maps students to their IDs to transform $O(R \times S)$ operations into $O(R)$ lookups during rendering.
3.  **Chronological Pruning**: The engine processes logs in a single pass, using the DESC-sorted order from the DAO to early-exit once the time window is exceeded.
4.  **Squared Distance**: All spatial calculations use $d^2$ to avoid expensive `sqrt()` operations.

---
*Documentation love letter from Scribe 📜*
