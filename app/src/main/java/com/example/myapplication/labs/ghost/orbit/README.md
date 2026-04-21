# 🌌 Ghost Orbit: The Classroom Galaxy

The **Ghost Orbit** experiment transforms the traditional seating chart into a dynamic, physics-driven "Classroom Galaxy." It models student engagement and behavioral stability as orbital mechanics, visualizing the social rhythm of the classroom as a procedural solar system.

## 🪐 The "Classroom Galaxy" Model

The engine maps student data to celestial physics parameters to create a living map of classroom dynamics.

### ☄️ Orbital Physics
- **Angular Velocity (Speed)**: Driven by **Engagement**. The frequency of recent logs (within a 60-minute window) determines how fast a student "planet" orbits. High-activity students exhibit higher kinetic energy.
- **Orbital Radius**: Driven by **Stability**. The ratio of positive to total logs determines the student's distance from their "Social Sun." Highly stable students maintain a tight, consistent orbit, while students with behavioral turbulence drift into wider, more chaotic paths.
- **Social Suns (Attractors)**: Students with significant positive history (5+ logs) are identified as "Social Suns." They act as gravitational centers, pulling nearby students into their orbital fields.

---

## 🎨 Visual Mapping: AGSL Galactic Rendering

The `GhostOrbitLayer` utilizes **AGSL (Android Graphics Shading Language)** to render a high-fidelity cosmic environment.

### 🌌 Neural Nebula
A procedural background shader that visualizes the "System Energy" of the entire classroom.
- The nebula's rotation speed and color intensity scale with the aggregate frequency of behavior logs across all students.
- **Cyan/Blue**: Represents a stable, productive atmosphere.
- **Magenta/Purple**: Indicates high energy or behavioral flux.

### 🕳️ Gravity Wells (Interaction)
The layer supports interactive "Gravity Wells." When a teacher taps the screen, a temporary gravitational distortion is rendered using a specialized shader, simulating a localized lensing effect that visually "warps" the social fabric.

---

## ⚡ BOLT Optimization

To ensure a fluid 60fps experience on mobile hardware:
1. **On-the-Fly Kinematics**: Orbital positions are calculated directly in the `Canvas` draw pass using manual trigonometry (`cos`/`sin`), eliminating the need for per-frame state updates or object allocations.
2. **Pre-Indexed Parameters**: Heavy operations like "Social Sun" identification and grouping are hoisted into a `remember` block and only re-calculated when the underlying student data or logs change.
3. **Allocation-Free Drawing**: Replaces expensive functional operators (`map`, `filter`) with manual index-based loops to minimize iterator churn and garbage collection pressure.
4. **Squared Distance**: Uses squared distance comparisons for nearest-sun detection to avoid expensive `sqrt()` operations during parameter calculation.

---
*Documentation love letter from Scribe 📜*
