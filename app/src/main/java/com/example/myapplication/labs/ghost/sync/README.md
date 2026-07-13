# 👻 Ghost Sync

A real-time **Collaboration Visualizer** for the Ghost Lab.

## 🔗 Overview
Ghost Sync identifies students who are operating in "Sync" — sharing similar behavioral rhythms, group membership, and spatial proximity. It visualizes the invisible threads of collaboration in the classroom as glowing "Neural Bridges."

## 🛠️ Components
- **GhostSyncEngine.kt**: Analyzes student state, group parity, and behavioral synchronicity to identify active sync links.
- **GhostSyncShader.kt**: High-performance AGSL shader for pulsing neural interference patterns.
- **GhostSyncLayer.kt**: Jetpack Compose layer that renders the bridges over the seating chart.

## ⚡ BOLT Optimizations
- **Shader Pooling**: Reuses `RuntimeShader` instances to avoid per-link allocations.
- **Pre-indexed Lookups**: Uses `associateBy` to transform O(N) student searches into O(1).
- **DESC Early Exit**: Optimized log scanning in the engine.
