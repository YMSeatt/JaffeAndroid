# 👻 Ghost Navigator: Spatial Orientation Engine

Ghost Navigator is a high-performance mini-map system designed to provide spatial orientation on the seating chart's 4000x4000 logical canvas. It allows teachers to quickly identify student clusters and navigate large classroom layouts via "Tap-to-Teleport".

## 🛠️ Components

### 1. Navigator Engine (`GhostNavigatorEngine.kt`)
The mathematical core of the feature.
- **Coordinate Mapping**: Translates high-fidelity 4000x4000 world space coordinates to a normalized 0.0-1.0 range for rendering.
- **Viewport Calculation**: Determines the visible "viewfinder" rectangle based on the current pan offset and zoom scale of the main seating chart.
- **Teleport Logic**: Calculates the precise pan offset required to center the seating chart on a specific logical coordinate selected via the mini-map.

### 2. Navigator Layer (`GhostNavigatorLayer.kt`)
The visual interface for spatial navigation.
- **Glassmorphic Overlay**: Renders as a semi-transparent, stylized mini-map in the bottom-right corner.
- **Viewfinder**: Displays a dynamic rectangle representing the current screen viewport.
- **Student Density**: Visualizes student locations as high-contrast cyan dots.
- **Interactive Teleport**: Intercepts tap gestures on the mini-map to instantly pan the main seating chart to the target location.

## ⚡ BOLT Optimizations

- **Single-Pass Rendering**: Student dots and the viewfinder are drawn in a single `Canvas` pass to minimize draw calls.
- **Allocation-Free Math**: The engine uses pure functions and primitive types to ensure sub-millisecond calculation times during high-frequency gestures.
- **Normalized Space**: By working in a 0.0-1.0 normalized coordinate space, the navigator remains responsive and accurate regardless of screen size or zoom level.

## 🚧 Status: Experimental
Requires `GhostConfig.NAVIGATOR_MODE_ENABLED = true`. Integrated into the 'More' menu of the seating chart.
