# 💓 Ghost Pulse: Neural Resonance Visualization

This experiment visualizes the "rhythm" of the classroom by mapping behavioral events to expanding ripples on the seating chart. It explores the concept of **Neural Resonance**, where simultaneous student activity creates constructive interference patterns in the classroom's data field.

## 🏛️ The "Neural Resonance" Metaphor

Much like ripples in a pond, every behavioral log recorded in the system creates a "Data Wave" that emanates from the student's spatial position.
- **Simultaneity**: When multiple students interact with the system within a tight temporal window (5 seconds), their pulses overlap.
- **Interference**: The AGSL shader calculates the combined intensity of these waves, highlighting areas of high activity through visual constructive interference.
- **Valence Coding**:
    - **Cyan/Green**: Positive behavioral resonance.
    - **Red**: Negative behavioral resonance.
    - **Blue**: Neutral or participation-based resonance.

## ⚡ BOLT Performance Architecture

To maintain 60fps while rendering up to 20 concurrent procedurally-generated ripples, the Pulse suite utilizes several **BOLT** (Performance-Obsessed) patterns:

### 1. O(Recent) Analysis Engine
The `GhostPulseEngine` avoids the $O(L)$ cost of scanning the entire behavior history.
- **DESC Sorting**: It assumes logs are sorted in descending chronological order.
- **Early Exit**: The analysis loop terminates immediately once it encounters a log older than the 5-second resonance window.
- **Manual Loops**: Replaces functional iterators with index-based `for` loops to eliminate `Iterator` object churn.

### 2. High-Frequency Rendering Optimizations
The `GhostPulseLayer` is designed for zero-allocation rendering:
- **Buffer Pooling**: Pre-allocates `FloatArray` buffers for pulse centers, colors, and intensities to avoid per-frame allocations during the `Canvas` draw pass.
- **Hoisted Shaders**: Reuses a single `RuntimeShader` instance across frames, updating uniforms just-in-time.
- **State Debouncing**: The `calculateResonance` logic is decoupled from the 60fps draw loop, running every 500ms to conserve CPU and battery life while maintaining the appearance of real-time responsiveness.

### 3. AGSL Shader Hardening
The `NEURAL_PULSE` shader is optimized for mobile GPUs:
- **Squared Distance**: Replaces expensive `distance()` calls with dot products where possible to eliminate `sqrt` overhead.
- **Noise Hoisting**: Fractal noise is calculated once per fragment and reused across the pulse accumulation loop.
- **Strict Branching**: Uses early `break` statements in the AGSL loop to skip processing for inactive pulse slots.

## 🛠️ Components

- **`GhostPulseEngine.kt`**: The logic center. Synthesizes recent logs into `ResonancePulse` objects.
- **`GhostPulseLayer.kt`**: The Compose UI layer. Manages the shader lifecycle, coordinate mapping, and buffer synchronization.
- **`GhostPulseShader.kt`**: The AGSL source. Implements the procedural ripple and interference math.

---
*Documentation love letter from Scribe 📜*
