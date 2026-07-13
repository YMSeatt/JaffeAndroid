# 👻 Ghost Aurora (Classroom Climate)

## 🌌 The Neural Metaphor
The **Ghost Aurora** is a procedural atmospheric visualization that translates the collective behavioral and academic "momentum" of the classroom into a flowing, ethereal light show. It serves as a non-intrusive "Neural Weather" system, allowing teachers to sense the classroom's emotional and intellectual climate at a glance.

- **Cyan (Positive)**: Dominates when participation and helpfulness are high.
- **Red (Negative)**: Emerges during periods of disruption or behavioral turbulence.
- **Purple (Academic)**: Pulsates during high-intensity academic activity (quizzes, homework).
- **Deep Blue (Neutral)**: The baseline state when the classroom is calm and steady.

## 🛠 Technical Implementation

### Neural Synthesis Pipeline (`GhostAuroraEngine`)
The engine performs a high-performance, single-pass synthesis of all recent behavior logs, quiz scores, and homework submissions.
- **BOLT ⚡ Optimization**: Since the database provides logs in `DESC` (newest first) order, the engine utilizes an early-exit strategy. It breaks the iteration loop as soon as it encounters a log outside the 10-minute "Climate Window," ensuring that performance remains constant regardless of total historical log volume.
- **Intensity Mapping**: The aurora's brightness and complexity scale linearly with the frequency of classroom events.
- **Color Blending**: A weighted ratio of positive, negative, and academic events is used to calculate the primary and secondary colors of the plasma field.

### AGSL Plasma Shader (`GhostAuroraShader`)
The visual effect is powered by a custom **Android Graphics Shading Language (AGSL)** script.
- **Domain Warping**: Uses nested Fractal Brownian Motion (FBM) to create fluid, organic motion that simulates ionized gas flowing through a magnetic field.
- **Vertical Gradient**: The aurora is naturally stronger at the top of the canvas, providing a sense of depth and atmospheric scale.
- **Digital Shimmer**: A subtle high-frequency noise layer adds a "neural data" texture to the light.

### Compose Integration (`GhostAuroraLayer`)
- **API 33+**: Leverages `RuntimeShader` and `ShaderBrush` for hardware-accelerated rendering.
- **Resource Hoisting**: Shaders and Brushes are hoisted and `remember`-ed to eliminate per-frame allocations during the 60fps animation loop.
- **Temporal Alignment**: Uses an `InfiniteTransition` to drive the shader's `iTime` uniform, ensuring smooth, continuous motion.
