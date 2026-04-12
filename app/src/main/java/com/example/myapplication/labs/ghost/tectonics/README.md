# 🌋 Ghost Tectonics: Social Stability Modeling

## Overview
The **Ghost Tectonics** engine is a pedagogical visualization tool that models classroom social stability as a geological system. By treating students as tectonic plates and behavioral events as seismic triggers, it identifies localized "Social Stress" and predicts potential "Seismic Events" (behavioral outbursts).

## Geological Metaphor

### 💎 The Social Lithosphere
The classroom background is rendered as a dynamic geological surface. Procedural "Cracks" and "Magma Glow" visualize the underlying social pressure.

### 🌡️ Social Stress
A cumulative metric representing the "pressure" at any given point in the classroom.
- **Base Stress**: Directly proportional to a student's negative behavioral history.
- **Proximity Stress**: Stress "radiates" from students, influencing their neighbors within a **600 logical unit** radius.

### 🌩️ Fault Lines
High-stress zones (>40% stress) in close proximity to one another form "Fault Lines". These are visual indicators of areas where social friction is critical.

### 🌋 Seismic Events
Predicted behavioral outbursts. The engine flags zones where the accumulated stress exceeds stable thresholds.

## Seismic Risk Levels

| Level | Description |
| :--- | :--- |
| **STABLE** | Nominal social pressure. No significant fault lines detected. |
| **ACCUMULATING** | Stress is building in the social lithosphere. Monitor cluster proximity. |
| **VOLATILE** | High social friction detected. Significant tremors expected in high-stress clusters. |
| **CRITICAL** | URGENT: Major seismic event imminent. Social fault lines are at breaking point. |

## Implementation Details
- **Engine**: `GhostTectonicEngine.kt` uses a $O(N^2)$ stress-field calculation optimized with squared distance comparisons.
- **Visualization**: `GhostTectonicLayer.kt` utilizes an AGSL shader (`SOCIAL_TECTONICS`) to render procedural noise and magma effects.
- **Parity**: Logically aligned with `Python/ghost_tectonics_analysis.py` for cross-platform analytical consistency.

---
*Documentation love letter from Scribe 📜*
