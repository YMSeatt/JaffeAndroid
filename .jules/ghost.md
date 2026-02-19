# 👻 Ghost's Journal: R&D & "What if?" Scenarios

## 🌀 Experiment: Ghost Portal (Drag & Drop Evolution)
**Date:** 2026-02-13
**Status:** PROTOTYPE COMPLETE

### 🌟 The Vision
In 2027, the boundaries between applications will blur. "Ghost Portal" is a PoC for a future where classroom data is fluid. Teachers can "teleport" students between different classroom environments or external AI analyzers with a single gesture.

### 🛠️ The Tech
- **Android 15 Drag & Drop:** Integrated `Modifier.dragAndDropSource` and `Modifier.dragAndDropTarget` into the Compose UI.
- **AGSL Shaders:** Created `GhostPortalShader` to provide a futuristic "Wormhole" effect.

---

## 🕸️ Experiment: Ghost Lattice (Social Graph Visualization)
**Date:** 2026-05-20
**Status:** PROTOTYPE COMPLETE

### 🌟 The Vision
Teachers often "feel" the social dynamics of a room but can't see them. "Ghost Lattice" makes the invisible visible. It visualizes student relationships as a glowing neural network, helping teachers identify social clusters, potential friction points, and isolated students at a glance.

### 🛠️ The Tech
- **AGSL Neural Shaders**: Implemented a specialized `NEURAL_LATTICE` shader that draws glowing, pulsing connections between student coordinates. It uses bounding-box optimization for efficient rendering of multiple lines.
- **Social Inference Engine**: A local engine that analyzes physical proximity and behavioral history to determine "Collaboration" vs "Friction" links.
- **Python Bridge**: `ghost_lattice.py` demonstrates how complex social graph metrics (cohesion, turbulence) can be computed from raw JSON exports.

### 🔦 The Discovery
- **Shader Batching**: Drawing multiple lines with individual `RuntimeShader` instances is performant enough for a PoC (~30-50 edges), but a single multi-line shader using uniform arrays would be better for larger classes.
- **Dynamic Recalculation**: By observing the `StudentUiItem` position states, the lattice updates in real-time as students are dragged, creating a "stretchy" social web effect.

### 💡 The "What if?"
*What if the lattice could predict 'social contagion'—where a negative behavior from one node travels through the edges to affect others?*

---

## 🌌 Experiment: Ghost Aurora (Classroom Climate Visualization)
**Date:** 2027-02-15
**Status:** PROTOTYPE COMPLETE

### 🌟 The Vision
In 2027, teachers don't just "feel" the room; they see it. "Ghost Aurora" translates the aggregate classroom energy into a beautiful, atmospheric visualization. It provides a non-distracting, ambient indicator of the "Classroom Climate," allowing teachers to gauge the mood without looking at specific logs.

### 🛠️ The Tech
- **AGSL Domain Warping**: Implemented a sophisticated aurora shader using multi-layered FBM and domain warping for fluid, ethereal motion.
- **Climate Engine**: A logic engine that blends colors based on behavior ratios and scales intensity with activity frequency.

### 🔦 The Discovery
- **Ambient Awareness**: We discovered that mapping "Climate" to a background layer reduces cognitive load compared to traditional charts.
- **Atmospheric Transition**: The smooth color blending between Cyan (Positive) and Red (Negative) creates a "weather-like" feel that is more intuitive than binary status indicators.

---

## 🛰️ Experiment: Ghost Vector (Social Gravity Visualization)
**Date:** 2026-08-10
**Status:** PROTOTYPE COMPLETE

### 🌟 The Vision
In 2027, social dynamics aren't just lines; they are vectors of influence. "Ghost Vector" translates the abstract social lattice into a concrete mathematical model of "Social Gravity." It helps teachers see the "pull" of collaborative groups and the "push" of social friction.

### 🛠️ The Tech
- **Vector Summation Engine**: Implemented `GhostVectorEngine` to calculate net social forces acting on students.
- **AGSL Needle Shaders**: Created `GhostVectorShader` to render directional indicators that scale with force magnitude.
- **Python Data Bridge**: `ghost_vector_analysis.py` provides a path for server-side or desktop-based deep analysis of classroom cohesion.

### 🔦 The Discovery
- **Emergent Hotspots**: By summing forces, we discovered that students positioned between two "Friction" clusters exhibit high vector magnitude and rapid color shifts (Cyan to Magenta), making them easy to identify as "at-risk" nodes even without looking at raw logs.
- **Dynamic Feedback**: Real-time vector updates during student movement provide immediate feedback on how a seating change might alter the "Social Gravity" of the surrounding nodes.

---

## 🌈 Experiment: Ghost Spectra (Data Refraction)
**Date:** 2026-11-15
**Status:** PROTOTYPE COMPLETE

### 🌟 The Vision
In 2027, "Data Transparency" is a literal concept. "Ghost Spectra" allows teachers to see through the surface of student icons into the "spectral data" underneath. By dragging a virtual prism, they reveal the hidden components of student engagement, behavior, and academic performance as a dispersive light effect.

### 🛠️ The Tech
- **AGSL Chromatic Dispersion**: Implemented `SPECTRA_PRISM` shader that simulates the refractive index of glass to create rainbow-like separation of data layers.
- **Spectral Engine**: Calculates "Dispersion" and "Agitation" metrics from behavioral variance.
- **Python Bridge**: `ghost_spectra_analyzer.py` provides offline spectroscopic analysis of exported classroom JSON data.

### 🔦 The Discovery
- **Refractive Index of Data**: We discovered that mapping "Behavioral Variance" to the dispersion uniform created a natural visual metaphor: "messy" data breaks more light, while "stable" data is clearer.
- **Interactive Discovery**: The draggable prism encourages "exploratory debugging" of the classroom state rather than static report reading.

---
*Ghost - Rapid Prototyping for the Classroom of 2027*
