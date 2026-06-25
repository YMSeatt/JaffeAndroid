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

### 26. Ghost Catalyst (`catalyst/GhostCatalystLayer.kt`, `catalyst/GhostCatalystShader.kt`, `catalyst/GhostCatalystEngine.kt`)
A Proof of Concept for **Behavioral Chain Reaction Mapping**. It treats the classroom as a chemical system where student actions act as catalysts for subsequent events.
- **Reaction Kinetics**: Detects "Catalytic Events" using a spatio-temporal sliding window (Radius 800, Window 300s).
- **Social Bonds**: Visualizes "Ionic Bonds" (glowing AGSL lines) between catalysts and reactants.
- **Macroscopic Analysis**: Integrated with `Python/ghost_catalyst_analysis.py` to calculate classroom Reaction Rates and Activation Energy.

### 27. Ghost Flora (`flora/GhostFloraLayer.kt`, `flora/GhostFloraShader.kt`, `flora/GhostFloraEngine.kt`)
A Proof of Concept for **Neural Botanical Visualization**. It implements a digital classroom ecosystem where students are represented by data-driven flowers.
- **Growth Dynamics**: Flower scale and petal length scale with academic performance (Quiz + Homework).
- **Vitality Gradient**: Colors shift from Cyan (Positive) to Magenta (Negative) based on behavioral balance.
- **Complexity**: Petal density and noise distortion reflect the frequency of classroom activity.
- **Python Parity**: Integrated with `Python/ghost_flora_analysis.py` for logical parity and ecosystem-wide climate analysis.

### 28. Ghost Tectonics (`tectonics/GhostTectonicEngine.kt`, `GhostTectonicLayer.kt`, `GhostTectonicShader.kt`)
A social stability visualization using a **Geological Metaphor**.
- **Social Stress**: Calculates localized stress based on student proximity and negative behavioral history.
- **Fault Lines**: Identifies "Fault Lines" between high-stress nodes where behavioral outbursts are statistically more likely.
- **Seismic Analysis**: Generates macroscopic reports (Risk Levels: Stable to Critical) with logic parity in `Python/ghost_tectonics_analysis.py`.
- **AGSL Cracks**: Renders procedural "Seismic Cracks" in the classroom floor using a displacement shader.

### 29. Ghost Horizon (`GhostHorizonEngine.kt`, `GhostHorizonLayer.kt`, `GhostHorizonShader.kt`)
A **Context-Aware Atmospheric Engine** that adapts the UI to the physical environment.
- **Sensor Fusion**: Integrates ambient light (Lux) and barometric pressure (hPa) sensors to drive background aesthetics.
- **Atmospheric Factor**: Dynamically shifts the UI color palette from "Amber/Ghost" (Dark) to "Solarized Cyan" (Bright) based on classroom lighting.
- **Neural Horizon**: Renders a reactive background horizon that tilts and shifts with environmental pressure changes.

### 30. Ghost Pulsar (`pulsar/GhostPulsarEngine.kt`, `pulsar/GhostPulsarLayer.kt`, `pulsar/GhostPulsarShader.kt`)
Visualizes **Harmonic Synchronicity** and classroom rhythms.
- **Rhythm Analysis**: Calculates a "Frequency" (logs per minute) and "Phase" for each student to determine their individual "Classroom Rhythm".
- **Harmonic Bonds**: Detects synchronicity between students with matching behavioral rhythms (Logic parity with `Python/ghost_pulsar_analyzer.py`).
- **Interference Patterns**: Renders glowing AGSL pulses that exhibit visual interference when synchronized students are near each other.

### 31. Ghost Magnetar (`magnetar/GhostMagnetarEngine.kt`, `magnetar/GhostMagnetarLayer.kt`, `magnetar/GhostMagnetarShader.kt`)
Models classroom dynamics as a **Social Magnetic Field**.
- **Magnetic Dipoles**: Assigns polarity to students (Positive = North, Negative = South). Negative behaviors exert stronger "Social Gravity" (1.5x weight).
- **Sensor Integration**: Skews the virtual magnetic field based on the device's physical magnetometer heading.
- **Field Analysis**: Calculates quadrant-based field intensities ($1/r^2$ model) and generates Markdown reports aligned with `Python/ghost_magnetar_analysis.py`.

