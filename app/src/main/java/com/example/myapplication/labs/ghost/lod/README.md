# 👻 Ghost LOD (Level of Detail)

## Overview
The **Ghost LOD (Level of Detail)** engine is a high-performance adaptive rendering system for the seating chart. It dynamically adjusts the complexity of student UI components based on the current viewport zoom level (`canvasScale`), ensuring a fluid 60fps experience even on complex canvases.

## Detail Levels

| Level | Scale Range | Description |
| :--- | :--- | :--- |
| **CRITICAL** | `> 1.5x` | High-fidelity mode. All experimental shaders, logs, and metadata are visible. |
| **FULL** | `0.8x - 1.5x` | Standard operation mode. Full names and recent behavior/academic logs are visible. |
| **COMPACT** | `0.4x - 0.8x` | Macroscopic view. Last names and detailed logs are hidden to reduce visual noise. |
| **MINIMAL** | `< 0.4x` | Bird's eye view. Only student initials are rendered at an increased scale for readability. |

## Implementation Details
- **Engine**: `GhostLODEngine.kt` manages the mapping logic and DetailLevel heuristics.
- **Integration**: `StudentDraggableIcon.kt` observes the `canvasScale` and conditionally renders sub-components (Logs, Names) based on the calculated LOD.
- **Optimization**: By shedding text layout and rendering tasks at lower zoom levels, the engine significantly reduces CPU/GPU overhead during rapid pan and zoom gestures.

## User Control
LOD can be toggled in the **Ghost Lab Preferences** menu under the **Visual Engine** section. When disabled, the system defaults to `FULL` detail regardless of zoom level.
