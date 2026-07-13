# 💥 Ghost Supernova: Classroom Criticality & Data Reset

The **Ghost Supernova** is a high-intensity visual metaphor for classroom behavioral stability. It models the cumulative "social friction" of the classroom as a physical pressure system that can reach a tipping point, triggering a multi-stage visual event that "resets" the classroom energy.

## 🏛️ The "Classroom Criticality" Metaphor

The Supernova is designed to help teachers visualize the "Core Pressure" of their classroom environment. It treats behavioral incidents as physical catalysts that increase the system's temperature and pressure.

- **Cumulative Friction**: Negative incidents (disruptions, conflicts) add significant pressure to the system, while positive participation contributes at a much lower rate.
- **The Event Horizon**: Once the `Core Pressure` reaches 1.0 (Critical Mass), a Supernova event is triggered. This serves as a psychological "hard reset" for the classroom atmosphere.

---

## 🔄 The Supernova Lifecycle

The engine orchestrates the visualization through four distinct stages, each driven by procedural **AGSL (Android Graphics Shading Language)** shaders.

### 1. 💤 IDLE (Pressure Accumulation)
The baseline state. The `GhostSupernovaLayer` renders a subtle heat distortion effect in the background. As pressure builds, the intensity of the distortion and the frequency of "neural flickering" increases.

### 2. 🌀 CONTRACTION (Implosion)
Upon reaching critical mass, the system enters a 1.5-second contraction phase. The UI appears to "shrink" or implode toward a central singularity, represented by a bright, blue-shifted radial pulse. This represents the classroom "holding its breath" before a reaction.

### 3. 💥 EXPLOSION (The Reset)
A rapid (1.0s), high-energy magenta shockwave expands from the center, accompanied by a procedural "screen shake." This phase represents the peak behavioral volatility and the subsequent release of tension.

### 4. 🌌 NEBULA (Cooling)
A slow, 5-second cooling phase where gaseous clouds drift across the seating chart. This signals to the teacher and students that the "storm has passed" and the classroom has returned to a stable, neutral state.

---

## 🧪 Mathematical Model & Heuristics

The engine utilizes a deterministic scoring model to calculate pressure, ensuring logical parity with the **Python Analysis Suite** (`Python/ghost_supernova_analysis.py`).

### 🌡️ Core Pressure Calculation
Pressure is calculated within a **15-minute sliding window**.
- **Negative Log Weight**: 0.1f (represents high friction).
- **Positive Log Weight**: 0.04f (represents baseline activity).
- **Decay**: Logs older than 15 minutes are discarded, allowing the classroom pressure to naturally "cool down" during quiet periods.

### 📊 Criticality Index
The engine provides a `calculateCriticality` heuristic to evaluate the global risk state:
`Criticality = (Total Logs / (Student Count * 5.0)) * (1.0 + (Negative Ratio * 2.0))`

- **5.0 Divisor**: Represents the "Satiation Point"—the average number of logs per student before the classroom is considered "saturated" with data.
- **Stress Multiplier**: Disproportionately weights negative logs to reflect their higher impact on social stability.

---

## ⚡ Performance Optimization (BOLT)

To ensure the Supernova can execute at 60fps even during high-intensity classroom periods:
1.  **State-Driven Shaders**: Shader uniforms are driven by Compose `Animatable` states, ensuring smooth transitions without triggering full-box recompositions.
2.  **O(Recent) Analysis**: The pressure engine only scans behavior logs within the sliding window, achieving sub-millisecond analysis times.
3.  **Hardware-Level Capture**: During the explosion phase, the shader leverages `graphicsLayer` transformations for the screen-shake effect, bypassing expensive UI-thread position updates.

---
*Documentation love letter from Scribe 📜*
