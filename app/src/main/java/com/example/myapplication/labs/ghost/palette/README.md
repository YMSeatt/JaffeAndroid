# 🎨 Ghost Palette: Neural Color Harmony Engine

**Experiment ID:** GHOST-2028-06-25
**Status:** PROTOTYPE COMPLETE

## 🌟 The Vision
In 2027, the classroom atmosphere isn't static. "Ghost Palette" allows teachers to tune the visual frequency of their classroom management environment. By using mathematical color harmonies (Complementary, Triadic), the app's theme evolves from a static set of colors into a dynamic, "living" interface that reflects the teacher's desired energy.

## 🛠️ The Tech
- **AGSL Neural Field:** `GhostPaletteShader.kt` implements a GPU-accelerated interactive color field that responds to touch with neural ripples.
- **Harmony Engine:** `GhostPaletteEngine.kt` provides the algebraic foundations for Complementary and Triadic color relationships using HSV space.
- **Jetpack DataStore:** Integrated with the experimental `GhostPreferencesStore` for real-time, coroutine-safe theme persistence.
- **Haptic Feedback:** Uses the `GhostHapticManager` to provide tactile "notches" as the user explores the color space.

## 🔦 The Discovery
- **Mathematical Aesthetic:** We found that limiting the color picker to mathematically valid harmonies ensures that the "Ghost" aesthetic remains cohesive even when customized.
- **GPU Interaction:** Real-time AGSL uniforms allow for 60fps color exploration on API 33+ devices, making the "selection" process feel like an immersive experience rather than a menu setting.

## 💡 The "What if?"
*What if the Ghost Palette could adapt automatically based on the classroom's 'Aurora' — shifting to high-contrast triadic harmonies when student agitation is high to improve glanceability?*
