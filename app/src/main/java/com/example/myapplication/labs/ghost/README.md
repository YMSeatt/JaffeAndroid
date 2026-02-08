# 👻 Ghost Lab: Experimental Features

This directory contains experimental, future-facing features for the Seating Chart & Behavior Logger. These "Ghost" features explore the intersection of data visualization, automated layout optimization, and natural user interfaces.

## 🛠️ Components

### 1. Cognitive Proximity Engine (`GhostCognitiveEngine.kt`)
An automated layout optimizer that uses a **force-directed graph algorithm**.
- **Physics-Based Layout**: Treats students as physical bodies.
    - **Repulsion**: Inverse-square law repulsion ensures even distribution.
    - **Social Distance**: Students with negative logs repel others 2.5x more strongly.
    - **Attraction**: Linear spring-like attraction keeps student groups clustered.
- **Performance**: Optimized with primitive arrays and pre-calculated lookups for $O(N^2)$ simulation efficiency.
- **Integration**: Accessed via `SeatingChartViewModel.runCognitiveOptimization()`.

### 2. Tactical HUD & Neural Map (`GhostHUDLayer.kt`, `NeuralMap.kt`, `GhostShader.kt`)
Advanced visualization layers using **AGSL Shaders** (Android Graphics Shading Language).
- **Tactical Radar**: A 360-degree radar overlay that maps 2D seating chart coordinates to polar space relative to a virtual observer. Integrates with device rotation sensors for physical orientation awareness.
- **Neural Lines**: Visualize group connections using high-performance line shaders.
- **Cognitive Auras**: Pulsating red glows around students requiring attention, rendered with procedural noise shaders.

### 3. Ghost Oracle (`GhostOracle.kt`)
A predictive analysis engine (simulating on-device AI like Gemini Nano).
- **Social Friction**: Predicts tension when high-risk students are seated together.
- **Engagement Drop**: Flags students who haven't received positive feedback in over 7 days.
- **Confidence Scores**: Each "prophecy" includes a confidence metric used by the HUD to scale visual intensity.

### 4. Ghost Voice Assistant (`GhostVoiceAssistant.kt` & `GhostVisualizer.kt`)
A hands-free interface for classroom management.
- **Command Parsing**: Translates speech (e.g., "Log positive participation for John Doe") into database actions.
- **Neural Waveform**: A reactive shader-based visualizer that responds to voice amplitude in real-time.

### 4. Insight Engine (`GhostInsightEngine.kt` & `GhostInsightDialog.kt`)
A data analysis tool that generates behavioral and academic "insights."
- Categorizes students as **Peak Performers**, **Steady Progress**, or requiring **Attention/Academic Support** based on aggregated log data.

## 🚧 Status: Experimental
These features require `GhostConfig.GHOST_MODE_ENABLED = true` and target **API 33+** (for `RuntimeShader` support). They are intended for research and development and may be subject to rapid changes.
