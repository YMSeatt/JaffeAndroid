# 🕸️ Ghost Lattice: Social Dynamics Visualization

## Overview
The **Ghost Lattice** engine is a real-time social dynamics visualizer that maps student relationships as a glowing, interconnected neural network. By analyzing spatial proximity and behavioral history, it identifies the "Social Fabric" of the classroom, helping educators visualize collaboration, friction, and engagement clusters.

## Social Metaphor

### 🧠 The Neural Lattice
The classroom is modeled as a neural graph where students are "Nodes" and their relationships are "Edges" (Connections). The lattice is dynamic; as students move or receive behavioral logs, the connections stretch, pulse, and shift in color.

### ⛓️ Connection Types
The engine classifies relationships into three primary categories, which drive the AGSL shader's visual output:

| Type | Description | Visual Representation |
| :--- | :--- | :--- |
| **COLLABORATION** | Positive synergy and frequent interaction. | Glowing Cyan/Green pulses. |
| **FRICTION** | Potential tension or negative history. | Pulsing Red with high-frequency interference. |
| **NEUTRAL** | Standard proximity-based connection. | Deep Blue steady glow. |

## BOLT Performance Optimizations ⚡
To maintain 60fps on a 4000x4000 logical canvas with up to 50 active connections, the following optimizations are implemented:

- **Edge Growth Capping**: The engine caps the total number of inferred edges at **50** to prevent visual clutter and JNI overhead.
- **Shader & Brush Pooling**: `GhostLatticeLayer` utilizes a growable pool of `RuntimeShader` and `ShaderBrush` objects to eliminate per-frame allocations and prevent the "Uniform Overwrite" bug.
- **Spatial Heuristics**: Proximity detection uses a squared-distance threshold (**800²**) to avoid expensive `sqrt` calls during the initial scan.
- **Bounding Box Clipping**: The layer only draws the specific `drawRect` bounding box for each connection, minimizing the fragment shader's pixel-fill requirements.

## Implementation Details
- **Engine**: `GhostLatticeEngine.kt` performs O(N²) proximity scans, optimized for O(Recent) log analysis.
- **Layer**: `GhostLatticeLayer.kt` handles high-performance Canvas rendering with AGSL integration.
- **Shader**: `GhostLatticeShader.kt` contains the `NEURAL_LATTICE` AGSL source, featuring distance-based glows and temporal energy pulses.
- **Parity**: Logically aligned with `Python/ghost_lattice.py`, but scaled 2x for the Android logical coordinate system.

---
*Documentation love letter from Scribe 📜*
