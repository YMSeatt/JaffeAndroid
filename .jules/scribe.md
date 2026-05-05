# 📜 Scribe's Journal - Seating Chart & Behavior Logger

## 💡 Tribal Knowledge & Implementation Secrets

### 1. Ghost Cognitive Engine Physics
- **Repulsion Logic**: The `REPULSION_CONSTANT` (500,000f) is calibrated for a 4000x4000 canvas. If the canvas size changes, this constant may need exponential scaling.
- **Social Distancing**: The engine treats negative behavior as a physical repulsion force. A student with 2+ negative marks effectively "pushes" others away 2.5x harder than a neutral student.
- **Equilibrium**: 50 iterations with 0.9 damping was found to be the "sweet spot" where the layout stabilizes without excessive jitter or long wait times.

### 2. AGSL Shader Coordinate Mapping
- **The "Flip"**: Composable `Canvas` coordinates (0,0 at top-left) often need translation when passed to shaders that expect normalized UVs or centered coordinates.
- **Aura Centering**: `iCenter` in `COGNITIVE_AURA` must be passed in absolute pixel coordinates from the Compose `onSizeChanged` or `layout` phase, then normalized *inside* the shader using `iResolution`.

### 3. Voice Assistant Parsing
- **Nicknames over Full Names**: The `handleLogCommand` prioritized nicknames. If a student has a nickname "JJ" and a full name "John Jones", "Log good behavior for JJ" will match more reliably than the full name due to substring matching.
- **Keyword Collision**: Using "bad" in a comment might accidentally trigger a "Negative behavior" log if the parsing logic isn't careful. Current implementation uses simple `contains()`, so teachers should be advised to use specific trigger phrases.

### 4. Neural Map Connections
- **The Star Pattern**: To avoid $O(N^2)$ visual clutter, the `NeuralMapLayer` connects group members in a "star pattern" (everyone to the first member) rather than a "complete graph" (everyone to everyone). This keeps the UI clean while still showing group cohesion.

### 5. Cross-Platform Security & Data Migration
- **The Bridge Key**: The hardcoded key in `SecurityUtil.kt` (Android) and `encryption_key.py` (Python) is the "cryptographic glue" that allows users to move their classroom between desktop and mobile. While Android has upgraded to KeyStore-managed keys (`fernet.key.v2`), this `FALLBACK_KEY` is preserved specifically for data ingestion from the Python app.
- **Hashing Parity**: Python's SHA3-512 hashing was chosen for speed, while Android's PBKDF2 with 100,000 iterations was chosen for security hardening. The Android `verifyPassword` method includes specific logic to detect the length of a hash (128 chars for SHA3-512) and apply the correct legacy verification to ensure users don't get locked out when migrating.

### 6. Database Migration Strategy
- **Relational Groups**: Transitioning `groupId` from `String` to `Long` (v6 to v7) was critical for supporting Room's `@ForeignKey` constraints. This ensures that deleting a `StudentGroup` either cascades or nullifies member students.
- **JSON Flexibility**: The `marksData` field in `QuizLog` (v12) and `HomeworkLog` (v11) is a strategic "future-proofing" move. Instead of adding new columns for every possible scoring metric (effort, accuracy, etc.), developers can store them in a JSON map, processed by the `Converters` class.

### 7. Ghost Catalyst Kinetics
- **The Metaphor**: Classroom behavior is modeled as a chemical system where student actions act as catalysts.
- **Spatio-temporal Inversion**: `GhostCatalystEngine` processes DESC-sorted database logs in reverse to scan chronologically, identifying localized chain reactions within a 300s/800-unit window.
- **Normalization**: Macroscopic metrics like "Reaction Rate" are normalized to a 5-minute window for cross-classroom comparison.
- **Activation Energy**: This heuristic represents the classroom's "resistance" to social volatility. High global frequency (engagement) lowers the activation energy required for a chain reaction.

