# 📡 Ghost Sonar: Spatial Engagement Discovery

Ghost Sonar is an experimental diagnostic tool that identifies "Quiet Zones" in the classroom. It uses a spatiotemporal sweep to find students who have not had any behavioral or academic interactions within a recent time window, ensuring that no student is overlooked.

## 🏛️ Architectural Components

The Sonar system is composed of three primary elements:

1.  **`GhostSonarEngine` (The Intelligence)**: Analyzes the classroom's longitudinal history (Behavior, Quizzes, Homework) to identify "Quiet" students. It uses a sliding time window (default 10 minutes) to determine engagement status.
2.  **`GhostSonarShader` (The Wavefront)**: An AGSL shader that renders an expanding circular "ping" ripple. It features smooth-step edge softening and temporal fading.
3.  **`GhostSonarLayer` (The Orchestrator)**: A Compose layer that manages the sweep animation, renders the shader, and performs real-time intersection tests to trigger tactile feedback.

## 📐 Interaction & Logic

### 1. The Sonar Sweep
When activated, the sonar originates from a target point (usually the teacher's current focus) and expands to cover the entire 4000x4000 logical canvas.

### 2. Quiet Student Identification
The engine evaluates engagement based on three data streams:
-   **Behavioral**: Presence of any `BehaviorEvent`.
-   **Academic (Quizzes)**: Presence of any `QuizLog` entry.
-   **Academic (Homework)**: Presence of any `HomeworkLog` entry.

Students with **zero** logs in the active window are flagged as "Quiet" and become targets for the sonar's haptic intersection.

### 3. Tactile Feedback (Haptic Pings)
As the visual wavefront intersects with the center of a "Quiet" student's icon, the system triggers a precise haptic ping (`UI_CLICK`). This allows teachers to "feel" the presence of quiet students as they scan the classroom.

## ⚡ BOLT Performance Optimizations

To maintain a fluid 60fps experience during the high-frequency sweep animation:

-   **O(Recent) Analysis**: The engine utilizes chronologically DESC-sorted logs with early-exit loops to minimize search space.
-   **HashSet Lookups**: Active students are mapped into a `HashSet` for $O(1)$ lookup complexity during the identification pass.
-   **Zero-Allocation Intersection**: The intersection logic in the `Canvas` layer uses manual index-based loops to avoid `Iterator` churn and GC pressure during the 3000ms animation.
-   **Hoisted Shaders**: `RuntimeShader` and `ShaderBrush` instances are hoisted and `remember`ed to prevent per-frame GPU state re-allocations.

---
*Documentation love letter from Scribe 📜*
