# 👻 Ghost Lab: Experimental Features

This directory contains experimental, future-facing features for the Seating Chart & Behavior Logger. These "Ghost" features explore the intersection of data visualization, automated layout optimization, on-device AI synthesis, and natural user interfaces.

## 🛠️ Components

### 1. Cognitive Proximity Engine (`GhostCognitiveEngine.kt`)
An automated layout optimizer that uses a **force-directed graph algorithm**.
- **Physics-Based Layout**: Treats students as physical bodies.
    - **Repulsion**: Inverse-square law repulsion ensures even distribution.
    - **Social Distance**: Students with negative logs repel others 2.5x more strongly.
    - **Attraction**: Linear spring-like attraction keeps student groups clustered.
- **Performance**: Optimized with primitive arrays and pre-calculated lookups for $O(N^2)$ simulation efficiency.
- **Integration**: Accessed via `SeatingChartViewModel.runCognitiveOptimization()`.

### 2. Tactical HUD & Neural Map (`GhostHUDLayer.kt`, `NeuralMap.kt`, `GhostShader.kt`)
Advanced visualization layers using **AGSL Shaders** (Android Graphics Shading Language).
- **Tactical Radar**: A 360-degree radar overlay that maps 2D seating chart coordinates to polar space relative to a virtual observer. Integrates with device rotation sensors for physical orientation awareness.
- **Neural Lines**: Visualize group connections using high-performance line shaders.
- **Cognitive Auras**: Pulsating red glows around students requiring attention, rendered with procedural noise shaders.

### 3. Ghost Chronos (`GhostChronosLayer.kt`, `GhostChronosEngine.kt`, `GhostChronosShader.kt`)
A temporal behavioral heatmap engine.
- **Spatio-temporal Analysis**: Divides the classroom into a grid and calculates behavioral "intensity" over a time window.
- **Dynamic Heatmap**: Renders a glowing floor layer using an AGSL shader. Positive behavior appears green; negative behavior appears red.
- **Transformation-aware**: The heatmap aligns with the seating chart's zoom and pan states.

### 4. Ghost Synapse (`synapse/GhostSynapseEngine.kt`)
A Proof of Concept for **On-Device AI Narrative Synthesis** (simulating Gemini Nano via AICore).
- **Generative Insights**: Transforms raw behavioral and academic logs into cohesive high-fidelity "Neural Narratives".
- **Student Synthesis**: Categorizes student state into stable signatures, behavioral turbulence, or peak cognitive performance.
- **Background Visuals**: Uses a reactive `NEURAL_FLOW` AGSL shader to visualize the "thought process" during synthesis.

### 5. Ghost Phantasm (`GhostPhantasmLayer.kt`, `GhostPhantasmShader.kt`)
A presence-based visualization layer using **Meta-balls**.
- **Classroom Presence**: Renders students as fluid, glowing blobs that merge and split based on their proximity and behavioral "agitation".
- **Agitation Dynamics**: Scaling factors increase blob size and intensity for students with high log frequency (especially negative logs).
- **Privacy Glitch**: Incorporates a "Privacy Glitch" mode triggered when screen recording is detected (using Android 15 APIs).

### 6. Ghost Portal (`GhostPortalLayer.kt`, `GhostPortalShader.kt`)
A visual experiment for **Inter-app Data Teleportation**.
- **Wormhole Effect**: Renders a swirling AGSL wormhole shader during Drag & Drop operations.
- **Android 15 Integration**: Leverages `Modifier.dragAndDropSource` and `Modifier.dragAndDropTarget` for seamless data transfer between compatible apps.

### 7. Ghost Echo (`GhostEchoLayer.kt`, `GhostEchoEngine.kt`)
A real-time **Auditory Atmosphere Monitor**.
- **Acoustic Turbulence**: Visualizes classroom noise levels as ambient turbulence using a procedural noise shader.
- **Reactive Engine**: Normalizes microphone amplitude data to drive shader uniforms.

### 8. Ghost Hologram (`GhostHologramLayer.kt`, `GhostHologramEngine.kt`)
A **3D Parallax & Holographic Overlay** for the seating chart.
- **Motion-Sensing**: Uses the device's rotation vector sensor to apply 3D `graphicsLayer` transformations (pitch and roll).
- **Holographic Glass**: Overlays a futuristic "scanning line" and flicker effect using an AGSL shader.

### 9. Ghost Oracle (`GhostOracle.kt`)
A predictive analysis engine.
- **Social Friction**: Predicts tension when high-risk students are seated together.
- **Engagement Drop**: Flags students who haven't received positive feedback in over 7 days.
- **Confidence Scores**: Each "prophecy" includes a confidence metric used by the HUD to scale visual intensity.

