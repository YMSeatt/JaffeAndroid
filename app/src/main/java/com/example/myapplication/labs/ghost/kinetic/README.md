# 👻 Ghost Kinetic: Physics-Based Seating Interaction

The **Ghost Kinetic** experiment introduces tactile, physics-based momentum to the seating chart interface. It allows student icons to glide across the canvas with realistic friction after a "flick" gesture, making classroom organization feel more organic and responsive.

## 🌟 The Vision
In a high-fidelity interaction model, UI elements should obey the laws of digital physics. By adding momentum to student icons, we reduce the effort required to move students across large classroom layouts and provide immediate, satisfying feedback to the teacher's gestures.

## 🛠️ Components

### 1. Kinetic Engine (`GhostKineticEngine.kt`)
The mathematical core of the momentum system.
- **Exponential Decay**: Uses a standard exponential decay model for natural deceleration.
- **Friction Tuning**: Calibrated with a friction coefficient of `1.1f` to ensure smooth movement that doesn't overshoot the intended target too aggressively.
- **Velocity Threshold**: Only triggers kinetic glide if the release velocity exceeds `200 pixels/sec`.

### 2. Gesture Integration (`StudentDraggableIcon.kt`)
The interaction layer that captures and applies kinetic energy.
- **Velocity Tracking**: Utilizes Compose's `VelocityTracker` to monitor the speed of the user's finger in logical coordinate units.
- **Animated Glide**: Triggers a coroutine-based decay animation on `onDragEnd`, updating student positions in real-time.
- **Async Reconciliation**: Synchronizes the final "resting" position with the database only after the glide completes.

## ⚡ BOLT Optimizations

- **Allocation-Free Tracking**: Reuses a single `VelocityTracker` instance per student icon to avoid GC pressure during high-frequency dragging.
- **State-Direct Updates**: Updates the `MutableState` properties of `StudentUiItem` directly during the glide, maintaining 60fps even as multiple icons are tossed around.
- **Efficient Decay**: Leverages the native `Animatable.animateDecay` API for hardware-optimized performance.

## 🚧 Status: Experimental
Requires `GhostConfig.KINETIC_MODE_ENABLED = true`. Integrated directly into the seating chart's standard drag gestures.

---
*Documentation love letter from Scribe 📜*
