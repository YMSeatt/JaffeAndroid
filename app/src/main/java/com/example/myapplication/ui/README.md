# 🎨 UI & User Experience Architecture

This package contains the presentation layer of the Seating Chart application, built entirely with **Jetpack Compose**. It implements a high-performance, reactive UI that balances complex spatial interactions with real-time data visualization.

## 📂 Package Structure

The UI layer is organized into functional sub-packages:

- **`screens/`**: The top-level entry points for the application (e.g., `SeatingChartScreen`, `RemindersScreen`). These composables orchestrate the state from ViewModels and manage major UI transitions.
- **`components/`**: Reusable atomic and molecular UI widgets. Includes core interactive items like `StudentDraggableIcon` and `FurnitureDraggableIcon`, as well as environmental elements like `GridAndRulers`.
- **`dialogs/`**: All modal interactions. This app follows a "Dialog-Heavy" design for data entry (adding students, logging behaviors) to keep the main seating chart focused on spatial management.
- **`model/`**: UI-specific data representations. The `StudentUiItem` and `FurnitureUiItem` classes are optimized for Compose observation, using `MutableState` fields to enable fine-grained recomposition.
- **`settings/`**: A comprehensive suite of configuration screens, organized by tabs (General, Display, Data, Email, etc.), allowing teachers to customize every aspect of the experience.
- **`theme/`**: The visual foundation of the app, defining the Color palette (including dynamic theme support), Typography, and Shapes.

## ⚡ The "Fluid Interaction" Model

To maintain a buttery-smooth 60fps experience—even when dragging dozens of items or rendering complex AGSL shaders—the UI utilizes a sophisticated performance model:

1.  **Optimistic UI Updates**: During interactive gestures (like dragging), components directly update the `MutableState` properties of their underlying UI items (e.g., `studentUiItem.xPosition`). This provides immediate visual feedback by bypassing the standard database-to-UI update loop.
2.  **Object Identity Preservation**: The `SeatingChartViewModel` maintains a persistent cache of UI items. When data changes in the database, the repository updates the *existing* fields of these cached items rather than creating new objects. This allows Compose to perform highly efficient "diff-and-patch" updates.
3.  **Fine-Grained Recomposition**: By binding UI elements to individual `MutableState` fields, the system ensures that only the specific part of the UI that changed (e.g., just the font color or just the coordinates) is re-rendered.

## 📐 Coordinate Systems & Spatial Mapping

The seating chart operates on a **4000x4000 Logical Canvas**.

-   **World Space**: Students and furniture are positioned using fixed logical coordinates (0-4000). This ensures that classroom layouts remain consistent regardless of the device's screen size or aspect ratio.
-   **Screen Space**: The `SeatingChartContent` uses pan (`offset`) and zoom (`scale`) transformations to map this logical world onto the physical device screen.
-   **Interaction Normalization**: Gestures (drag, pinch-to-zoom) are normalized by the current scale factor to ensure that "10 pixels of movement" on a zoomed-in screen correctly translates to the intended logical distance in world space.

## 👻 Ghost Lab & Layered Composition

The seating chart is implemented as a **Multi-Layered Stack**, allowing for the seamless integration of "Ghost Lab" experimental features:

1.  **Atmospheric Layers**: Background colors, `GhostHorizon` (ambient light/pressure).
2.  **Environmental Layers**: `GridAndRulers`, `GhostAurora`, `GhostNebula`.
3.  **Social/Behavioral Layers**: `GhostChronos` (heatmaps), `GhostLattice` (social links), `GhostVector` (social gravity).
4.  **Interactive Layer**: The primary `StudentDraggableIcon` and `FurnitureDraggableIcon` items.
5.  **Refraction/Portal Layers**: `GhostLens`, `GhostPortal`.
6.  **HUD & Overlay Layers**: `GhostHUD`, `NeuralMap`, and Voice Assistant visualizers.

Most experimental layers leverage **AGSL (Android Graphics Shading Language)** for GPU-accelerated procedural visuals, integrated directly into the Compose `Canvas` hierarchy.

---
*Documentation love letter from Scribe 📜*
