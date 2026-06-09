# 🧶 Ghost Weaver: Neural Academic Synergy

This experiment visualizes "Neural Threads" of academic synergy and collaboration that form between students based on shared milestones and performance parity.

## 🛠️ Components

### 1. Neural Weaver Engine (`GhostWeaverEngine.kt`)
The analytical engine that identifies hidden academic connections.
- **Academic Synergy**: Detects links between students who achieve high scores (>= 80%) on the same quiz assessments.
- **Homework Collaboration**: Identifies students who consistently complete the same homework assignments.
- **BOLT ⚡ Optimization**:
    - **Single-Pass Pre-processing**: Transforms raw log lists into student-keyed `Set`s once per update, reducing pairwise comparison complexity from $O(S^2 \times L^2)$ to $O(S^2 \times L_{shared})$.
    - **Refined Heuristics**: Implements strict status checking (e.g., explicitly excluding "Not Done") to prevent false synergy markers.
    - **Manual Indexing**: Replaces functional iterators with manual loops in high-frequency paths.

### 2. Neural Thread Shader (`GhostWeaverShader.kt`)
A high-performance **AGSL Shader** that renders the interwoven fibers.
- **Interwoven Sine Waves**: Uses multiple offset sine waves to create the appearance of a "woven" neural thread.
- **Dynamic Glow**: Applies exponential decay glow centered on the primary and secondary wave paths.
- **Reactive Coloration**: Shifts between **Cyan** (Academic Synergy) and **Purple** (Homework Collaboration) to visualize the nature of the connection.

### 3. Neural Weaver Layer (`GhostWeaverLayer.kt`)
The Jetpack Compose rendering layer.
- **Shader Pooling**: Utilizes a persistent pool of `RuntimeShader` instances to avoid expensive allocations during 60fps rendering.
- **Efficient Student Lookup**: Uses `LongSparseArray` to map student IDs to UI items, avoiding the boxing overhead of standard `Map<Long, T>`.
- **Draw Pruning**: Replaces full-screen rectangle drawing with wide `drawLine` calls, restricting the fragment shader to pixels within the thread's influence zone.

## 💡 Implementation Secrets

- **Parity with Python**: The core synergy logic is verified against `verify_weaver_logic.py` to ensure mathematical consistency with the original R&D prototypes.
- **Thread Strength**: Connection strength (opacity/thickness) scales linearly with the number of shared milestones, capped at 3 quizzes or 5 homework assignments for visual stability.

---
*Documentation love letter from Scribe 📜*