### 8. Ghost Ion "Neural Ionization"
- **Thermal Correlation**: The engine uses hardware battery temperature as a proxy for classroom "energy." A 0.3x density boost is applied when the battery temperature rises above 25°C, simulating a "heated" atmosphere.
- **Charge Polarity**: Behavior logs are mapped to an electrostatic charge. "Participating" and "Positive" logs are cationic (+), while "Disruptive" and "Negative" logs are anionic (-).
- **GPU Batching**: To avoid hitting the JNI overhead of multiple `setFloatUniform` calls, all 10 student ion points are batched into a single `float4 iPoints[10]` array.

### 9. Ghost Entropy "Neural Turbulence"
- **The "First-20" Tradeoff**: `GhostEntropyLayer` only samples the first 20 students to calculate the global distortion intensity. This is a deliberate performance cap to ensure 60fps on mid-range hardware.
- **Academic Normalization**: The variance normalization uses a 4.0x multiplier because the maximum possible variance of a value in the range [0, 1] is 0.25.
- **Shannon Normalization**: Behavioral entropy is normalized against `ln(5)`, assuming a maximum of 5 distinct behavior types in the standard classroom log schema.

### 10. Ghost Helix "Neural DNA"
- **The Genetic Mapping**: Behavioral and academic data are mapped to four bases: Adenine (Positive Behavior), Thymine (Negative Behavior), Cytosine (High Academic >60%), and Guanine (Low Academic <60%).
- **Twist Dynamics**: The `twistRate` (1.0..3.0) is driven by the frequency of academic logs (C/G), making the helix spin faster for academically active students.
- **Stability Heuristic**: `stability` (0.1..1.0) is calculated as the ratio of positive to total logs. A stability score below 0.5 triggers noticeable procedural jitter in the AGSL shader to visually signal "behavioral noise."

