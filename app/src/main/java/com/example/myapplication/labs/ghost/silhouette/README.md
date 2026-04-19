# 👻 Ghost Silhouette: Neural Spatial Continuity

## Overview
The **Ghost Silhouette** experiment explores the preservation of spatial context during UI interactions. When a student icon is dragged across the seating chart, a ghostly, pulsating AGSL silhouette remains at the student's original position until the drag is finalized.

## Technical Details
- **Shader Logic**: `GhostSilhouetteShader.kt` uses a signed distance function (SDF) to render a rectangular glow with a pulsating core.
- **State Coordination**: `SeatingChartViewModel` tracks active drag operations and exposes a list of `SilhouetteData`.
- **Performance**: Utilizes a `RuntimeShader` pool and `ShaderBrush` caching to ensure 60fps rendering without JNI overhead.

## Interaction Model
1. **Drag Start**: The `StudentDraggableIcon` captures its current coordinates and notifies the ViewModel.
2. **Layer Rendering**: `GhostSilhouetteLayer` renders a cyan placeholder at the captured coordinates.
3. **Drag End**: The silhouette fades as the student "teleports" to their new stable coordinate in the database.