### 32. Ghost Cortex (`cortex/GhostCortexEngine.kt`, `cortex/GhostCortexLayer.kt`, `cortex/GhostCortexShader.kt`)
A **Somatic Exploration Engine** that translates data into physical sensation.
- **Neural Tension**: Calculates a student's tension score by combining academic entropy and behavioral turbulence.
- **Neural Haptics**: Utilizes Android 15's `VibrationEffect.Composition` to trigger nuanced haptic pulses (Ticks, Clicks, Thuds, Spins) that match the student's tension level.
- **Tactile Mapping**: Allows teachers to "feel" the classroom's data landscape through the device's actuators.

### 33. Ghost Quasar (`quasar/GhostQuasarEngine.kt`, `quasar/GhostQuasarLayer.kt`, `quasar/GhostQuasarShader.kt`)
Identifies **High-Energy Focal Points** in the classroom ecosystem.
- **Luminosity Mapping**: Flags students as "Quasars" based on rapid bursts of behavioral or academic activity.
- **Accretion Disks**: Renders rotating, glowing rings around Quasar nodes using high-performance AGSL shaders.
- **BOLT Optimization**: Uses a single-pass O(Recent) analysis loop with early-exits for real-time tracking of high-energy shifts.

### 34. Ghost Helix (`helix/GhostHelixEngine.kt`, `helix/GhostHelixLayer.kt`, `helix/GhostHelixShader.kt`)
Visualizes student trajectories as **Neural DNA**.
- **Genetic Sequencing**: Transforms behavior and academic logs into a unique 4-base sequence (Adenine, Thymine, Cytosine, Guanine).
- **Genetic Trajectory**: Calculates a stability score and trajectory vector (Logic parity with `Python/ghost_helix_analysis.py`).
- **3D Double Helix**: Renders a pulsing, rotating DNA helix around student icons, where the twist rate scales with academic momentum.

### 35. Ghost Supernova (`supernova/GhostSupernovaEngine.kt`, `supernova/GhostSupernovaLayer.kt`, `supernova/GhostSupernovaShader.kt`)
A visual metaphor for **Classroom Criticality**.
- **Core Pressure**: Tracks cumulative classroom friction. Reaching critical mass triggers a 4-stage "Supernova" event (Contraction -> Explosion -> Nebula -> Idle).
- **Criticality Index**: Calculates classroom-wide explosion risk based on negative log density (Logic parity with `Python/ghost_supernova_analysis.py`).
- **AGSL Nebula**: Renders a beautiful, expanding gas cloud shader that "resets" the visual energy of the seating chart.

### 36. Ghost Vortex (`vortex/GhostVortexEngine.kt`, `vortex/GhostVortexLayer.kt`, `vortex/GhostVortexShader.kt`)
Detects **Rotational Social Momentum**.
- **Social Whirlpools**: Identifies clusters of high-activity students that create localized social vortices.
- **Angular Momentum**: Calculates the intensity and polarity (Synergy vs. Distraction) of vortices.
- **Spatial Swirl**: Renders a high-performance AGSL swirl distortion that physically "warps" the seating chart UI around the vortex centers.

### 37. Ghost Ray (`ray/GhostRayEngine.kt`, `ray/GhostRayActivity.kt`, `ray/GhostRayShader.kt`)
A **Neural Directional Pointer** for spatial navigation.
- **Sensor Fusion**: Uses the device's rotation vector sensor to project a volumetric beam into the seating chart canvas.
- **Intersection Haptics**: Triggers tactile "Thuds" when the ray intersects with student icons, enabling eyes-free navigation.
- **Volumetric Beam**: Renders a glowing AGSL beam that reacts to device orientation in real-time.

### 38. Ghost Orbit (`orbit/GhostOrbitEngine.kt`, `orbit/GhostOrbitLayer.kt`, `orbit/GhostOrbitShader.kt`)
Visualizes classroom social dynamics as a **Classroom Galaxy**.
- **Orbital Dynamics**: Maps students to orbits where Speed is driven by log frequency (Engagement) and Radius is driven by log polarity (Stability).
- **Social Suns**: High-positive-engagement students act as gravitational "Suns" that other students orbit around.
- **Gravity Wells**: Renders procedural AGSL nebula and gravity well effects driven by orbital energy.

