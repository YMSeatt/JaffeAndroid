# 🎡 Ghost Hub: Radial Quick-Action Menu

The **Ghost Hub** is a futuristic, radial quick-action menu designed for high-frequency interaction within the Ghost Lab experimental suite. It allows teachers to rapidly toggle advanced visualization modes (HUD, Vision, Phantasm, Spectra, Aurora, Future) using an intuitive "Long-press and Swipe" gesture.

## 🏛️ The Radial Interaction Model

The Hub is designed for speed and eyes-free operation. Instead of hunting through traditional menus, the user interacts with a 360-degree field of options centered around their touch point.

### 🖖 Gestures & Navigation
- **Activation**: Triggered by a **Long-press** on the seating chart background.
- **Selection**: While maintaining contact with the screen, the user drags their finger towards one of the radial segments.
- **Haptic Guidance**: As the user's finger passes over different segments, the [GhostHapticManager](../util/GhostHapticManager.kt) triggers sharp `UI_CLICK` ticks, providing tactile confirmation of the active selection.
- **Execution**: Releasing the touch over a segment activates the corresponding Ghost mode and triggers a `SUCCESS` haptic pattern.
- **Dismissal**: Releasing the touch near the center or outside the segments cancels the action.

---

## 🎨 Visual Architecture: AGSL Radial Background

The Hub utilizes **AGSL (Android Graphics Shading Language)** to create a high-performance, responsive background that reflects the user's interaction in real-time.

### ✨ Shader Features (`GhostHubShader.kt`)
- **Orbital Glow**: A procedural core glow that pulses with time, drawing focus to the interaction center.
- **Segment Borders**: Sharp radial lines that mathematically divide the 360-degree field based on the number of available actions.
- **Selection Sweep**: A dynamic highlight sweep that rotates to follow the user's finger, driven by the `iSelectedAngle` uniform.
- **Neural Noise**: Subtle background jitter and color shifts that maintain the "Ghost" aesthetic.

---

## ⚡ Performance & Optimization

To ensure the Hub remains responsive during complex classroom simulations:
1.  **Gesture-Logic Decoupling**: Selection logic is handled within a custom `pointerInput` loop using `awaitEachGesture`, ensuring low-latency tracking independent of the main Compose layout pass.
2.  **Shader Efficiency**: The radial background is rendered as a single rectangle with a [ShaderBrush], minimizing draw calls and leveraging the GPU for all math-heavy coordinate transformations (e.g., `atan2` calculations for segment selection).
3.  **Haptic Throttling**: The engine tracks the `activeActionIndex` and only triggers haptic feedback when the selection actually changes, preventing vibrator saturation.

---
*Documentation love letter from Scribe 📜*
