# 🧠 Ghost Cortex: Somatic Neural Exploration

The **Ghost Cortex** experiment bridges abstract classroom data with physical sensation. It models "Neural Tension" and "Somatic Resonance," allowing teachers to "feel" the classroom's data landscape through high-fidelity haptic feedback and organic visual metaphors.

## 🏛️ The "Neural Intent" Model

The engine calculates a student's current state as a physical force called **Neural Tension**. This metric combines academic performance and behavioral stability into a single vector of intensity.

### ⚡ Neural Tension Formula
The tension $T$ is calculated as the RMS (Root Mean Square) of Academic Entropy and Behavioral Turbulence:

$$T = \sqrt{\frac{E_a^2 + T_b^2}{2}}$$

- **Academic Entropy ($E_a$):** Derived from the variance and average of quiz scores. High entropy indicates inconsistent performance or low scores.
- **Behavioral Turbulence ($T_b$):** The ratio of negative behavior logs to total logs.

---

## 📳 Somatic Haptics: Tactile Communication

The `GhostCortexEngine` maps the calculated tension to a series of sophisticated [VibrationEffect.Composition] primitives (API 31+), providing nuanced tactile feedback.

| Tension Threshold | Feedback Level | Haptic Primitives | Intent |
| :--- | :--- | :--- | :--- |
| **< 0.2** | Level 1: Subtle | `PRIMITIVE_LOW_TICK` (30%) | Classroom is in a state of "Zen." |
| **< 0.5** | Level 2: Noticeable | `PRIMITIVE_CLICK` x2 | Normal activity; accumulating energy. |
| **< 0.8** | Level 3: Urgent | `PRIMITIVE_TICK` (80%), `PRIMITIVE_SPIN` | High friction; localized stress clusters. |
| **>= 0.8** | Level 4: Critical | `PRIMITIVE_THUD`, `PRIMITIVE_QUICK_FALL` | Immediate intervention recommended. |

---

## 🎨 Visual Mapping: Somatic Field Shader

The `GhostCortexLayer` renders the "Somatic Field" using **AGSL (Android Graphics Shading Language)**.

### 🌊 Organic Ripples
Multi-layered **Fractal Brownian Motion (fbm)** noise creates a fluid, organic background. When the teacher interacts with the seating chart, radial ripples are generated at the touch point.
- **Pulse Speed:** Correlates with the global agitation level.
- **Decay:** Ripples dissipate based on an exponential decay function relative to the distance from the source.

### 🌈 Tension Color Mapping
The field's color palette shifts dynamically based on the local Neural Tension:
- **Cyan (0.0):** Represents a calm, stable environment.
- **Magenta/Red (1.0):** Represents high tension and behavioral volatility.

---

## ⚡ 2027 R&D Directive: Predictive Back

`GhostCortexActivity` serves as a Proof of Concept for **Predictive Back** integration. It utilizes the Compose `BackHandler` to provide fluid, high-fidelity somatic transitions when returning from the "Neural History" view to the primary seating chart.

---
*Documentation love letter from Scribe 📜*
