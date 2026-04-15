# 🎯 Ghost Ray: Neural Directional Pointer

The Ghost Ray is an experimental spatial navigation tool that allows teachers to interact with the seating chart using their device's physical orientation. It projects a volumetric "Neural Beam" into the digital classroom, enabling hands-free student selection and tactile feedback.

## 🚀 Key Features

- **Spatial Intersection**: Uses the device's rotation sensors to "point" at student icons on the 2D canvas.
- **Intersection Haptics**: Triggers a tactile "Thud" when the ray intersects with a student, enabling eyes-free navigation and node identification.
- **Volumetric Visualization**: Renders a high-performance AGSL beam with procedural pulsing and chromatic aberration.
- **State-Aware UI**: The beam shifts from **Cyan** (Observation) to **Magenta** (Intersection/Focus) based on its target.

## 🛠️ Implementation Details

### 1. Sensor Fusion & Coordinate Mapping
The `GhostRayEngine` utilizes the Android **Rotation Vector Sensor** (`Sensor.TYPE_ROTATION_VECTOR`) to track the device's 3D orientation. It maps the device's pitch and roll to the 2D seating chart coordinate system:

- **X-Axis Mapping**: `orientation[2]` (Roll) is scaled by `2000f` and offset by `500f`.
- **Y-Axis Mapping**: `-orientation[1]` (Pitch) is scaled by `2000f` and offset by `500f`.

This mapping ensures that tilting the device left/right or up/down translates into natural movement on the 4000x4000 logical canvas.

### 2. Optimized Intersection Logic
To maintain 60fps performance during complex sensor updates, the intersection engine employs a **Squared Distance Thresholding** strategy. Instead of calculating the expensive `sqrt()` for every student node, it compares the squared distance between the ray target and student center:

```kotlin
val thresholdSq = (60f * canvasScale) * (60f * canvasScale)
val distSq = dx * dx + dy * dy
if (distSq < thresholdSq) { /* Intersection Detected */ }
```

### 3. Neural Beam Shader (`NEURAL_BEAM`)
The visualization is powered by an AGSL shader that simulates a data-driven beam:
- **Core**: A high-intensity center calculated via distance-to-segment.
- **Halo**: A pulsating outer glow driven by `iTime` and a sine-wave frequency.
- **Dispersion**: Chromatic aberration at the beam edges to simulate data "refraction".

### 4. Somatic Feedback
The engine utilizes `VibrationEffect.Composition` (on supported API 31+ devices) to trigger nuanced haptics.
- **PRIMITIVE_THUD**: Delivered at 0.6f intensity when the ray enters a new student's intersection volume.

## 🧪 Calibration
The `GhostRayActivity` provides a standalone sandbox to test the mapping constants and haptic sensitivity without the overhead of the full seating chart database.

---
*Documentation love letter from Scribe 📜*
