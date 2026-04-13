# Ghost Emergence 👻🌱

Ghost Emergence is an experimental simulation that models classroom behavior as a **Cellular Automata** system. It visualizes the "social contagion" effect, where individual behavioral events diffuse through the classroom environment, creating emergent patterns of vitality or decay.

## 🧬 The Metaphor: Behavioral Emergence

In this model, the classroom is treated as a living field of energy.
- **Positive Behavior** acts as an "Impulse of Vitality," injecting energy into a specific location.
- **Negative Behavior** acts as a "Void," removing energy and creating a localized dip in the field.
- **Social Contagion** is modeled through **Diffusion**, where energy flows from high-vitality areas to neighboring cells, simulating how mood and behavior can spread between students.

## ⚙️ Simulation Rules

The engine operates on a **10x10 Vitality Grid** (optimized for GPU uniform limits) and applies the following rules during each update cycle:

1.  **Impulse**: Behavioral events are mapped from the 4000x4000 logical canvas to the 10x10 grid.
    -   **Positive Events**: +0.15f vitality.
    -   **Negative Events**: -0.20f vitality (Negative events have a slightly stronger "gravitational" pull on the field).
2.  **Diffusion**: 10% of a cell's vitality difference relative to its neighbors flows into or out of the cell.
    -   `diffused = current + (neighborAvg - current) * 0.1f`
3.  **Decay**: To prevent energy accumulation and simulate the passing of time, the field naturally dissipates at a rate of 5% per cycle.
    -   `decayed = diffused * 0.95f`

## 🎨 AGSL Visualization

The emergent field is rendered as a background layer using an **AGSL Shader** (`GhostEmergenceShader.kt`).
-   **Vitality Mapping**: The 10x10 grid is passed as a `float[100]` uniform.
-   **Color Spectrum**:
    -   **Cyan (Positive)**: Areas of high vitality and growth.
    -   **Red (Negative)**: Areas of behavioral voids or "decay."
-   **Organic Texture**: Uses procedural noise to simulate a living, breathing field that responds to the underlying data in real-time.

## 📊 Emergence Analysis

The `GhostEmergenceEngine` analyzes the grid to categorize the classroom's state:
-   **Growth Dominant**: When the count of high-vitality clusters (>0.1f) exceeds the count of voids.
-   **Decay Warning**: When negative clusters (<-0.1f) are more prevalent, suggesting a downward trend in classroom climate.

## 🔄 Logic Parity

This implementation maintains strict logical parity with the research prototype:
-   **Android**: `app/src/main/java/com/example/myapplication/labs/ghost/emergence/`
-   **Python**: `Python/ghost_emergence_analysis.py`

---
*Documentation love letter from Scribe 📜*
