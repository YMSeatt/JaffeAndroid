# 🧪 Ghost Ion: Neural Ionization Mapping

The **Ghost Ion** experiment models the classroom environment as a high-energy plasma field. It visualizes student engagement and behavior through the lens of "Neural Ionization," where student actions and even hardware signals (device temperature) influence the atmospheric charge of the seating chart.

## 🏛️ The "Ionization" Model

The engine identifies students as "Ion Points" based on their recent behavioral history and the physical state of the device.

### ⚛️ Ionic Potential & Charge Density
- **Charge (-1.0 to 1.0)**: Derived from the polarity of a student's last 5 behavior logs. Positive/Participating logs increase the charge (Cyan), while Negative/Disruptive logs decrease it (Red).
- **Density (0.0 to 1.0)**: Represents the "activity concentration" of a student. It is a combination of log frequency and **Battery Temperature**.
- **The Thermal Factor**: Device temperature is mapped to a 0.3x density boost. As the hardware works harder (simulating a "heated" classroom environment), the ionization effects become more pronounced and volatile.

### 🌐 Global Ion Balance
The engine calculates a classroom-wide balance by averaging the charges of all active ion points. This global metric shifts the background "Atmospheric Ionization" (the overall haze color) between calm blue and turbulent red.

---

## 🎨 Visual Mapping: AGSL Ionic Field

The `GhostIonLayer` utilizes a high-performance AGSL shader (`ION_FIELD`) to render the energy state.

### 🌟 Ion Cores
Each student with sufficient log density manifests as a glowing core. The core's color represents their current charge polarity, and its size represents their density.

### ⚡ Electrostatic Discharge
A procedural noise-based lightning effect. "Discharges" occur randomly near high-density students, simulating the unpredictable energy of a highly active classroom.

### 🌫️ Atmospheric Ionization
A semi-transparent haze that permeates the entire layer. The haze's base color is driven by the `iGlobalBalance` uniform, providing an ambient indicator of the classroom's overall behavioral "climate."

---

## ⚡ BOLT Optimization

To maintain 60fps performance on high-refresh-rate displays:
1.  **Background Processing**: Complex behavioral log analysis and charge calculations are offloaded to the `SeatingChartViewModel` background pipeline.
2.  **MutableState Tracking**: The layer reads `ionCharge` and `ionDensity` directly from `StudentUiItem` states, allowing ionization cores to follow dragging icons without triggering whole-layer recompositions.
3.  **Buffer Reuse**: The `iPoints` uniform is backed by a pre-allocated `FloatArray` (40 elements for 10 points) which is filled and passed to the GPU in a single operation, eliminating per-frame object churn.
4.  **Hardware Signal Caching**: Battery temperature is polled once per recomposition and cached, preventing excessive system calls while still reflecting thermal shifts.

---
*Documentation love letter from Scribe 📜*
