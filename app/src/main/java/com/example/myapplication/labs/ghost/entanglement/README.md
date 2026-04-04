# ⚛️ Ghost Entanglement: Quantum Social Synchronicity

The **Ghost Entanglement** experiment explores the concept of "Quantum Social Synchronicity" in the classroom. It models students not just as individuals, but as "entangled" nodes whose behaviors and performance are deeply interlinked through social and academic proximity.

## 🏛️ The "Quantum Social" Model

The engine calculates a **Coherence Score** (0.0 to 1.0) between pairs of students. This score represents the strength of their social "entanglement," derived from three distinct "Quantum Pillars":

### 1. Spatial Coherence (40% Weight)
Uses a **Gaussian Decay** model to calculate proximity-based connection strength.
- **Formula**: `exp(-dist^2 / (2 * σ^2))`
- **Sigma (σ)**: Calibrated at **600 logical units**.
- **Intent**: Students sitting near each other share a common physical "field," increasing the probability of social interaction.

### 2. Behavioral Synchronicity (30% Weight)
Measures the "Tempo" and consistency of behavioral logs.
- **Calculation**: Analyzes the variance of intervals between behavior events.
- **Logic**: Lower variance in log timing indicates a shared behavioral rhythm or "tempo," suggesting social contagion or synchronized engagement.

### 3. Academic Parity (30% Weight)
Evaluates similarity in academic performance (Quizzes and Homework).
- **Formula**: `1.0 - abs(StudentA_Avg - StudentB_Avg)`
- **Intent**: Students with similar academic trajectories often form stronger peer-support or competitive "links."

---

## 🎨 Visual Mapping: The "Spooky Action"

The `GhostEntanglementLayer` transforms these abstract scores into a futuristic, high-fidelity HUD using **AGSL Shaders**.

### 🌊 Quantum Ripples
Each entangled student radiates a glowing ripple. When students are highly coherent, these ripples interfere and merge, visually conveying their "shared state."

### 🌉 Entanglement Bridges
A glowing, pulsating bridge is rendered between students once their coherence exceeds the **0.8 threshold**. The pulse speed scales with the intensity of their behavioral synchronicity.

### ⚡ BOLT Optimization
To maintain 60fps interaction during student dragging:
- **Shader Pooling**: The layer utilizes a pool of `RuntimeShader` and `ShaderBrush` instances, avoiding per-frame object allocations.
- **Memoized Analysis**: The `GhostEntanglementEngine` pre-calculates node metrics during the background update cycle, ensuring that the UI thread only handles light-weight uniform updates.

---
*Documentation love letter from Scribe 📜*
