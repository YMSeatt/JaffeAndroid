# Ghost UI Utilities 👻

This directory contains reusable, high-performance UI components and modifiers designed for experimental "Ghost Lab" features.

## Components & Modifiers

### 1. `Modifier.ghostShimmer()`
A reusable "Neural Shimmer" modifier for skeleton loaders and atmospheric UI highlights.
- **BOLT ⚡ Optimization**: Uses `drawBehind` to minimize modifier object allocations in the composition tree.
- **Dynamic Translation**: The shimmer gradient is automatically calculated based on the composable's size, ensuring a consistent look across different UI elements.
- **Usage**:
  ```kotlin
  Box(modifier = Modifier.size(100.dp).ghostShimmer())
  ```

### 2. `GhostGlassmorphicSurface`
A futuristic "Frosted Glass" container for experimental HUDs and preference screens.
- **BOLT ⚡ Optimization**: Applies `Modifier.blur()` strictly to a background layer, keeping child content (text, icons) perfectly sharp.
- **Native Fallback**: Automatically degrades to a semi-transparent background on devices below Android 12 (API 31) where native hardware-accelerated blur is unavailable.
- **Usage**:
  ```kotlin
  GhostGlassmorphicSurface(glassmorphismEnabled = true) {
      Text("Neural Stats")
  }
  ```

### 3. `GhostHapticManager`
A high-fidelity haptic feedback wrapper for tactile UI awareness.
- **Android 15 Primitives**: Leverages `PRIMITIVE_SPIN`, `PRIMITIVE_THICK_TICK`, and other API 35+ somatic effects.
- **Somatic Patterns**:
  - `SUCCESS`: Double-check confirmation.
  - `ERROR`: A physical "thud" obstruction.
  - `NEURAL_THINKING`: Rotational momentum buildup.
  - `UI_CLICK`: Clean, sharp interaction.
  - `SPARK_POP`: Light, sudden energy burst.
- **Usage**:
  ```kotlin
  val hapticManager = remember { GhostHapticManager(context) }
  hapticManager.perform(GhostHapticManager.Pattern.SUCCESS)
  ```

## Performance & Native Philosophy

All Ghost UI utilities follow two core principles:
1. **BOLT ⚡ (Performance First)**: Avoid expensive allocations and recompositions in the hot path (e.g., draw loops, animations).
2. **NATIVE 📱 (Platform Integrity)**: Use the latest native Android APIs (Compose, AGSL, DataStore, Android 15 Haptics) with robust fallbacks for older OS versions.