### 39. Ghost Entropy (`entropy/GhostEntropyEngine.kt`, `entropy/GhostEntropyLayer.kt`, `entropy/GhostEntropyShader.kt`)
Measures **Behavioral Uncertainty** and academic variance.
- **Shannon Entropy**: Calculates the unpredictability of student behavior types using a normalized entropy model.
- **Neural Turbulence**: Visualizes high-entropy nodes with "Heat Distortion" and "Thermal Noise" shaders.
- **Entropy Analysis**: Generates macroscopic reports identifying "High-Entropy Nodes" (Logic parity with `Python/ghost_entropy_analyzer.py`).

### 40. Ghost Zenith (`zenith/GhostZenithEngine.kt`, `zenith/GhostZenithLayer.kt`, `zenith/GhostZenithShader.kt`)
Implements **Spatial Depth and Parallax Mapping**.
- **Neural Elevation**: Calculates a student's "Altitude" (buoyancy) based on academic performance and behavioral stability.
- **3D Parallax**: Uses device tilt sensors to shift the seating chart layers, creating a sense of 3D depth.
- **Zenith Shadows**: Renders dynamic shadows under student icons that scale with their calculated altitude.

### 41. Ghost Emergence (`emergence/GhostEmergenceEngine.kt`, `emergence/GhostEmergenceLayer.kt`, `emergence/GhostEmergenceShader.kt`)
A **Cellular Automata Simulation** for emergent behavior patterns.
- **Vitality Grid**: Simulates behavior diffusion and decay on a 10x10 grid using localized rules (Impulse, Diffusion, Decay).
- **Emergent Patterns**: Identifies "Growth Dominant" clusters vs. "Decay Voids" (Logic parity with `Python/ghost_emergence_analysis.py`).
- **Vitality Layer**: Renders the emergent field as a glowing, fluid background layer using an AGSL shader.

### 42. Ghost Spark (`GhostSparkEngine.kt`, `GhostSparkLayer.kt`, `GhostSparkShader.kt`)
A high-performance **Neural Particle System**.
- **Data Sparks**: Emits autonomous particles when logs are recorded, which then drift through the classroom's "Social Gravity" field.
- **Social Gravity**: Sparks are attracted to or repelled by student icons based on their energy levels.
- **BOLT Physics**: Optimized for 60fps with 300+ particles using spark object pooling and minimized Compose tracking overhead.

### 43. Ghost Pulse (`GhostPulseEngine.kt`, `GhostPulseLayer.kt`)
Visualizes **Neural Resonance** and activity clusters.
- **Temporal Resonance**: Identifies students who receive behavior logs within the same time window.
- **Activity Ripples**: Renders synchronized, expanding AGSL ripples from resonant student nodes, color-coded by behavior type.
- **O(Recent) Performance**: Uses DESC-sorted logs to identify resonant pulses in sub-millisecond time.

### 44. Ghost Link: Neural Dossier (`GhostLinkEngine.kt`)
Generates futuristic **Neural Dossiers** for individual students.
- **Stochastic AI Analysis**: Transforms student metadata into a high-fidelity Markdown report featuring predictive trajectories.
- **Neural Metrics**: Calculates "Cognitive Resonance", "Collaborative Flux", and "Academic Entropy" with deterministic seeding per student ID.
- **Logic Parity**: Maintains strict architectural alignment with the `Python/ghost_link.py` R&D prototype.

### 44b. Ghost Link: Neural Pairing (`link/GhostLinkEngine.kt`, `link/GhostLinkLayer.kt`)
Visualizes **Neural Pairing** between students based on spatial proximity and behavioral synergy.
- **Neural Strands**: Renders animated, pulsing AGSL strands between students who are "in sync."
- **Social Fabric**: Models the classroom as a dynamic network driven by real-time activity parity.

### 44c. Ghost Weaver (`weaver/GhostWeaverEngine.kt`, `weaver/GhostWeaverLayer.kt`, `weaver/GhostWeaverShader.kt`)
Visualizes **Academic Synergy** and **Homework Collaboration** as neural threads.
- **Academic Synergy**: Detects shared high-performance (>= 80%) on quiz assessments.
- **Homework Collaboration**: Identifies students who consistently complete the same homework assignments.
- **Neural Threads**: Renders glowing, interwoven AGSL fibers between synergistic students.

### 45. Ghost Flux (`GhostFluxEngine.kt`, `GhostFluxLayer.kt`, `GhostFluxShader.kt`)
Visualizes classroom engagement as **Neural Flow**.
- **Classroom Tempo**: Calculates a normalized flow intensity driven by log density and a sinusoidal "Tempo" factor (Logic parity with `Python/ghost_flux_simulator.py`).
- **Neural Haptics**: Synchronizes AGSL flow distortions with Android 15 "Flux Surge" haptic effects.
- **Flow Visualization**: Renders a fluid, glowing stream that flows between student icons based on interaction frequency.

