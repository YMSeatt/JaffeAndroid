# 👻 Ghost Lab: Experimental Features

This directory contains experimental, future-facing features for the Seating Chart & Behavior Logger. These "Ghost" features explore the intersection of data visualization, automated layout optimization, and natural user interfaces.

## 🛠️ Components

### 1. Cognitive Proximity Engine (`GhostCognitiveEngine.kt`)
An automated layout optimizer that uses a **force-directed graph algorithm**.
- **Attraction**: Students in the same group are pulled together.
- **Repulsion**: All students repel each other to maintain space, with significantly higher repulsion for students with high "negative behavior" scores to suggest social distancing in the seating chart.
- **Integration**: Accessed via `SeatingChartViewModel.runCognitiveOptimization()`.

### 2. Neural Map (`NeuralMap.kt` & `GhostShader.kt`)
A visualization layer that renders on top of the seating chart using **AGSL Shaders** (Android Graphics Shading Language).
- **Neural Lines**: Visualize connections between group members.
- **Cognitive Auras**: Visual indicators (pulsating red glows) around students who have reached a threshold of negative behavior events.
- **Neural Pulse**: Background effects used in insights to indicate "active analysis."

### 3. Ghost Voice Assistant (`GhostVoiceAssistant.kt` & `GhostVisualizer.kt`)
A hands-free interface for classroom management.
- **Command Parsing**: Translates speech (e.g., "Log positive participation for John Doe") into database actions.
- **Neural Waveform**: A reactive shader-based visualizer that responds to voice amplitude in real-time.

### 4. Insight Engine (`GhostInsightEngine.kt` & `GhostInsightDialog.kt`)
A data analysis tool that generates behavioral and academic "insights."
- Categorizes students as **Peak Performers**, **Steady Progress**, or requiring **Attention/Academic Support** based on aggregated log data.

## 🚧 Status: Experimental
These features require `GhostConfig.GHOST_MODE_ENABLED = true` and target **API 33+** (for `RuntimeShader` support). They are intended for research and development and may be subject to rapid changes.
