# 🪸 Ghost Coral: Social Reef & Collaborative Growth

Ghost Coral is an experimental visualization that models long-term collaborative stability in the classroom as an organic reef.

## 🧪 The Metaphor: Social Calcification
In nature, coral reefs are built slowly over time through the accumulation of calcium carbonate. In the Ghost Lab, **Social Calcification** is the process where student relationships are strengthened through consistent, positive interactions in close spatial proximity.

## 🛠️ Technical Implementation

### 1. GhostCoralEngine.kt
The engine uses an $O(S^2)$ proximity pass combined with an $O(Recent)$ log analysis pipeline.
- **Calcification Potential**: Calculated for each student based on the frequency of positive behavior logs over a 24-hour window.
- **Branch Synthesis**: When two students with growth potential are within 800 units of each other, a "Coral Branch" is synthesized.
- **Density & Vitality**:
    - **Density**: Driven by shared calcification potential and Gaussian spatial decay.
    - **Vitality**: Driven by behavioral parity (how "in sync" the students are).

### 2. GhostCoralShader.kt
A high-performance AGSL shader that renders procedural branches.
- **Domain Warping**: Uses fractal Brownian motion (FBM) to create organic, non-linear branch shapes.
- **Neural Glow**: Internal "neural fire" pulses within the coral, reflecting the vitality of the connection.

### 3. GhostCoralLayer.kt
A zero-allocation Compose layer optimized for 60fps rendering. It uses `RuntimeShader` and `ShaderBrush` to draw the reef directly onto the seating chart canvas.

## ⚡ BOLT Optimizations
- **Memoized Synthesis**: The reef is only re-calculated in the background when students move or new logs are recorded.
- **Primitive Hoisting**: Expensive shader uniforms and brushes are hoisted to prevent per-frame object churn.
- **Gaussian Decay**: Uses efficient exponential decay to model spatial influence without hard cutoffs.

## 🔄 Logic Parity
The core logic is verified via `verify_coral_logic.py`, ensuring mathematical consistency between the R&D simulation and the native implementation.