### 46. Ghost Future (`GhostFutureEngine.kt`)
A stochastic engine for **Neural Classroom Simulation**.
- **Simulated Trajectories**: Uses heuristics and predictions from [GhostOracle] to generate a series of "Simulated Events" representing a likely future trajectory of the classroom.
- **Probabilistic Modeling**: Influences event probability based on historical negative counts and identified social friction.

### 47. Ghost Beacon (`beacon/GhostBeaconEngine.kt`, `beacon/GhostBeaconLayer.kt`, `beacon/GhostBeaconShader.kt`)
A data-driven **Neural Student Selection** engine.
- **Need for Interaction (NFI)**: Calculates a weighted probability score for each student based on behavioral and academic metrics.
- **Volumetric Beacon**: Renders a glowing "tractor beam" over the selected student using a volumetric AGSL shader.
- **Haptic Confirmation**: Integrates with device actuators to provide tactile feedback upon student selection.

### 48. Ghost Frost (`frost/GhostFrostEngine.kt`, `frost/GhostFrostLayer.kt`, `frost/GhostFrostShader.kt`)
Visualizes **Cold Zones** and areas of high classroom entropy.
- **Crystallization Logic**: Identifies students in "Concerning" states or near negative events to drive procedural frost growth.
- **Procedural Frost**: Renders organic ice crystal structures using a Voronoi-based AGSL shader.
- **Cold Zone Clustering**: Frost intensity increases in areas where multiple at-risk students are seated together.

### 49. Ghost Ink (`ink/GhostInkEngine.kt`, `ink/GhostInkLayer.kt`, `ink/GhostInkShader.kt`)
Implements **Persistent Spatial Annotations** for the seating chart.
- **Logical Space Storage**: Strokes are stored in the 4000x4000 logical canvas, ensuring they remain correctly positioned across zoom and pan states.
- **BOLT Thinning**: Implements distance-based point thinning to prevent memory explosion during long drawing sessions.
- **Neon Ink**: Renders glowing, high-fidelity lines using a specialized ink shader.

### 50. Ghost Mood Board (`mood/GhostMoodEngine.kt`, `mood/GhostMoodLayer.kt`, `mood/GhostMoodShader.kt`)
Visualizes the **Collective Classroom Atmosphere**.
- **Mood Synthesis**: Analyzes behavioral valence and academic intensity to determine states like Calm, Focused, or Turbulent.
- **Atmospheric Field**: Renders a shifting color field that reflects the classroom's aggregate mood and stability.
- **BOLT O(Recent) Analysis**: Utilizes DESC-sorted logs and early-exit loops for real-time climate tracking.

## 🔄 Logic Parity & The R&D Bridge

Most features in the Ghost Lab are mobile-optimized ports of pedagogical and spatial theories first modeled in the **Python Analysis Suite**.

