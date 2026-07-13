# 👻 Ghost Prism: Neural Vibe Themes

Ghost Prism implements dynamic, data-driven student themes based on their individual behavioral and academic "vibes". It replaces static student backgrounds with animated AGSL shaders that reflect their current classroom state.

## 🌈 The Vibes

The engine synthesizes five distinct vibes:

1.  **NEON_DREAM**: High positive balance and academic performance. Renders a cyan/magenta procedural flow.
2.  **CYBER_PUNK**: High activity frequency with mixed valence (chaotic). Features glitchy yellow highlights and scanlines.
3.  **ZEN_GARDEN**: Low frequency and stable performance. Uses soft, calming green and blue gradients.
4.  **VOID_RUNNER**: High negative frequency or academic struggle. Renders a deep red pulsing core.
5.  **SOLAR_FLARE**: Recent burst of positive interactions (last 15 minutes). Renders a bright, high-energy orange glow.

## 🛠️ Implementation Details

### 1. Vibe Synthesis (`GhostPrismEngine.kt`)
Uses a BOLT-optimized O(Recent) single-pass analysis loop. It evaluates behavior logs, quiz scores, and timing to map each student to a `Vibe` enum.

### 2. AGSL Shaders (`GhostPrismShader.kt`)
A single complex shader `PRISM_BACKGROUND` handles all five vibes using a `uVibe` uniform. This minimizes GPU program switches and improves performance during high-frequency seating chart interactions.

### 3. Compose Layer (`GhostPrismLayer.kt`)
An API 33+ component that utilizes `RuntimeShader` and `ShaderBrush`. It is integrated directly into `StudentDraggableIcon` to provide immersive, per-student aesthetics.

## ⚡ Performance Optimizations
- **Memoized Synthesis**: Student vibes are calculated in the `SeatingChartViewModel` background transformation pipeline and stored in a `LongSparseArray` to avoid O(N) calculations on the UI thread.
- **Shader Pooling**: Reuses `RuntimeShader` instances to minimize GPU memory pressure.
- **BOLT Manual Loops**: All analysis loops use index-based traversal to eliminate iterator churn.
