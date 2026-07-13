# 🌌 Ghost Stellar: Neural Constellation Mapping

The **Ghost Stellar** experiment transforms the classroom into a digital cosmos. It models students as celestial bodies whose brightness and connectivity are driven by academic performance and social groupings.

## 🏛️ The "Neural Metaphor"

The experiment uses an astronomical lens to visualize classroom data:

### ⭐ Stellar Magnitude (Brightness)
- **Data Source**: Academic Performance (Quiz Scores).
- **Metaphor**: High-achieving students shine as high-magnitude stars (brighter, larger icons). Students with lower scores or missing data appear as dimmer, distant stars.
- **Formula**: Magnitude $M$ is the normalized average of all historical quiz scores.

### 🌈 Spectral Type (Color)
- **Data Source**: Behavioral Balance.
- **Metaphor**: Student "temperature" is mapped to spectral colors.
    - **Blue/Cyan**: High stability and positive engagement.
    - **Amber/Gold**: Neutral or developing stability.

### 🕸️ Constellations (Group Connectivity)
- **Data Source**: Seating Chart Groups.
- **Metaphor**: Students in the same group are connected by glowing "Stellar Threads," forming unique constellations in the classroom sky.
- **Visualization**: AGSL-powered glowing segments that pulse with "neural energy."

---

## 🎨 Visual Mapping: AGSL Cosmic Rendering

The `GhostStellarLayer` utilizes **AGSL (Android Graphics Shading Language)** for high-fidelity cosmic effects.

### 🌌 Procedural Star Field
A dynamic background shader (`STAR_FIELD`) that generates a deep-space environment with twinkling stars. The twinkling frequency is unique to each coordinate, ensuring a non-repetitive atmosphere.

### 🧵 Neural Threads
A specialized line shader (`STELLAR_THREAD`) that renders glowing connections between group members. It features an exponential glow decay and a temporal pulse to simulate data flow.

---

## ⚡ BOLT Optimization

To maintain 60fps interaction on API 33+ devices:
1. **O(Recent) Synthesis**: The `GhostStellarEngine` processes student data and quiz logs in a single pass ($O(N)$) to calculate star parameters.
2. **Identity-Based Synthesis**: Synthesis is gated by `remember(students, quizLogsByStudent)`, ensuring heavy calculations only occur when underlying data actually changes.
3. **Shader Pooling**: Reuses `RuntimeShader` and `ShaderBrush` instances to eliminate allocation-churn in the `Canvas` draw loop.
4. **Star Topology**: Constellation links are rendered using a star-pattern (connecting members to a root) to reduce the number of draw calls from $O(G^2)$ to $O(G)$ per group.

---
*Documentation love letter from Scribe 📜*