### 10. Ghost Blueprint Engine (`GhostBlueprintEngine.kt`)
Generates futuristic **SVG Classroom Blueprints**.
- **SVG Generation**: Produces stylized 1200x800 vector representations of the classroom layout.
- **Coordinate Mapping**: Implements a scaling formula `(pos / 4) + offset` to map Android's 4000x4000 logical canvas into the blueprint frame.
- **Aesthetic**: Features cyan-glow students and dashed-line furniture items.

### 11. Ghost Voice Assistant (`GhostVoiceAssistant.kt` & `GhostVisualizer.kt`)
A hands-free interface for classroom management.
- **Command Parsing**: Translates speech (e.g., "Log positive participation for John Doe") into database actions.
- **Neural Waveform**: A reactive shader-based visualizer that responds to voice amplitude in real-time.

### 12. Insight Engine (`GhostInsightEngine.kt` & `GhostInsightDialog.kt`)
A data analysis tool that generates behavioral and academic "insights."
- Categorizes students as **Peak Performers**, **Steady Progress**, or requiring **Attention/Academic Support** based on aggregated log data.

### 13. Ghost Lattice (`lattice/GhostLatticeLayer.kt`)
A social dynamics visualizer that maps student relationships as a glowing **Neural Network**.
- **Social Graph Inference**: Automatically builds a lattice of connections (Collaboration, Friction, Neutral) based on student proximity and behavioral history.
- **Neural Shaders**: Renders connections using glowing AGSL line shaders that pulse with "social energy."
- **Interactive Dynamics**: Connections dynamically stretch and pulse as students are moved on the chart.

### 14. Ghost Vector (`vector/GhostVectorLayer.kt`)
A physics-based visualization of **Social Gravity**.
- **Net Force Calculation**: Sums the attraction (Collaboration) and repulsion (Friction) forces acting on each student to determine their net "Social Vector."
- **Directional Needles**: Renders glowing AGSL-powered needles that point in the direction of the student's social momentum.
- **Tension Detection**: Color shifts from Cyan to Magenta for students under high social tension or conflict.
- **Python Analysis**: Integrated with `Python/ghost_vector_analysis.py` for calculating classroom-wide cohesion metrics.

### 15. Ghost Spectra (`GhostSpectraLayer.kt`, `GhostSpectraShader.kt`, `GhostSpectraEngine.kt`)
A data refraction layer using **Chromatic Dispersion**.
- **Spectroscopic Visualization**: "Breaks" the UI into its constituent data components (Behavior, Participation, Academic) using an AGSL dispersive prism shader.
- **Interactive Refraction**: Users drag a virtual prism across the screen to reveal hidden data "spectral signatures" under the student icons.
- **Neural Signature Analysis**: Integrated with `Python/ghost_spectra_analyzer.py` to process classroom signatures and identify students in "Infrared" (at-risk) or "Ultraviolet" (high engagement) states.

### 16. Ghost Singularity (`GhostSingularityLayer.kt`, `GhostSingularityShader.kt`, `GhostSingularityEngine.kt`)
A high-intensity **Data Sink** visualization for destructive actions.
- **Gravitational Lensing**: Uses an AGSL shader to distort UI space around a central singularity, simulating the curvature of space-time.
- **Tactile Collapse**: Implements multi-stage haptic feedback using `VibrationEffect.Composition` (PRIMITIVE_LOW_TICK -> PRIMITIVE_THUD -> PRIMITIVE_QUICK_FALL) as students approach the event horizon.
- **Accretion Disk**: Procedurally generates a glowing, rotating disk of "data energy" around the singularity.

### 17. Ghost Aurora (`GhostAuroraLayer.kt`, `GhostAuroraShader.kt`, `GhostAuroraEngine.kt`)
A "Classroom Climate" visualization using **Procedural Auroras**.
- **Atmospheric Visualization**: Renders a beautiful, flowing aurora in the background that reflects the collective mood of the classroom.
- **Data-Driven Colors**: Aurora colors shift based on the balance of positive (Cyan), negative (Red), and academic (Purple) logs.
- **Dynamic Intensity**: The brightness and flow speed of the aurora scale with recent log frequency.

### 18. Ghost Phasing (`phasing/GhostPhasingLayer.kt`, `phasing/GhostPhasingShader.kt`, `phasing/GhostPhasingEngine.kt`)
A multi-layered **Neural Backstage** visualization.
- **Neural Transition**: Uses a glitchy AGSL transition shader (Chromatic Aberration + Scanlines) to "phase" the classroom UI into a hidden data layer.
- **Backstage Void**: Renders a deep-space "Neural Void" background featuring floating "Neural Seeds" and real-time data particle streams.
- **Tactile Sync**: Synchronizes visual "glitches" with complex haptic feedback (VibrationEffect.Composition) to simulate the sensation of shifting through UI layers.

