# 🧩 Molecular UI Components & Interactive Widgets

This package contains the reusable UI building blocks for the Seating Chart application. These components are designed as "Molecular UI" elements—atomic widgets combined into complex, interactive structures that drive the classroom management experience.

## 🏛️ Component Philosophy

Following the **Molecular UI** pattern, each component in this package is:
1.  **Stateless Where Possible**: Components focus on rendering and gesture detection, delegating state management to ViewModels or hoisted `MutableState` objects.
2.  **Performance-Tuned**: Built from the ground up for high-frequency interactions (60fps), adhering to the **BOLT** design principles.
3.  **Context-Aware**: Components like `GridAndRulers` and `StudentDraggableIcon` are designed to operate within a transformed coordinate space (Pan/Zoom).

## ⚡ Role in the "Fluid Interaction" Model

To achieve a buttery-smooth experience on a 4000x4000 logical canvas, these components implement several critical optimizations:

-   **Direct State Observation**: Components bind their visual properties (color, position, size) directly to Compose `MutableState` fields within the UI models (`StudentUiItem`, `FurnitureUiItem`).
-   **Optimistic Feedback**: Interactive components update local states immediately during gestures (drag/resize) to provide instant feedback, bypassing the database-to-UI update cycle temporarily.
-   **Zero-Allocation Draw Paths**: High-frequency rendering logic (e.g., inside `GridAndRulers`'s `Canvas`) hoists constants and pre-calculates dimensions to avoid object churn during pan and zoom.

## 📂 Key Components

-   **`StudentDraggableIcon`**: The primary interactive element. Manages complex gestures, Level of Detail (LOD) rendering, and "Ghost Lab" visual integrations.
-   **`FurnitureDraggableIcon`**: A simplified version of the student icon for classroom objects like desks and tables.
-   **`GridAndRulers`**: The environmental framework. Handles coordinate mapping between logical world space and physical screen pixels.
-   **`StudentIconHeightCalculator`**: A legacy utility for dynamic layout sizing (maintained for backward compatibility with older seating chart snapshots).
-   **`CustomDropdownMenu` & `MultiSelectDropdown`**: Customized Material 3 selection components optimized for teacher workflows.

---
*Documentation love letter from Scribe 📜*
