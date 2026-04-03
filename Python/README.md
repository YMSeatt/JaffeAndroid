# 🐍 Seating Chart Desktop (Python)

This directory contains the Python-based desktop implementation of the Seating Chart & Behavior Logger. It provides a visual interface for managing classroom layouts, tracking student behavior, and generating detailed reports.

## 🛠️ Tech Stack

*   **Language**: Python 3
*   **UI Framework**: Tkinter (Standard Library)
*   **Theming**: `sv_ttk` (Sun-Valley Theme) for a modern look.
*   **Data Serialization**: `json`
*   **Encryption**: `cryptography` (Fernet) for secure data storage.
*   **Excel Reporting**: `openpyxl`
*   **Image Processing**: `pillow` (PIL)
*   **Single Instance Locking**: `portalocker`

## 📂 Project Structure

*   `seatingchartmain.py`: The entry point and main application logic. Coordinates the UI, canvas interactions, and data synchronization.
*   `commands.py`: Implements the **Command Pattern**. Every user action (moving students, logging behavior, changing settings) is encapsulated as a `Command` object, enabling a robust multi-step Undo/Redo system.
*   `data_encryption.py`: Handles Fernet encryption and decryption for the application's JSON data files.
*   `data_locker.py`: Manages file-level access and integrity.
*   `dialogs.py`: Contains custom Tkinter dialogs for adding/editing students, furniture, and logging events.
*   `quizhomework.py`: Logic and dialogs specifically for managing quiz and homework templates.
*   `other.py`: Miscellaneous utilities, including `PasswordManager`, `FileLockManager`, and the `HelpDialog`.
*   `undohistorydialog.py`: A visual interface for the Undo/Redo stack.

## 🚀 Setup & Execution

1.  **Install Dependencies**:
    ```bash
    pip install sv_ttk darkdetect openpyxl pillow cryptography portalocker
    ```
