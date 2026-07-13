# 📡 Ghost Radar: Spatiotemporal Behavioral Resonance

Ghost Radar is an experimental visualization tool that provides teachers with a localized, real-time "weather map" of classroom behavioral activity. It identifies hotspots of engagement or disruption by analyzing the spatial and temporal density of behavior logs.

## 🏛️ Architectural Components

The Radar system is composed of three primary elements:

1.  **`GhostRadarEngine` (The Brain)**: A high-performance logic engine that calculates "Resonance" values for specific coordinates. It performs $O(N \cdot L)$ analysis (optimized for recent events) to determine the behavioral intensity of a localized area.
2.  **`GhostRadarShader` (The Visuals)**: A sophisticated AGSL (Android Graphics Shading Language) program that renders the radar interface. It features a rotating sweep, concentric distance rings, and procedural "interference" driven by data intensity.
3.  **`GhostRadarLayer` (The Interface)**: A Jetpack Compose overlay that orchestrates the shader and positions the radar relative to the seating chart's logical coordinate system.

## 📐 Mathematical Models & Logic

### 1. Resonance Calculation
The core metric, **Resonance**, represents the "echo" of behavioral events in a specific spatial zone. It is calculated using two primary decay factors:

-   **Linear Distance Decay**: Influence decreases linearly from the center of the radar out to the `RADAR_RADIUS` (500 units).
    -   Formula: `1.0 - (distance / RADAR_RADIUS)`
-   **Linear Time Decay**: More recent events contribute more significantly to the resonance than older events within the sliding time window (default 24h).
    -   Formula: `(eventTimestamp - cutoff) / windowDuration`

### 2. Valence Weighting
Different behavior types contribute differently to the radar's intensity:
-   **Negative**: 0.4f (High Turbulence)
-   **Positive**: 0.2f (Active Engagement)
-   **Neutral**: 0.1f (Baseline Presence)

## ⚡ BOLT Performance Optimizations

To ensure the radar remains responsive during high-frequency UI updates (such as panning across a dense classroom):

-   **O(Recent) Traversal**: The engine assumes behavior logs are sorted in descending chronological order. It utilizes an early `break` once it encounters events outside the active time window, significantly reducing the search space.
-   **Hoisted Shader State**: The `GhostRadarLayer` utilizes `remember` and `ShaderBrush` to avoid redundant object allocations, ensuring 60fps rendering on mid-range hardware.

---
*Documentation love letter from Scribe 📜*
