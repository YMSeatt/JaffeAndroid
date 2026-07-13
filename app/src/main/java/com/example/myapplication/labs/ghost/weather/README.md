# ☁️ Ghost Weather: Dynamic Particle Weather Systems

The **Ghost Weather** experiment implements a dynamic, data-driven atmospheric system for the classroom. It translates behavioral and academic data into a living weather ecosystem.

## 🌪️ The Metaphor: Neural Meteorology
Classroom dynamics are modeled as a meteorological system where every interaction affects the "Classroom Climate":

- **Neural Precipitation (Intensity)**: Driven by the frequency of recent behavioral logs. A busy classroom creates heavy rain or snow, while a quiet one remains clear.
- **Social Wind (Direction/Force)**: Driven by the balance of positive vs. negative logs.
    - **Eastward Wind**: Strong positive reinforcement creates a "tailwind" of progress.
    - **Westward Wind**: Negative behavioral clusters create "headwinds" of resistance.
- **Academic Lightning (Impact Events)**: High-impact academic events (like logging a quiz score) trigger procedural lightning strikes across the canvas, symbolizing "Neural Breakthroughs."
- **Social Temperature (Weather Mode)**:
    - **Snow (Peaceful)**: Dominates when the room is highly positive and calm.
    - **Rain (Standard)**: The default state for balanced activity.
    - **Neural Storm (Turbulent)**: Triggered by high ratios of negative behavioral events.

## 🎨 AGSL Shader Implementation (`GhostWeatherShader.kt`)
The atmospheric effects are powered by AGSL:
- **Atmospheric Density**: A base shader layer that darkens the canvas and adds a "stormy" tint based on log intensity.
- **Procedural Lightning**: A branch lightning effect using fractal noise and distance-based glow, triggered by academic updates.

## ⚡ BOLT Performance Optimizations
- **Particle Pooling**: Manages a fixed pool of 200 particles (Raindrops/Snowflakes) using primitive `FloatArray` buffers to eliminate GC churn.
- **Zero-Allocation Physics**: The `GhostWeatherEngine` performs all calculations using primitive types and single-pass log traversal.
- **Hoisted Rendering**: All coordinate transformations (Logical -> Screen) are performed during the `Canvas` draw phase using pre-calculated scale/offset uniforms.

---
*Documentation love letter from Scribe 📜*
