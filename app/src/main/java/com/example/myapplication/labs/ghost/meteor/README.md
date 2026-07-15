# ☄️ Ghost Meteor: Neural High-Momentum Projectiles

The **Ghost Meteor** experiment introduces a high-energy visualization layer that models major classroom milestones as neural projectiles and impact events.

## 🌪️ The Metaphor: Neural Impact
In this mode, significant pedagogical events are no longer just table entries; they are physical forces that "strike" the classroom landscape:

- **Academic Breakthroughs (Purple)**: Logging high quiz scores (>= 80%) triggers meteors that strike the student's icon, symbolizing the sudden "impact" of knowledge.
- **Behavioral Surges (Cyan/Magenta)**: Bursts of positive or negative behavior logs emit meteors that ripple through the social field, visualizing the momentum of classroom culture.
- **Impact Shockwaves**: Upon arrival (or expiration), meteors create glowing AGSL shockwaves that pulse across the seating chart, signifying the "Neural Echo" of the event.

## ⚡ BOLT Performance Optimizations
To maintain 60fps during high-intensity classroom sessions, Ghost Meteor implements several architecture-level optimizations:

1. **Object Pooling**: Utilizes pre-allocated `Meteor` and `Impact` object pools to eliminate per-event garbage collection churn.
2. **Zero-Allocation Physics**: The `GhostMeteorEngine` update loop performs calculations using primitive types and `FloatArray` buffers.
3. **AGSL Shader Pipelines**: Renders complex streaks and shockwaves using a single-pass GPU pipeline with hoisted uniforms.
4. **O(N) Complexity**: Meteor updates and collision checks are performed in a single pass over the active projectile list.

## 🛠️ Implementation Details
- **Engine**: `GhostMeteorEngine.kt` manages the lifecycle, physics (velocity/momentum), and impact detection.
- **Layer**: `GhostMeteorLayer.kt` provides the Jetpack Compose rendering bridge, utilizing `RuntimeShader` (API 33+).
- **Shader**: `GhostMeteorShader.kt` contains the AGSL math for tapering streaks and expanding circular shockwaves.

---
*Documentation love letter from Ghost 👻*
