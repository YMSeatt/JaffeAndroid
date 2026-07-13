# 👻 Ghost Vector: Social Gravity Analysis

## 🛰️ The Metaphor
The **Ghost Vector** experiment models classroom social dynamics as a physical system of "Social Gravity." In this model, students are treated as celestial bodies that exert invisible forces on one another. These forces are derived from their spatial proximity on the seating chart and their historical behavioral interactions.

## 🧮 The Social Physics Model
The engine calculates a **Net Force Vector** for every student by summing the individual social forces acting upon them.

### 1. Force Types
Connections between students are categorized into three types, each with a specific force multiplier:
- **Collaboration (Attraction)**: Students with frequent positive interactions "pull" towards each other (Multiplier: `60.0`).
- **Friction (Repulsion)**: Students with historical tension or negative logs "push" away from each other (Multiplier: `100.0`).
- **Neutral (Mild Attraction)**: Standard proximity-based social gravity (Multiplier: `15.0`).

### 2. The Inverse-Proximity Law
The strength of a force is inversely proportional to the distance between students, up to a maximum threshold of **800 logical units**.
${Strength = (1.0 - (Distance / 1200.0))}$

### 3. Net Force Calculation
The final vector $(dx, dy)$ for a student $S$ is the sum of all normalized direction vectors multiplied by their respective force magnitudes:
${\vec{F}_{net} = \sum \hat{d}_{ij} \cdot Force_{ij}}$

## 📊 Synthetic Units & Classification
Forces are measured in **mG (milli-Gravities)**. Based on the magnitude of the net force, students are classified into social states:
- **High Turbulence (> 85 mG)**: Significant social conflict or intense interaction density. (Visualized in **Magenta**).
- **Active Synergy (> 40 mG)**: Healthy social momentum and collaboration. (Visualized in **Cyan**).
- **Nominal (5 - 40 mG)**: Balanced social state. (Visualized in **Cyan**).
- **Isolated (< 5 mG)**: Low social interaction or physical distance from the group.

## 🎨 Visualization (`GhostVectorLayer`)
The `GhostVectorLayer` uses high-performance **AGSL Shaders** to render these forces:
- **Directional Needles**: Pulsing needles point in the direction of the net force.
- **Magnitude Scaling**: The length and brightness of the needle scale with the force intensity.
- **Neural Flow**: An animated trail effect simulates the "Social Current" flowing through the student.
- **Color Shifts**: The needle shifts from Cyan to Magenta when the student enters a "High Turbulence" state.

## 🌉 The Python Bridge
This engine is logically matched with `Python/ghost_vector_analysis.py`. This parity allows for consistent data analysis across desktop and mobile platforms, ensuring that a "High Turbulence" alert on the Android app corresponds to the same mathematical threshold in the Python-based research suite.

---
*Documentation love letter from Scribe 📜*
