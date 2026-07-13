# 🧠 Ghost Strategist: Generative AI Tactical Co-Pilot

The **Ghost Strategist** is an experimental pedagogical advisor that leverages (simulated) on-device AI to provide real-time, actionable interventions for classroom management. It transforms raw behavioral and academic data into high-fidelity "Tactical Interventions."

## 🏛️ Architectural Intent

The Strategist acts as a cognitive bridge between high-level data analysis and immediate classroom action. It moves beyond simple visualization (like heatmaps or graphs) by providing concrete, worded recommendations tailored to specific students and classroom dynamics.

## 🎯 Pedagogical Goals

The engine operates under three primary strategic objectives, which the teacher can toggle to prioritize different classroom outcomes:

1.  **Harmony**: Focuses on resolving social friction, de-escalating negative behavioral loops, and improving interpersonal dynamics.
2.  **Excellence**: Targets high-performing students to ensure they remain challenged, providing recommendations for leadership roles or advanced academic depth.
3.  **Stability**: Focuses on maintaining a consistent baseline of engagement across the classroom, specifically identifying students who are beginning to "drift" or lose focus.

## 🛠️ Technical Implementation

### 1. Simulated AI Synthesis (`GhostStrategistEngine.kt`)
While currently implemented as a high-fidelity heuristic model, the architecture is designed to integrate with **Android AICore (Gemini Nano)**.
- **Latency Emulation**: The engine includes a `1200ms` delay to simulate the inference time of a large language model.
- **Prophecy Mapping**: It ingests "Prophecies" from the `GhostOracle` and transforms them into natural language tactics.
- **Heuristic Logic**: Uses temporal analysis (e.g., 1-hour windows for negative behavior density) to detect emerging crises before they escalate.

### 2. Neural Stream Visualization (`GhostStrategistShader.kt`)
To convey the "thought process" of the AI, the interface utilizes a specialized **AGSL Neural Stream Shader**.
- **Visual Feedback**: A flowing, white-and-cyan data stream appears behind the UI when the engine is "thinking" or when high-urgency interventions are present.
- **Atmospheric Alpha**: The shader's opacity and intensity are dynamically linked to the AI's confidence levels.

### 3. High-Fidelity Haptics (Android 15+)
The Strategist utilizes nuanced tactile feedback to alert teachers to high-urgency interventions without requiring them to look at the screen.
- **Haptic Alerts**: Uses `VibrationEffect.Composition` with `PRIMITIVE_SPIN` and `PRIMITIVE_TICK` for interventions with urgency > 0.9.

### 4. Sandbox Environment (`GhostStrategistActivity.kt`)
A dedicated activity provides a controlled environment for testing and demonstrating the strategist's logic using mocked classroom datasets.

---
*Documentation love letter from Scribe 📜*
