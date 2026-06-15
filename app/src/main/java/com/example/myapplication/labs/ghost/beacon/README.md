# 🚨 Ghost Beacon: Data-Driven Neural Student Selection

Ghost Beacon is an experimental "Need for Interaction" (NFI) engine that identifies students who require immediate teacher attention. It moves beyond simple "random student" picking by utilizing a weighted probability model driven by longitudinal behavioral and academic data.

## 🏛️ Architectural Components

The Beacon system is composed of three primary elements:

1.  **`GhostBeaconEngine` (The Brain)**: A selection engine that calculates an NFI score for every student in the current roster. It performs a single-pass aggregation of behavior and academic logs to build a weighted probability map, followed by a weighted random selection.
2.  **`GhostBeaconShader` (The Visuals)**: A volumetric AGSL shader that renders a "tractor beam" or searchlight effect over the selected student. It features procedural flickering, a tapered vertical beam, and a localized "splash" effect at the target coordinates.
3.  **`GhostBeaconLayer` (The Interface)**: A Jetpack Compose overlay that manages the activation state, triggers the volumetric visuals, and coordinates with `GhostHapticManager` to provide tactile confirmation when a target is identified.

## 📐 The "Need for Interaction" (NFI) Model

The engine calculates a student's NFI weight using several weighted factors:

-   **Base Weight**: 1.0f (Ensures every student has a baseline chance of being selected).
-   **Negative Behavior (2.0x)**: Each negative behavior log adds 2.0 to the weight, prioritizing students with recent disruptions.
-   **Positive Silence (Time Decay)**: Adds up to 5.0 to the weight based on the time elapsed since the last positive log (0.5 weight per hour of "silence").
-   **Academic Struggle (3.0x)**: Inversely proportional to academic performance. A student with a 0% average receives a +3.0 weight boost, while a 100% student receives 0.

## ⚡ BOLT Performance Optimizations

To ensure that the selection process is instantaneous even in large classrooms with extensive history:

-   **Single-Pass Aggregation**: The engine iterates through the behavior, quiz, and homework logs exactly once to build student-specific metric maps, avoiding $O(N \cdot L)$ nested loops.
-   **Weighted Probability Map**: Uses a cumulative weight algorithm for $O(N)$ selection complexity after the initial aggregation pass.
-   **Primitive Uniforms**: The shader utilizes primitive float uniforms for target positioning and intensity to minimize JNI overhead.

---
*Documentation love letter from Scribe 📜*