2.  **External Dependency: Ghostscript**:
    The "Export Layout as Image" feature generates PostScript files which are rasterized using `pillow`. For this to work, **Ghostscript** must be installed on your system and added to your `PATH`.
    - Windows: [Ghostscript Downloads](https://ghostscript.com/releases/gsdnld.html)
    - Linux: `sudo apt install ghostscript`
3.  **Run the App**:
    ```bash
    python seatingchartmain.py
    ```

## 🔐 Security & Persistence

*   **Encryption**: Data is stored in encrypted JSON files (`classroom_data_{version}.json`) using the Fernet (AES-128) specification.
*   **Passwords**: User passwords are hashed using **SHA3-512** via `hashlib`. Note: The Android application uses PBKDF2-HMAC-SHA256 for enhanced security and can automatically migrate legacy hashes.
*   **Locking**: The app uses `portalocker` to ensure only one instance of the application can access the data files at a time.
*   **Hardened**: Previous versions contained a hardcoded master recovery password hash; this has been removed to ensure zero-backdoor security.

## 🔄 Logic Parity

This application is designed to maintain logical parity with its [Android counterpart](../README.md).
*   **Data Structures**: Shared JSON schema for students, furniture, and logs.
*   **Coordinate Parity**: While Python uses a **2000x1500** logical canvas and Android uses **4000x4000**, the data is import-compatible.
*   **Feature Alignment**: Shared implementations of the Command pattern for Undo/Redo and complex conditional formatting rules.

## 👻 Ghost Lab (Experimental Analysis)

The `Python/` directory contains the **Ghost Lab Suite**—a collection of 30+ R&D scripts that serve as the logical foundation for the Android application's futuristic experimental features. These scripts process classroom data (exported via JSON v10) to generate high-fidelity insights, stylized blueprints, and mathematical models for "Neural" visualizations.

### 🏛️ Core Analysis Suite

| Script | Functional Category | Android Counterpart | Description |
| :--- | :--- | :--- | :--- |
| **`ghost_architect_analysis.py`** | Spatial Strategy | `ARCHITECT_MODE` | Calculates layout synergy scores for COLLABORATION and FOCUS goals. |
| **`ghost_blueprint.py`** | Visualization | `BLUEPRINT_ENGINE` | Generates stylized 1200x800 SVG classroom blueprints. |
| **`ghost_catalyst_analysis.py`** | Behavioral Kinetics | `CATALYST_MODE` | Maps behavioral chain reactions and social "Reaction Rates." |
| **`ghost_emergence_analysis.py`** | Cellular Automata | `EMERGENCE_MODE` | Simulates behavioral diffusion on a vitality grid. |
| **`ghost_entanglement.py`** | Social Physics | `ENTANGLEMENT_MODE` | Identifies "Quantum Links" between synchronized students. |
| **`ghost_entropy_analyzer.py`** | Predictability | `ENTROPY_MODE` | Calculates Shannon entropy for student behavior variance. |
| **`ghost_flora_analysis.py`** | Biological Metaphor | `FLORA_MODE` | Maps student performance to a digital "Neural Botanical" ecosystem. |
| **`ghost_flux_simulator.py`** | Temporal Flow | `FLUX_MODE` | Calculates "Neural Flow" intensity and classroom tempo. |
| **`ghost_future.py`** | Stochastic Modeling | `FUTURE_MODE` | Generates simulated future trajectories for the classroom. |
| **`ghost_glyph_analysis.py`** | Symbolic Logic | `GLYPH_MODE` | Processes neural gestures and symbolic behavioral signatures. |
| **`ghost_helix_analysis.py`** | Genomic Metaphor | `HELIX_MODE` | Transforms log history into "Neural DNA" sequences. |
| **`ghost_ion_analyzer.py`** | Atmospheric Energy | `ION_MODE` | Calculates classroom "Ionic Potential" and charge distribution. |
| **`ghost_lattice.py`** | Social Graph | `LATTICE_MODE` | Infers relationship networks based on proximity and logs. |
| **`ghost_link.py`** | AI Synthesis | `LINK_ENGINE` | Generates 2027-era "Neural Dossiers" with predictive insights. |
| **`ghost_magnetar_analysis.py`** | Social Magnetism | `MAGNETAR_MODE` | Models student interactions as a dipole-based magnetic field. |
| **`ghost_nebula_analyzer.py`** | Density Mapping | `NEBULA_MODE` | Visualizes classroom "Social Atmosphere" density. |
| **`ghost_orbit_analysis.py`** | Orbital Dynamics | `ORBIT_MODE` | Maps students to orbits around high-engagement "Social Suns." |
| **`ghost_osmosis_analyzer.py`** | Diffusion Modeling | `OSMOSIS_MODE` | Calculates "Knowledge Potential" and behavioral concentration. |
| **`ghost_pulsar_analyzer.py`** | Rhythmic Sync | `PULSAR_MODE` | Detects harmonic synchronicity and classroom rhythms. |
| **`ghost_quasar_analysis.py`** | Energy Focal Points | `QUASAR_MODE` | Identifies "Quasars" (students with rapid activity bursts). |
| **`ghost_ray_analysis.py`** | Spatial Navigation | `RAY_MODE` | Simulates volumetric beam intersections for eyes-free UI. |
| **`ghost_spark_simulator.py`** | Particle Physics | `SPARK_MODE` | Models "Data Sparks" in a social gravity field. |
| **`ghost_spectra_analyzer.py`** | Spectroscopy | `SPECTRA_MODE` | Identifies Infrared (at-risk) vs Ultraviolet (engaged) states. |
| **`ghost_strategist_analysis.py`** | AI Tactics | `STRATEGIST_MODE` | Generates pedagogical interventions using heuristic trees. |
| **`ghost_supernova_analysis.py`** | Criticality | `SUPERNOVA_MODE` | Measures classroom "Core Pressure" and explosion risk. |
| **`ghost_tectonics_analysis.py`** | Social Stability | `TECTONICS_MODE` | Identifies social fault lines and localized stress. |
| **`ghost_vector_analysis.py`** | Social Gravity | `VECTOR_MODE` | Calculates net social force vectors and cohesion index. |
| **`ghost_vision_analysis.py`** | Sensor Synthesis | `VISION_MODE` | Processes spatial AR viewport metadata. |
| **`ghost_vortex_analysis.py`** | Rotational Momentum | `VORTEX_MODE` | Detects "Social Whirlpools" and angular momentum. |
| **`ghost_warp_analysis.py`** | Spacetime Dilation | `WARP_MODE` | Visualizes behavioral "Gravity Wells" and grid distortion. |
| **`ghost_zenith_analysis.py`** | Parallax Mapping | `ZENITH_MODE` | Calculates student "Altitude" and 3D depth offsets. |

### 🚀 Execution Model

Most scripts can be executed independently from the command line, accepting a classroom JSON export (v10 format) as their primary input.

**Usage:**
```bash
python ghost_vector_analysis.py path/to/classroom_data_v10.json
```

**Output:**
Scripts generate high-fidelity Markdown reports to `stdout` or produce visual artifacts (like `.svg` blueprints) in the `Python/` directory. These reports serve as the deterministic gold-standard for the Android app's implementation.

---
*Documentation love letter from Scribe 📜*
