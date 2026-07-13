# 👁️ Ghost Glance: Neural State Preview

The **Ghost Glance** experiment provides a rapid, high-fidelity overview of a student's current classroom standing. It synthesizes longitudinal behavioral and academic data into a concise "Neural State" visualized through a glassmorphic HUD overlay.

## 🏛️ Neural Metaphor & Signatures

Ghost Glance models student activity as a fluid system of momentum and stability.

### 1. Neural Momentum (`MOM`)
Represents the absolute intensity of classroom engagement over a sliding 7-day window. It aggregates behavioral logs, quizzes, and homework checks. Higher momentum results in more intense wave interference within the background shader.

### 2. Neural Stability (`STB`)
The ratio of positive to total behavioral logs. High stability produces a calm, coherent visual state (Cyan), while low stability (Turbulence) introduces Magenta shifts and increased visual "friction" in the pulsating wave pattern.

### 3. Neural Signatures (`SIG`)
The engine categorizes students into four distinct behavioral archetypes:
- **`STABLE`**: Balanced positive and academic activity. The "Gold Standard" for classroom presence.
- **`PEAK`**: Exceptional engagement and high performance. Triggers a high-momentum visual state.
- **`TURBULENT`**: Identified by frequent negative behavioral triggers. Signals a need for pedagogical intervention.
- **`QUIET`**: Minimal recent log activity. Represented by a low-intensity, dormant visual state.

## ⚡ BOLT Performance Optimizations

Glance is designed to be highly responsive, often triggered by hovering or selecting students in high-density layouts. It adheres to strict **BOLT** design principles:

- **Single-Pass Synthesis**: The `GhostGlanceEngine` replaces standard functional chains (`filter`, `count`) with optimized manual index-based loops. This allows the engine to calculate momentum, stability, and engagement for a student's entire 7-day history in a single $O(L)$ pass.
- **Zero-Allocation Analysis**: The synthesis logic avoids intermediate list or object allocations, minimizing Garbage Collection (GC) pressure during rapid UI interactions.
- **Shader Uniform Batching**: The background `NEURAL_WAVE` shader captures pre-calculated metrics directly as float uniforms, offloading the visual representation of "momentum" to the GPU.

## 🎨 Visual Representation

The `GhostGlanceSurface` utilizes a **Glassmorphic HUD** aesthetic:
- **AGSL Interference**: Background waves are generated using a custom AGSL shader that simulates constructive and destructive interference based on the student's momentum.
- **Adaptive Color Blending**: The shader performs real-time color interpolation between `GhostCyan` (Stability) and `GhostMagenta` (Turbulence) based on the synthesized stability index.

---
*Documentation love letter from Scribe 📜*
