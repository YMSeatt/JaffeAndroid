# 👻 Ghost Phoenix: Neural Resilience & Recovery Visualization

Ghost Phoenix is an experimental classroom visualization that identifies and celebrates student **resilience**. It uses spatiotemporal analysis of behavioral history to highlight students who have overcome a period of struggle and are now exhibiting a strong recovery.

## 🏹 The "Phoenix" Metaphor

In the context of the classroom, a "Phoenix" is a student who has risen from the "ashes" of past behavioral challenges. The engine looks for a specific pattern:
1. **The Struggle**: A baseline of negative behavioral logs in the historical window.
2. **The Recovery**: A recent streak of positive engagement logs.

When this pattern is detected, the student is granted a "Phoenix" status, accompanied by a procedural fire aura in the seating chart UI.

## 🛠️ Technical Implementation

### Resilience Engine (`GhostPhoenixEngine.kt`)
The engine performs a single-pass analysis of a student's behavioral history using two sliding windows:
- **Struggle Window**: The last 24 hours (excluding the recovery window). It looks for negative incidents to establish the "ashes."
- **Recovery Window**: The last 2 hours. It looks for positive incidents to establish the "rising."

### The Resilience Formula
A student's resilience score (0.0 to 1.0) is calculated as:
`Score = (RecentPositiveCount * 0.2) + (HistoricalNegativeCount * 0.1)`

*Note: The score is only calculated if `HistoricalNegativeCount > 0`. A student who is consistently good is not a "Phoenix" in this specific metaphor.*

### "Phoenix Rising" Shader (`GhostPhoenixShader.kt`)
The visualization is powered by an AGSL shader featuring:
- **Procedural Fire Aura**: Uses Fractal Brownian Motion (fbm) noise to create a flickering flame effect around the student icon.
- **Rising Embers**: A hash-based particle system that simulates embers ascending from the student, with frequency and intensity scaled by the resilience score.

## ⚡ BOLT Performance Optimizations

Ghost Phoenix is designed to maintain 60fps on mid-range hardware through several optimization strategies:
- **Single-Pass Analysis**: `GhostPhoenixEngine` traverses chronologically descending logs once, terminating early when it exits the 24-hour window ($O(\text{Recent})$).
- **Shader Pooling**: `GhostPhoenixLayer` maintains a pre-allocated pool of `RuntimeShader` instances. This prevents "Uniform Overwrite" bugs where multiple students drawing with the same shader object would overwrite each other's parameters (like position and resilience score) before the GPU draw calls execute.
- **Hoisted State**: Animation time and student positions are hoisted out of the draw loop to minimize `MutableState` reads during the high-frequency `Canvas` pass.

## 🧪 Logic Verification
The core resilience logic is verified via `verify_phoenix_logic.py`, ensuring that the mathematical model remains consistent with the Python-based R&D prototypes.

---
*Documentation love letter from Scribe 📜*
