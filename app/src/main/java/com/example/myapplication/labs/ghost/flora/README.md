# 🌸 Ghost Flora: Neural Botanical Visualization

The **Ghost Flora** experiment implements a digital classroom ecosystem where students are represented by data-driven, procedural flowers. It explores the intersection of academic performance, behavioral patterns, and generative art.

## 🌿 The Metaphor: Neural Botanical Growth
Classroom dynamics are modeled as a living ecosystem. Student data drives the growth and vitality of their individual "Neural Flower":

- **Growth (Scale/Petal Length)**: Driven by **Academic Performance**. It calculates the average of quiz scores and homework completion rates. High performers exhibit longer, more flourishing petals.
- **Vitality (Color/Vibrancy)**: Driven by **Behavioral Balance**.
    - **Cyan (Positive)**: Represents a high ratio of positive behavioral logs.
    - **Magenta (Negative)**: Represents a higher concentration of negative or disruptive logs.
- **Complexity (Petal Density/Noise)**: Driven by **Activity Frequency**. The total number of logs (behavior, quiz, homework) determines the complexity of the petal divisions and procedural noise distortion.

## 🎨 AGSL Shader Implementation (`GhostFloraShader.kt`)
The visual representation is powered by a high-performance **AGSL Shader** that utilizes:
- **Polar Coordinate Warping**: Translates Cartesian coordinates into polar space to create organic petal shapes using `cos()`-based distance functions.
- **Multi-layered Noise**: Uses pseudo-random hash functions and time-based rotation to simulate organic swaying and variation.
- **Procedural Bloom**: Implements a center-glow effect that pulses with the student's "data energy."

## ⚡ BOLT Performance Optimizations
To maintain 60fps on mid-range Android hardware, the layer implements several BOLT patterns:
- **Log Pre-grouping**: Logs are grouped by `studentId` once per update cycle using `groupBy`, preventing $O(N \times L)$ lookups within the high-frequency rendering path.
- **Shader & Brush Pooling**: `RuntimeShader` and `ShaderBrush` instances are cached in a `remember` block. This prevents thousands of per-frame allocations and ensures that uniform updates don't "bleed" between student icons.
- **Zero-Allocation Draw Loop**: All heavy calculations and object creations are hoisted out of the `Canvas` draw scope.

## 🐍 Logic Parity
This implementation maintains strict logical parity with the **[Python Analysis Suite](../../../../../../../../../Python/ghost_flora_analysis.py)**. The Python engine provides macroscopic "Classroom Climate" reports (e.g., Tropical vs. Tundra) using the same Growth and Vitality metrics.

---
*Documentation love letter from Scribe 📜*