### 11. Ghost Quasar Energy and Polarity
- **Sliding Window**: The engine uses a hardcoded 30-minute window (`30 * 60 * 1000L`). Events older than this are ignored to focus on current classroom "energy."
- **Activation Threshold**: A student only achieves "Quasar" status (and visual auras) after reaching 3 behavioral events within the window. This prevents UI flicker from single, isolated logs.
- **Polarity Mapping**: Polarity is mapped linearly from -1.0 (Magenta/Negative) to +1.0 (Cyan/Positive). The visual core of the accretion disk shifts color based on this normalized float.
- **Shader Batching/Pooling**: To avoid the "Uniform Overwrite" bug (where multiple draw calls sharing the same native shader object overwrite each other's uniforms before the GPU executes), the `GhostQuasarLayer` maintains a `shaderPool`. Each active Quasar is assigned its own `RuntimeShader` instance from the pool.

### 13. Conditional Formatting DSL and Performance
- **Early-Exit Optimization**: Rules with `activeModes` or `activeTimes` are filtered at the start of the `checkCondition` function. This prevents executing more complex behavioral or academic analysis if the rule is logically inactive.
- **Case-Insensitivity & Allocations**: Behavior names in rules are split once into a `Set<String>` during rule decoding. Comparisons use `lowercaseCache` to avoid per-event string allocations when checking against hundreds of historical logs.
- **Sorting Requirement**: The `behavior_count` condition type assumes the `behaviorLog` is sorted in descending chronological order. It uses a manual loop with an early `break` when it hits events outside the `timeWindowHours`, achieving O(Recent) performance instead of O(TotalLogs).

### 12. Ghost Magnetar "Social Polarity"
- **Negativity Bias**: The engine applies a 1.5x multiplier to negative behavioral logs when calculating social magnetic strength. This intentionally models the disproportionate impact disruptive students have on classroom cohesion.
- **Hardware Skewing**: The `iHeading` uniform in `GhostMagnetarLayer` passes the device's actual magnetometer heading. Rotating the physical device rotates the virtual field, a deliberate UI metaphor for "reorienting" social analysis.
- **BOLT Performance Cap**: The shader is hard-coded to 15 dipoles (`iDipolePos[15]`). `GhostMagnetarLayer` uses `students.take(15)` to ensure visual stability and prevent out-of-bounds uniform access in the AGSL program.

### 14. Ghost Vector "Social Gravity" Metrics
- **mG (milli-Gravities)**: The synthetic unit for social force intensity. Calibrated such that a standard interaction at 400 units distance yields approximately 15-60 mG.
- **The 85mG Threshold**: This "High Turbulence" marker is the mathematical equivalent of 1.5x standard deviation in the Python reference suite. It represents a state where social friction outweighs collaboration by a significant margin.
- **Force Multipliers**:
    - `FORCE_FRICTION (-100f)`: Repulsion is weighted 1.6x more heavily than `FORCE_COLLABORATION (60f)` to highlight disruptive social dynamics.
    - `FORCE_NEUTRAL (15f)`: Provides a baseline "social cohesion" pull even in the absence of explicit logs.
- **Zero-Allocation Needle**: The `GhostVectorLayer` utilizes a pooled `RuntimeShader` approach to render hundreds of needles without triggering GC pauses, capturing uniforms just-in-time during the `Canvas` draw pass.

### 15. Ghost Memento Architecture
- **Metaphor**: A specialized "Long-Term Memory" engine for command history persistence.
- **Persistence Strategy**: Uses a high-integrity mapping system (`GhostMementoMapper`) to transform complex production `Command` objects into pure-data `MementoCommand` DTOs.
- **Encryption**: History is serialized to JSON and encrypted using `SecurityUtil` before storage in Jetpack DataStore, ensuring privacy for historical behavioral and academic records.
- **Atomic Recovery**: The `SeatingChartViewModel` re-hydrates its undo/redo stacks from the store on launch, enabling a seamless "Resume Work" experience across app restarts.

### 16. Ghost Osmosis "Neural Diffusion" Calibration
- **The Gaussian Kernel**: The engine uses a Gaussian Radial Basis Function (RBF) for spatial influence, specifically calibrated with **$\sigma = 400$**. This value was chosen to ensure that a student's "potential" remains significant within their immediate group (approx 800 units) but tapers off before affecting the entire 4000-unit canvas.
- **The 320,000 Constant**: To avoid expensive `pow()` and `sqrt()` operations, the code uses a pre-calculated denominator of **320,000f** ($2\sigma^2$).
- **Color Mapping Protocol**:
    - **Blue (Cyan)**: Knowledge Potential (Academic logs).
    - **Green**: Positive Behavioral Concentration.
    - **Red**: Negative Behavioral Concentration.
- **Shader Pooling Logic**: The `GhostOsmosisLayer` uses a strict pooling strategy for `RuntimeShader` and `ShaderBrush`. This is required because `ShaderBrush` captures uniforms at the moment of application; drawing multiple patches with different uniforms in one frame requires unique object instances to prevent visual "bleeding" or overwrites.

### 17. Ghost Pulsar "Harmonic Synchronicity" Calibration
- **The 0.2 Threshold**: The sync threshold for "Harmonic Bonds" is hardcoded to 0.2 LPM. This value was calibrated against the Python reference suite to represent a +/- 12% frequency variance for active students.
- **Phase Derivation**: Phase is calculated as `(currentTime * frequency) % 1.0`. By using absolute system time as the base, students with the *exact* same frequency will naturally pulse in perfect synchronization regardless of when they were added to the chart.
- **Wave Interference**: Visual constructive interference in the AGSL shader occurs when two synchronized students are near each other, with the `sin()` waves combining their amplitudes and shifting colors toward a brighter composite.

### 18. Ghost Flora "Neural Botanical" Calibration
- **The Saturation Point**: The complexity metric is normalized against a **saturation point of 10 logs**. Beyond this, the visual density of petals reaches a maximum to prevent GPU overdraw and maintain 60fps.
- **Academic Baselines**: In the absence of data, students are assigned a **0.75f average growth factor** (0.7 Quiz, 0.8 Homework). This creates a "healthy baseline" sprout rather than an empty canvas for new students.
- **Vitality Color Shift**: The color interpolation in the AGSL shader uses a linear `mix()` between Cyan (Positive) and Magenta (Negative). Magenta was chosen specifically for its visual "friction" against the standard app palette.
- **Stable Seeding**: The procedural seed is derived via `studentId % 1000`. This ensures that while every student has a unique flower, it remains consistent across sessions unless their database ID changes.
