# 👻 Ghost Frost: Neural Cold Zones

Ghost Frost is an experimental visualization that identifies and maps "Cold Zones" within the classroom — areas where behavioral entropy and academic struggle are concentrated.

## 🧊 The "Cold Zone" Metaphor

In the Ghost Lab ecosystem, classroom energy is often modeled as heat or light. **Ghost Frost** represents the inverse: a loss of thermal data energy. When students struggle academically or exhibit disruptive behavioral patterns, they create localized "Cold Zones." These zones are visualized as procedural frost crystallization that grows across the seating chart, signaling areas that may require pedagogical "re-warming" or intervention.

## ⚙️ Calculation Logic (`GhostFrostEngine.kt`)

The engine synthesizes multiple data streams to calculate the `intensity` (0.0 to 1.0) of frost for each student:

1.  **Factor A: Concerning Status (40% Weight)**: Leverages the `GhostInsightEngine` to identify students already flagged as "CONCERNING" due to high academic struggle or significant negative behavior history.
2.  **Factor B: Negative Event Proximity (25% Weight)**: Uses a spatial decay model ($1 - dist/radius$) to calculate the influence of nearby negative behavioral events.
3.  **Factor C: Cold Zone Clustering (15% Weight)**: Amplifies frost intensity when "Concerning" students are seated near each other, modeling the social contagion of behavioral entropy.

## 🎨 Visualization (`GhostFrostLayer.kt` & `GhostFrostShader.kt`)

The visualization is rendered using a high-performance **AGSL Shader** (API 33+).

-   **Procedural Crystallization**: Uses a **Voronoi-based noise** function to simulate the growth of organic ice crystal structures.
-   **Organic Growth (fBm)**: Implements Fractional Brownian Motion to create non-linear, "branching" frost edges that feel biological rather than geometric.
-   **Dynamic Pulse**: The frost subtly pulses over time, suggesting that these zones are active and evolving.
-   **Transformation-Aware**: The shader correctly maps logical 4000x4000 coordinates to the screen, maintaining its position during pan and zoom.

## ⚡ BOLT Performance Optimizations

To maintain a fluid 60fps experience even with complex procedural shaders:

-   **Identity-Based Grouping**: The engine pre-groups logs by student ID using `HashMap` to avoid expensive $O(N \times L)$ scans during every calculation cycle.
-   **Background Synthesis**: Calculations are hoisted into a `derivedStateOf` block, ensuring they only re-run when underlying data actually changes.
-   **Shader Pooling**: The layer reuses `RuntimeShader` instances, avoiding the overhead of object allocation and JNI uniform mapping on every frame.
-   **Manual Indexing**: All high-frequency loops use manual index-based iteration to eliminate `Iterator` object churn and GC pressure.

## 🛠️ Requirements

-   **Minimum API**: 33 (Android 13) for `RuntimeShader` support.
-   **Configuration**: Must have `GhostConfig.GHOST_MODE_ENABLED = true`.

---
*Documentation love letter from Scribe 📜*
