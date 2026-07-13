# 🧬 Ghost Helix: Neural DNA Visualization

Ghost Helix is an experimental visualization layer that transforms student behavioral and academic data into a unique, animated "Neural DNA" sequence. It provides a high-fidelity representation of a student's classroom trajectory using a double-helix metaphor.

## 🧠 The Metaphor: Neural DNA

In this model, every interaction (behavioral or academic) is treated as a "Base Pair" in a student's digital genome. By sequencing these events chronologically, the engine builds a unique helix that reflects the student's history, stability, and momentum.

### 🧬 Genetic Mapping (Base Pairs)
The `GhostHelixEngine` maps logs to four distinct base pairs:

| Base Pair | Representation | Data Source |
| :--- | :--- | :--- |
| **Adenine (A)** | Positive Engagement | Positive Behavior Logs |
| **Thymine (T)** | Behavioral Turbulence | Negative Behavior Logs |
| **Cytosine (C)** | Peak Performance | Academic Marks > 60% |
| **Guanine (G)** | Academic Turbulence | Academic Marks < 60% |

## 🛠️ Implementation Details

### 1. Helix Sequencing (`GhostHelixEngine.kt`)
The engine performs a multi-stage analysis of student data:
- **Chronological Sequencing**: Logs are sorted by timestamp to ensure the helix represents a true temporal trajectory.
- **Stability Calculation**: Calculated as the ratio of positive (A, C) to negative (T, G) base pairs. Lower stability introduces visual "jitter" in the shader.
- **Twist Rate**: Driven by total academic activity. High-performing or high-activity students exhibit a more tightly wound and rapidly rotating helix.
- **Trajectory Analysis**: A weighted scoring system (A: 1.0, C: 0.8, T: -1.0, G: -0.6) that determines the global "Social/Academic Momentum" of the student.

### 2. AGSL Rendering (`GhostHelixShader.kt`)
The visualization is rendered using a high-performance **Android Graphics Shading Language (AGSL)** shader:
- **3D Projection**: Simulates a rotating 3D double helix using sinusoidal displacement and depth-based shading.
- **Procedural Jitter**: Injects high-frequency noise into the vertex positions based on the `iStability` uniform.
- **Dynamic Color Refraction**: The helix color shifts between **Ghost Cyan** (Positive Trajectory) and **Ghost Magenta** (Turbulent Trajectory) based on the calculated trajectory score.
- **Base Pair Rungs**: Renders horizontal connectors between the two DNA strands at regular intervals.

### 3. UI Integration (`GhostHelixLayer.kt`)
The layer is implemented as a Compose `graphicsLayer` effect:
- **Content Wrapping**: Can be wrapped around any student UI component.
- **Runtime Shader Effect**: Uses `RenderEffect.createRuntimeShaderEffect` (API 33+) to apply the helix as a post-processing layer over the student icon.

## 🔄 Logic Parity
This implementation maintains strict logical parity with the research prototype in `Python/ghost_helix_analysis.py`.

---
*Documentation love letter from Scribe 📜*