### 19. Ghost Lens (`GhostLensLayer.kt`, `GhostLensShader.kt`, `GhostLensEngine.kt`)
A predictive spatiotemporal **Refraction Layer**.
- **Predictive Magnification**: Uses a draggable virtual lens that "refracts" the seating chart to reveal predicted student states and prophecies.
- **AGSL Refraction**: Implements spherical distortion and chromatic aberration within the lens field using a high-performance shader.
- **AI Integration**: Bridges to `GhostOracle` to display real-time on-device AI prophecies for students under the lens.

### 20. Ghost Iris (`GhostIrisLayer.kt`, `GhostIrisShader.kt`, `GhostIrisEngine.kt`)
A personalized **Neural Signature** visualization.
- **Procedural Identity**: Generates a unique, animated fractal iris for each student, seeded by their student ID.
- **Data-Driven Evolution**: The iris's colors and pattern complexity shift dynamically based on the student's behavioral balance and academic performance.
- **AGSL Fractal Noise**: Uses a high-performance Voronoi/Fractal noise shader to create organic, biological patterns.

### 21. Ghost Warp (`warp/GhostWarpLayer.kt`, `warp/GhostWarpShader.kt`, `warp/GhostWarpEngine.kt`)
A Proof of Concept for **Neural Spacetime Dilation**. It visualizes the "Spacetime Curvature" of the classroom based on behavioral intensity. High-activity students create "Gravity Wells" that distort the background grid.

### 22. Ghost Nebula (`GhostNebulaLayer.kt`, `GhostNebulaShader.kt`, `GhostNebulaEngine.kt`)
A Proof of Concept for **Atmospheric Density Visualization**. It visualizes the "Social Atmosphere" as a dynamic, gaseous nebula. High-activity areas create "Stellar Nurseries" (bright, dense gas), while quiet areas remain "Cold Voids".

### 23. Ghost Osmosis (`osmosis/GhostOsmosisLayer.kt`, `osmosis/GhostOsmosisShader.kt`, `osmosis/GhostOsmosisEngine.kt`)
A Proof of Concept for **Knowledge Diffusion & Behavioral Concentration**. It visualizes the classroom as a fluid field where academic strength and behavioral patterns "diffuse" between students in close proximity.
- **Diffusion Gradients**: Calculates "Knowledge Potential" and "Behavioral Concentration" to drive a glowing, fluid-like diffusion field.
- **Osmotic Balance**: Integrated with `Python/ghost_osmosis_analyzer.py` to identify equilibrium states and high-gradient zones.

### 24. Ghost Entanglement (`entanglement/GhostEntanglementLayer.kt`, `entanglement/GhostEntanglementShader.kt`, `entanglement/GhostEntanglementEngine.kt`)
A Proof of Concept for **Quantum Social Synchronicity**. It visualizes "Quantum Links" between students who exhibit high synchronicity (timing, performance, proximity).
- **Quantum Coherence**: Calculates "Coherence" scores between students to drive visual ripples and glowing connections.
- **Spooky Action**: Visualizes how a behavioral event in one student might "ripple" to entangled partners.
- **Python Bridge**: Integrated with `Python/ghost_entanglement.py` for mathematical parity and entanglement matrix analysis.

### 25. Ghost Ion (`ion/GhostIonLayer.kt`, `ion/GhostIonShader.kt`, `ion/GhostIonEngine.kt`)
A Proof of Concept for **Neural Ionization & Electrostatic Visualization**. It maps classroom "energy" to a glowing, ionized gas layer.
- **Ionic Potential**: Calculates charge based on behavioral balance and density based on log frequency and hardware temperature.
- **Hardware Synthesis**: Integrates real-time device battery temperature as a metaphor for classroom "heat".
- **AGSL Discharge**: Renders ionized clouds and procedural "electrostatic discharge" flickers using a high-performance shader.
- **Python Bridge**: Integrated with `Python/ghost_ion_analyzer.py` for logical parity and charge distribution analysis.

## 🚧 Status: Experimental
These features require `GhostConfig.GHOST_MODE_ENABLED = true` and target **API 33+** (for `RuntimeShader` support), with some features requiring **API 35+** (Android 15). They are intended for research and development and may be subject to rapid changes.

---
*Documentation love letter from Scribe 📜*
