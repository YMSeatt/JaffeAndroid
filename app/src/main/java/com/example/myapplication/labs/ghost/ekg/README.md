# 👻 Ghost EKG: Neural Vitality Monitor

Ghost EKG (#83) implements a real-time biometric visualization of student biological rhythms. It translates behavioral and academic data into a scrolling waveform (EKG) that reflects the student's internal "state."

## 🔬 Metaphor: Biometric Resonance
The EKG represents the student as a living, breathing participant in the classroom ecosystem. By visualizing their "heartbeat," we surface hidden patterns of engagement and tension.

## 🛠️ Components

### 1. The Logic Engine (`GhostEKGEngine.kt`)
Synthesizes a normalized waveform signal (0.0 to 1.0).
- **Vitality Modulation**: Amplitude is driven by the student's vitality (engagement).
- **Frequency Modulation**: "Heart Rate" increases with student stress (Negative logs).
- **Interaction Spikes**: Recent behavior events create momentary spikes in the signal.
- **BOLT Optimization**: Zero-allocation synthesis loop for 60fps performance.

### 2. The AGSL Shader (`GhostEKGShader.kt`)
Renders the waveform with futuristic CRT aesthetics.
- **Glowing Trace**: A high-fidelity line that glows brighter at signal peaks.
- **CRT Grid**: A background grid that scrolls with the signal.
- **Scanlines**: Subtle horizontal scanlines for a "biometric monitor" feel.
- **Dynamic Palette**: Colors shift from Cyan (Stable) to Magenta (Stressed) based on real-time metrics.

### 3. The Compose Layer (`GhostEKGLayer.kt`)
A glassmorphic overlay for the seating chart.
- **Waveform History**: Maintains a 100-point buffer for smooth scrolling.
- **Biometric HUD**: Displays vitality percentage and status alerts.

## 🚀 Integration
- **Toggle**: Accessible via the **Student Hub** (Long-press student -> `Icons.Default.MonitorHeart`).
- **State**: Integrated into the `SeatingChartViewModel` background update pipeline.
