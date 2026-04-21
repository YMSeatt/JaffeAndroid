# 🦋 Ghost Morph: Shared Element Neural Transitions

Ghost Morph is an experimental visualization engine within Ghost Lab that focuses on **seamless UI continuity**. It leverages Jetpack Compose's `SharedTransitionLayout` to provide high-fidelity "morph" transitions between student icons and their full-screen Neural Dossiers.

## 🌀 Neural Fluid Aesthetic

The dossiers are rendered over a dynamic **Neural Fluid** background powered by an AGSL shader (`GhostMorphShader.kt`).
- **Organic Flow**: Uses Simplex noise and plasma-inspired algorithms to create a flowing, biological feel.
- **Data-Driven Color**: The background shifts between Deep Violet and Cyber Cyan, reflecting the classroom's "Neural Flux".
- **Luminosity Pulse**: Subtle highlights pulse through the fluid, synchronized with the student's cognitive resonance.

## 💎 Shared Element Implementation

Ghost Morph utilizes the modern `SharedTransitionLayout` and `AnimatedContent` to synchronize:
- **Shared Bounds**: The student icon's bounds (simulated by a 60dp circle) seamlessly expand and "morph" into the full dossier card using `Modifier.sharedBounds`.
- **Content Continuity**: Essential student metadata transitions logically from the seating chart into the report header through coordinated state management.

## ⚡ Bolt Optimizations

- **Single-Pass Shader**: The Neural Fluid shader is optimized to avoid multiple noise octaves on the UI thread, ensuring a stable 60fps even on mid-range devices.
- **Activity Decoupling**: Launching the dossier in a separate `GhostMorphActivity` prevents main seating chart recomposition during transition, maximizing performance.
- **Intent Data Minimization**: Follows the **ID-Only Protocol**, passing only the student ID and display name to the activity to ensure privacy and low-latency startup.

## 🔄 Logic Parity

Maintains logical parity with the **Ghost Link** engine, utilizing the same stochastic seeding for deterministic "AI" analysis results.

---
*Documentation love letter from Scribe 📜*
