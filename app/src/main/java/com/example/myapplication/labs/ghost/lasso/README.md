# 👻 Ghost Lasso: Neural Gesture Multi-Selection

The **Ghost Lasso** experiment introduces a high-performance, gesture-driven multi-selection tool for the seating chart. Instead of tapping individual students in a dedicated selection mode, teachers can draw a "Neural Lasso" around groups of students to select them instantly.

## 🌟 The Vision
In 2027, interaction is fluid. "Ghost Lasso" bridges the gap between individual assessment and group management by allowing teachers to "corral" students with a single, expressive gesture.

## 🛠️ Components

### 1. Spatial Selection Engine (`GhostLassoEngine.kt`)
A geometric analysis engine that performs high-speed Point-in-Polygon detection.
- **Ray Casting Algorithm**: Implements a single-pass Jordan Curve Theorem calculation to determine if student coordinates fall within the gesture boundary.
- **Point Resampling**: Normalizes raw touch streams to consistent intervals for mathematical stability.
- **BOLT Optimization**: Optimized for 60fps tracking during high-density drag operations.

### 2. Neural Lasso Shader (`GhostLassoShader.kt`)
An **AGSL Shader** that provides real-time visual feedback for the lasso.
- **Neon Path**: Renders a shimmering, cyan neon trail along the touch points.
- **Neural Glow**: Adds a semi-transparent, pulsating blue glow to the enclosed area to confirm the selection field.
- **Dynamic Shimmer**: Uses temporal noise to simulate data "energy" flowing through the selection boundary.

### 3. Ghost Lasso Layer (`GhostLassoLayer.kt`)
A Compose layer that orchestrates the gesture capture and selection synchronization.
- **Gesture Capture**: Intercepts `detectDragGestures` to build the lasso path in real-time.
- **ViewModel Integration**: Automatically switches the seating chart to `selectMode` and populates `selectedItemIds` upon gesture completion.
- **Fade Animation**: Implements a smooth neural fade-out after selection is finalized.

## 🚀 Activation
Ghost Lasso can be activated via:
1.  **Ghost Hub**: Long-press the background and slide to the "Neural Lasso" action.
2.  **Top Bar Menu**: Access via the overflow menu under "Ghost Lasso 👻".

---
*Ghost - Rapid Prototyping for the Classroom of 2027*
