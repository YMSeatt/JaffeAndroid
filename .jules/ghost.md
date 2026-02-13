# 👻 Ghost's Journal: R&D & "What if?" Scenarios

## 🌀 Experiment: Ghost Portal (Drag & Drop Evolution)
**Date:** 2026-02-13
**Status:** PROTOTYPE COMPLETE

### 🌟 The Vision
In 2027, the boundaries between applications will blur. "Ghost Portal" is a PoC for a future where classroom data is fluid. Teachers can "teleport" students between different classroom environments or external AI analyzers with a single gesture.

### 🛠️ The Tech
- **Android 15 Drag & Drop:** Integrated `Modifier.dragAndDropSource` and `Modifier.dragAndDropTarget` into the Compose UI. This allows for system-level data transfer that works even between different app instances in multi-window mode.
- **AGSL Shaders:** Created `GhostPortalShader` to provide a futuristic "Wormhole" effect, making the abstract concept of data transfer tangible and visually engaging.
- **Inter-App Communication:** Used `ClipData` with JSON payloads to ensure compatibility with external tools.

### 🔦 The Discovery
- **Android 15's Edge-to-Edge:** Apps targeting API 35 are edge-to-edge by default, which makes full-screen shader overlays (like the Portal Layer) look even more immersive.
- **Compose Drag & Drop:** The new `dragAndDropSource` modifier is much more declarative than the legacy `View.startDragAndDrop` approach, allowing for seamless integration with Composable state.

### 💡 The "What if?"
*What if we could "drag" a student into a virtual group, and the app automatically simulated the social outcome?*
- The `Ghost Link` Python bridge demonstrates this by acting as an "External Neural Analyzer" that generates a predictive dossier from the portal payload.

### 🚧 Limitations
- System Drag & Drop requires a long-press gesture by default in this PoC to distinguish from standard pan/zoom gestures in the seating chart.
- Multi-window support is essential to see the "portal" effect in action between two instances of the app.

---
*Ghost - Rapid Prototyping for the Classroom of 2027*
