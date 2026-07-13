# 🎨 Design System & Visual Identity

This package defines the application's visual framework, implementing a modern **Material 3** design system that balances teacher productivity with the immersive aesthetic of the "Ghost Lab" suite.

## 🏛️ Architecture & Theming Strategy

The theming system is designed to be highly adaptive, supporting both system-level configurations and granular user preferences:

### 1. Material 3 & Dynamic Color
The application leverages the Material 3 `colorScheme` to provide a consistent look and feel.
- **Dynamic Color (Android 12+)**: On supported devices, the app utilizes `dynamicLightColorScheme` and `dynamicDarkColorScheme` to harmonize the UI with the user's system wallpaper, providing a personalized experience.
- **Manual Fallback**: For older devices or when dynamic color is disabled, the app falls back to a curated `DarkColorScheme` and `LightColorScheme`.

### 2. Global Animation Control (`LocalAnimationSpec`)
To ensure high performance across a wide range of hardware, the app implements a custom `CompositionLocal` named `LocalAnimationSpec`.
- **Purpose**: It provides a centralized `AnimationSpec` that is used by UI components for transitions and effects.
- **Performance Integration**: When the user enables the "No Animations" preference, `MyApplicationTheme` provides a `tween(durationMillis = 0)`. This effectively bypasses Compose's animation overhead globally, ensuring a high-speed, "instant" interface for power users or lower-end devices.

### 3. Accessible Typography & "Bold Font" Mode
Typography is managed via the `Typography` object, providing standardized scales for body text, headlines, and labels.
- **Bold Font Mode**: The theme includes a dedicated parameter to globally override font weights to `Bold`. This enhances legibility and provides a high-contrast experience for users who require extra visual emphasis.

## 🌈 The Color Palette

The application utilizes a multi-layered color strategy:

1.  **Core Material 3 Colors**: Standard primary, secondary, and tertiary colors used for buttons, containers, and surfaces.
2.  **Extended Neural Palette**: A specialized set of high-saturation colors (e.g., `GhostCyan`, `GhostMagenta`) used specifically for data-driven visualizations and "Ghost Lab" experiments. These colors are chosen for their high contrast against the standard app background.

## 📂 Key Files

-   **`Theme.kt`**: The central coordinator. Manages the orchestration of color schemes, typography, and the `LocalAnimationSpec` provider.
-   **`Color.kt`**: Defines the static color constants, including the extended neural palette.
-   **`Type.kt`**: Configures the typography scales and font families.

---
*Documentation love letter from Scribe 📜*
