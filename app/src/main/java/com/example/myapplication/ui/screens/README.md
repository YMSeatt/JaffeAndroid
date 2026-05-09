# 🖥️ Screen Orchestration & Top-Level Composables

This package contains the primary entry points for the application's user interface. These screens orchestrate state from various ViewModels and manage major UI transitions and modal flows.

## 🏛️ Core Screens

-   **`SeatingChartScreen.kt`**: The heart of the application. A high-performance, interactive classroom map that combines spatial management with advanced data visualization.
-   **`RemindersScreen.kt`**: A dedicated interface for managing teacher tasks, student-specific reminders, and system-level alerts.
-   **`QuizTemplateScreen.kt`**: The management hub for standardized assessment schemas, allowing teachers to define and reuse quiz structures.

## 👑 Seating Chart Architecture: The Multi-Layered Stack

The `SeatingChartScreen` is implemented as a sophisticated multi-layered stack. This architecture allows the application to overlay complex social and behavioral analytics on top of the physical classroom layout without compromising performance.

### 🥞 Layer Hierarchy (Bottom to Top):

1.  **Atmospheric & Environmental Layers**:
    *   **Background**: Base color and ambient light effects (`GhostHorizon`).
    *   **Grid & Framework**: `GridAndRulers` and structural overlays like `GhostAurora` or `GhostNebula`.
2.  **Social & Behavioral Analytics (Ghost Lab)**:
    *   **Relationship Maps**: `GhostLattice` (social links) and `GhostVector` (social gravity).
    *   **Temporal Heatmaps**: `GhostChronos` and `GhostTectonic` stress nodes.
    *   **Synchronicity**: `GhostPulsar` and `GhostEntanglement` ripples.
3.  **Interactive Logic Layer**:
    *   **Primary Actors**: `StudentDraggableIcon` and `FurnitureDraggableIcon`.
    *   **Selection & Lasso**: `GhostLassoLayer` for bulk management.
4.  **Refraction & Diagnostic Layers**:
    *   **Virtual Optics**: `GhostLens`, `GhostSpectra`, and `GhostIris` for per-student signature analysis.
    *   **Neural Backstage**: `GhostPhasing` and `GhostPortal`.
5.  **HUD & Control Layers**:
    *   **Tactical HUD**: `GhostHUDLayer` radar and `NeuralMapLayer`.
    *   **Command & Control**: `GhostShellLayer` (The Neural Dock) and radial menus (`GhostHubLayer`).

## 📐 Coordinate Systems & Spatial Mapping

To maintain classroom layout consistency across devices of all sizes (from phones to tablets), the seating chart utilizes a **4000x4000 Logical Canvas**.

-   **World Space**: Students and furniture are positioned using fixed logical coordinates (0-4000).
-   **Screen Space**: The UI uses pan (`offset`) and zoom (`scale`) transformations to map this logical world onto the physical device screen.
-   **Interaction Normalization**: All gestures (drag, pinch-to-zoom) are normalized by the current scale factor, ensuring that movement remains precise regardless of the current zoom level.

## ⚡ The "Fluid Interaction" Performance Model

The screens in this package utilize a **Stage 2: Transformation** pipeline (documented in the `viewmodel` package) to maintain a fluid 60fps experience.
-   **Optimistic UI**: Interactive items update their local `MutableState` coordinates immediately during drag gestures.
-   **Identity Preservation**: Existing UI objects are updated in-place rather than replaced, minimizing recomposition overhead.

---
*Documentation love letter from Scribe 📜*