To ensure consistency between platforms, maintainers should refer to the following documentation in the `Python/` directory:
- [**Ghost Lab Guide**](../../../../../../../../../Python/GHOST_LAB_GUIDE.md): Explains the "Logic Parity Bridge" and the mathematical constants (like the 2x coordinate scaling factor) shared between Python and Android.
- [**Python Ghost Suite**](../../../../../../../../../Python/README.md#ghost-lab-experimental-analysis): Catalogs the 30+ R&D scripts that serve as the "Gold Standard" for Android implementations.

### 74. Ghost Neural Origami (origami/GhostOrigamiLayer.kt, GhostOrigamiEngine.kt, GhostOrigamiShader.kt)
A high-fidelity spatial transition for the Seating Chart.
- **3D Folding**: Allows the UI to "fold" like a piece of paper using hardware-accelerated 3D rotations.
- **Neural Backstage**: Reveals a procedural "Backside Material" data-grid when folded.
- **Paper Crease**: Renders a dynamic AGSL crease shadow along the folding axis.

### 75. Ghost Radar (radar/GhostRadarLayer.kt, radar/GhostRadarEngine.kt, radar/GhostRadarShader.kt)
Localized behavioral resonance visualization.
- **Social Resonance**: Identifies spatiotemporal hotspots of behavioral activity.
- **Linear Decay**: Uses a linear distance and time decay model for resonance intensity.
- **BOLT Optimization**: Optimized log traversal with DESC-sorting and early-exits.

### 76. Ghost Mirage (mirage/GhostMirageLayer.kt, GhostMirageEngine.kt, GhostMirageShader.kt)
Neural Focus Tracking Heatmap.
- **Focus Mapping**: Captures teacher spatial attention by mapping touch interactions into a persistent neural heatmap.
- **Temporal Decay**: Features a linear time-decay model where focus "fades" naturally over time, encouraging broader classroom interaction.
- **AGSL Mirage**: Renders an ethereal shimmering heatmap using domain-warped FBM noise and a specialized Amber/Cyan palette.
- **BOLT Optimization**: Uses a fixed 20x20 primitive grid for zero-allocation 60fps rendering.

### 77. Ghost Sonar (sonar/GhostSonarLayer.kt, sonar/GhostSonarEngine.kt, sonar/GhostSonarShader.kt)
Spatial Engagement Discovery.
- **Quiet Zone Detection**: Identifies students with zero recent behavioral or academic interactions.
- **Expanding Ripple**: Renders an expanding AGSL sonar wave that sweeps the 4000x4000 canvas.
- **Haptic Pings**: Triggers tactile feedback (UI_CLICK) upon intersection with quiet students.

### 78. Ghost Carbon (carbon/GhostCarbonLayer.kt, carbon/GhostCarbonEngine.kt, carbon/GhostCarbonShader.kt)
Identifies "Behavioral Twins" in the classroom.
- **Signature Matching**: Uses cosine similarity between behavioral frequency vectors.
- **Resonance Bridges**: Visualizes connections between students with matching behavioral archetypes using pulsing AGSL bridges.

### 79. Ghost Rain (rain/GhostRainLayer.kt, rain/GhostRainEngine.kt, rain/GhostRainShader.kt)
A "Neural Rain" atmospheric simulation.
- **Atmospheric Physics**: Rain/Snow intensity scales with recent behavioral frequency.
- **Collision Detection**: BOLT-optimized droplet intersection with student icons for 60fps particle simulation.

### 80. Ghost Phoenix (phoenix/GhostPhoenixLayer.kt, phoenix/GhostPhoenixEngine.kt, phoenix/GhostPhoenixShader.kt)
Visualizes student resilience as a procedural fire aura.
- **Resilience Scoring**: Compares 24h struggle windows with 2h recovery windows.
- **Fire Aura**: Renders a "Phoenix Rising" effect around resilient students using specialized AGSL fire/ember shaders.

### 81. Ghost Weather (weather/GhostWeatherLayer.kt, weather/GhostWeatherEngine.kt, weather/GhostWeatherShader.kt)
Translates classroom state into a dynamic meteorological system.
- **Social Wind**: Social polarity balance drives wind direction and atmospheric turbulence.
- **Academic Lightning**: High-frequency quiz events trigger procedural AGSL lightning discharges.

## 🚧 Status: Experimental
These features require `GhostConfig.GHOST_MODE_ENABLED = true` and target **API 33+** (for `RuntimeShader` support), with some features requiring **API 35+** (Android 15). They are intended for research and development and may be subject to rapid changes.

---
*Documentation love letter from Scribe 📜*

### 51. Ghost Shell (shell/GhostShellLayer.kt, GhostShellEngine.kt, GhostShellShader.kt)
An immersive bottom dock for classroom management and health monitoring.
- **Neural Pulse**: Visualizes classroom "Health" and "Activity" as a reactive, glowing wave using an AGSL shader.
- **Health Metrics**: Calculates a real-time Health Index based on the balance of behavioral logs in a 5-minute sliding window.
- **Immersive Control**: Provides a glassmorphic interface for toggling high-frequency Ghost modes (HUD, Vision, Strategist, Aurora).
- **BOLT Optimization**: Uses a single-pass O(N) calculation loop for metrics and hoisted shaders for zero-allocation rendering.

### 52. Ghost Deck (GhostDeckLayer.kt, GhostDeckEngine.kt, GhostDeckShader.kt)
A high-fidelity **Neural Student Card Stack** for rapid classroom review.
- **Swipe-to-Log**: Allows teachers to rapidly review student cards and log behavior with intuitive swipe gestures (Right = Positive, Left = Negative).
- **Neural Affinity**: Calculates a priority-based "Affinity" score (Student Turbulence) to drive card order and visual intensity.
- **AGSL Neural Flux**: Features a reactive domain-warping background shader that reflects student affinity and real-time swipe progress.
- **BOLT Optimization**: Uses manual index-based loops for deck synthesis and hoisted AGSL shaders for zero-allocation 60fps rendering.

### 53. Ghost Halo (GhostHaloLayer.kt, GhostHaloShader.kt)
A **Neural Halo** visualization for peak performing students.
- **Peak Performance Identification**: Queries [GhostInsightEngine] to identify students in the "OPTIMAL" state based on high grades and zero negative behaviors.
- **AGSL Neural Halo**: Renders a thin, rotating, and pulsing golden/cyan ring with an ethereal light scatter around student icons.
- **BOLT Optimization**: Uses `derivedStateOf` and identity-based caching to minimize recompositions and zero-allocation shader drawing.

### 54. Ghost Flare (flare/GhostFlareLayer.kt, flare/GhostFlareEngine.kt, flare/GhostFlareShader.kt)
High-intensity behavioral milestone visualization.
- **Milestone Detection**: Scans for students with 3+ recent logs and a high positive balance.
- **AGSL Flare**: Renders an anamorphic lens flare and starburst effect using a high-performance shader.
- **BOLT Optimization**: Offloads detection logic to a background pipeline to maintain 60fps.

### 55. Ghost Comet (comet/GhostCometLayer.kt, comet/GhostCometEngine.kt, comet/GhostCometShader.kt)
High-momentum activity trails for student interactions.
- **Social Gravity**: Activity trails are attracted to neighboring students, simulating "social contagion."
- **Physics Engine**: BOLT-optimized zero-allocation physics with 0.97 drag.
- **AGSL Trails**: Renders tapering, glowing streaks using API 33+ `RuntimeShader`.

### 56. Ghost Glyph (glyph/GhostGlyphLayer.kt, glyph/GhostGlyphEngine.kt, glyph/GhostGlyphShader.kt)
Neural gesture-based logging system.
- **Gesture Recognition**: Translates custom touch patterns (glyphs) directly into behavior logs.
- **AGSL Feedback**: Provides real-time visual feedback as the user draws the glyph.

### 57. Ghost Strategist (strategist/GhostStrategistLayer.kt, strategist/GhostStrategistEngine.kt)
A generative AI tactical engine for pedagogical planning.
- **Strategic Advice**: Analyzes classroom entropy and tension to suggest seating changes or intervention strategies.

### 58. Ghost Vision (vision/GhostVisionLayer.kt, vision/GhostVisionEngine.kt, vision/GhostVisionActivity.kt)
A sensor-driven AR viewport for the classroom.
- **AR Overlay**: Uses device orientation to project data-rich overlays onto a camera-mediated view of the classroom.

### 59. Ghost Architect (architect/GhostArchitectLayer.kt, architect/GhostArchitectEngine.kt, architect/GhostArchitectShader.kt)
A strategic layout generative engine.
- **Generative Design**: Proposes optimal seating arrangements based on predicted academic synergy and minimized social friction.

### 60. Ghost Navigator (navigator/GhostNavigatorLayer.kt, navigator/GhostNavigatorEngine.kt)
A high-performance mini-map for spatial navigation.
- **Global Overview**: Provides a persistent, interactive thumbnail of the 4000x4000 canvas.
- **Fast-Travel**: Allows users to rapidly pan the main canvas by interacting with the navigator.

### 61. Ghost Spotlight (spotlight/GhostSpotlightLayer.kt, spotlight/GhostSpotlightShader.kt)
A focused pedagogical assessment mode.
- **Attention Isolation**: Dims the rest of the chart to highlight a single student or group during assessment.

### 62. Ghost Glance (glance/GhostGlanceSurface.kt, glance/GhostGlanceEngine.kt)
Double-tap preview for student dossiers.
- **Rapid Insight**: Provides a non-interruptive popup showing recent logs and insights when a student icon is double-tapped.

### 63. Ghost Filter (filtering/GhostFilterScreen.kt, filtering/GhostFilterViewModel.kt)
An optimized student list and filtering system.
- **High-Speed Search**: Leverages BOLT patterns to filter large student lists with sub-millisecond latency.

### 64. Ghost LOD (lod/GhostLODEngine.kt)
Adaptive Level of Detail rendering.
- **Dynamic Detail**: Automatically simplifies student icons and disables complex shaders when zooming out or when high student counts are detected.

### 65. Ghost Adaptive (adaptive/GhostAdaptiveLayer.kt, adaptive/GhostAdaptiveEngine.kt)
Density-aware layout optimization.
- **UI Scaling**: Adjusts icon sizes and spacing dynamically based on classroom density to prevent overlap.

### 66. Ghost Memento (memento/GhostMementoMapper.kt, memento/GhostMementoStore.kt)
Persistent command history and "Long-Term Memory."
- **Atomic Recovery**: Re-hydrates undo/redo stacks across app restarts using encrypted DataStore storage.
- **Encryption**: History is hardened using `SecurityUtil` to ensure privacy.

### 67. Ghost Silhouette (silhouette/GhostSilhouetteLayer.kt, silhouette/GhostSilhouetteShader.kt)
Neural drag placeholders.
- **Spatial Memory**: Renders a glowing "ghost" icon at the student's original position during drag operations.

### 68. Ghost Morph (morph/GhostDossierScreen.kt)
Shared-element neural transitions.
- **Fluid Continuity**: Implements seamless transitions between the seating chart and detailed student views.

### 69. Ghost Lasso (lasso/GhostLassoLayer.kt, lasso/GhostLassoEngine.kt)
Gesture-based multi-selection.
- **Neural Lasso**: Allows users to select multiple students by drawing a freeform path around them.

### 70. Ghost Glitch (glitch/GhostGlitchLayer.kt, glitch/GhostGlitchEngine.kt, glitch/GhostGlitchShader.kt)
Neural feedback during spatial conflicts.
- **Collision Feedback**: Renders a "digital glitch" effect when student icons are moved into invalid or conflicting positions.

### 71. Ghost Kinetic (kinetic/GhostKineticEngine.kt)
Physics-based momentum for student icons.
- **Inertial Drag**: Icons exhibit momentum and friction when released after a drag gesture.

### 72. Ghost Seeds (util/GhostSeedEngine.kt)
Native Android Home Screen shortcuts.
- **Neural Seeds**: Allows teachers to "seed" specific students to the home screen for instant access to their dossiers.
- **PII Masking**: Labels are hardened (e.g., "J. DOE") to prevent data leakage on the OS level.

### 73. Ghost Stream (stream/GhostStreamLayer.kt, GhostStreamEngine.kt, GhostStreamShader.kt)
A real-time neural activity ticker for classroom events.
- **Event Synthesis**: Aggregates behavior, quiz, and homework logs into a unified data stream using O(N) single-pass analysis.
- **Glassmorphic Ticker**: Renders a translucent, scrolling overlay with high-fidelity typography and entry/exit animations.
- **Data Flow Shader**: Uses an AGSL background shader to visualize the "velocity" of classroom data.

## 💡 Tribal Knowledge & Implementation Secrets

### 1. Spatial Coordinate Mapping
- **The "Flip"**: Composable `Canvas` coordinates (0,0 at top-left) often need translation when passed to shaders that expect normalized UVs or centered coordinates.
- **Aura Centering**: Absolute pixel coordinates must be normalized *inside* the shader using `iResolution` to maintain visual consistency across screen sizes.
- **The 4000x4000 Canvas**: The logical seating chart operates on a fixed 4000-unit coordinate system, independent of device resolution.

### 2. BOLT Performance Strategies
- **Zero-Allocation Rendering**: High-frequency draw loops (60fps) use primitive arrays and manual index-based loops to eliminate iterator churn and GC pressure.
- **Shader Pooling**: To avoid the "Uniform Overwrite" bug, complex layers maintain a pool of `RuntimeShader` instances, ensuring each student node has its own isolated GPU state.
- **O(Recent) Logic**: Analysis engines (like Quasar and Pulse) utilize chronologically DESC-sorted logs with early-exit loops to achieve $O(\text{Recent})$ performance rather than $O(\text{Total})$.

### 3. Physics & Metaphors
- **Social Friction (1.5x/2.5x)**: The engines (Cognitive, Magnetar, Vector) apply higher mathematical weights to negative logs, modeling the disproportionate impact disruptive behavior has on classroom dynamics.
- **Atmospheric Proxy**: Hardware sensors like battery temperature and ambient light are used as metaphors for classroom "energy" and "mood," driving background visualizations like Ghost Ion and Ghost Horizon.

---
*Documentation love letter from Scribe 📜*
