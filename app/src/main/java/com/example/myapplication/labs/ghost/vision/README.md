# Ghost Vision: Neural AR Viewport

Ghost Vision is an experimental Augmented Reality (AR) module that allows teachers to "see" student data projected into a 3D virtual environment. By physically moving the device, the teacher can look around a virtual classroom where students are positioned based on their seating chart coordinates.

## 💡 The Neural AR Concept

The core idea is to transform a 2D seating chart into a 360-degree immersive viewport. This is achieved by mapping the logical seating chart canvas (4000x4000 units) onto a virtual sphere surrounding the teacher.

### 📐 Coordinate Projection Math

The projection logic in `GhostVisionEngine` translates 2D coordinates $(x, y)$ into angular coordinates (Azimuth and Pitch):

1.  **Azimuth (Horizontal Rotation):**
    The $x$ coordinate $[0, 4000]$ is mapped to an azimuth range of $[-\pi, \pi]$ (360 degrees).
    $$\text{Azimuth} = \frac{x - 2000}{2000} \times \pi$$

2.  **Pitch (Vertical Rotation):**
    The $y$ coordinate $[0, 4000]$ is mapped to a pitch range of $[-\pi/2, \pi/2]$ (180 degrees).
    $$\text{Pitch} = \frac{y - 2000}{2000} \times \frac{\pi}{2}$$

3.  **Field of View (FoV):**
    The engine uses a fixed **60-degree** Field of View. Students are only rendered if their angular delta from the device's current orientation is within this FoV.

## 🏗️ Architecture

- **`GhostVisionEngine`**: The "Brain" of the module. It handles:
    - **Sensor Fusion**: Uses the `TYPE_ROTATION_VECTOR` sensor for high-precision orientation tracking.
    - **Smoothing**: Applies a low-pass filter ($\alpha = 0.1$) to sensor inputs to prevent jitter.
    - **Projection**: Calculates the screen-space `Offset` for students based on the current orientation.

- **`GhostVisionLayer`**: The "Eyes". A Jetpack Compose layer that:
    - Renders the futuristic **AR HUD** overlay.
    - Animates **Neural Glyphs** for each projected student.
    - Displays orientation telemetry (Azimuth/Pitch).

- **`GhostVisionShader`**: The "Aesthetics". Contains AGSL shaders:
    - `AR_HUD`: Procedural scanning lines, grid patterns, and "Neural Static".
    - `VISION_GLYPH`: Pulsating data rings and bracket highlights for student nodes.

## 🚀 Performance (Bolt ⚡)

- **Shader Hoisting**: `ShaderBrush` and `RuntimeShader` instances are hoisted to prevent per-node allocations.
- **Allocation-Free Loops**: Uses manual index-based loops in the drawing path to avoid iterator churn.
- **Low-Pass Efficiency**: Low-overhead linear interpolation for sensor smoothing.
